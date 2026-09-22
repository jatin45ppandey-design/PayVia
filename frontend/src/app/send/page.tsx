'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { signPayload } from '@/lib/crypto';
import { buildCanonicalPayload } from '@/lib/canonicalPayload';
import Link from 'next/link';

interface ResolvedUser {
  id: string;
  fullName: string;
  payviaHandle: string;
}

interface PaymentSuccessResult {
  publicReference: string;
  amount: number;
  counterpartyName: string;
  counterpartyHandle: string;
  mode?: string;
  status?: string;
}

export default function SendMoneyPage() {
  const router = useRouter();
  const { user, loading } = useAuth();
  
  const [handle, setHandle] = useState('');
  const [amount, setAmount] = useState('');
  const [mode, setMode] = useState<'ONLINE' | 'OFFLINE'>('ONLINE');
  
  const [resolving, setResolving] = useState(false);
  const [resolvedUser, setResolvedUser] = useState<ResolvedUser | null>(null);
  const [resolveError, setResolveError] = useState('');

  const [sending, setSending] = useState(false);
  const [sendError, setSendError] = useState('');
  const [success, setSuccess] = useState<PaymentSuccessResult | null>(null);

  useEffect(() => {
    if (!loading && !user) {
      router.push('/login');
    }
  }, [user, loading, router]);

  const resolveHandle = async () => {
    if (!handle.trim()) return;
    setResolving(true);
    setResolveError('');
    setResolvedUser(null);
    try {
      const res = await fetchApi('/api/users/resolve?handle=' + encodeURIComponent(handle));
      if (res.ok) {
        setResolvedUser(await res.json());
      } else {
        setResolveError('Handle not found');
      }
    } catch {
      setResolveError('Error resolving handle');
    } finally {
      setResolving(false);
    }
  };

  const handleOnlineSend = async () => {
    const res = await fetchApi('/api/payments/send', {
      method: 'POST',
      body: JSON.stringify({
        receiverHandle: handle,
        amount: parseFloat(amount)
      })
    });
    
    if (res.ok) {
      const data = await res.json();
      setSuccess({
        publicReference: data.publicReference,
        amount: data.amount,
        counterpartyName: data.counterpartyName,
        counterpartyHandle: data.counterpartyHandle,
        mode: data.mode,
        status: data.status
      });
    } else {
      const data = await res.json();
      setSendError(data.message || 'Payment failed');
    }
  };

  const handleOfflineSend = async () => {
    const deviceRes = await fetchApi('/api/devices');
    if (!deviceRes.ok) {
      throw new Error('Failed to fetch your devices');
    }
    const devices = await deviceRes.json();
    const activeDevice = devices.find((d: { id: string; status: string }) => d.status === 'ACTIVE');
    if (!activeDevice) {
      throw new Error('No ACTIVE device found for this account. Please register this browser in Devices.');
    }

    const transactionId = crypto.randomUUID();
    const nonce = crypto.randomUUID();
    const sequenceNumber = Date.now(); 
    const createdAt = new Date().toISOString();
    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString();
    const amtStr = parseFloat(amount).toFixed(2);

    const canonicalPayload = buildCanonicalPayload(
      "1", transactionId, user!.id, activeDevice.id, resolvedUser!.id,
      amtStr, "INR", nonce, sequenceNumber.toString(), createdAt, expiresAt
    );

    const signature = await signPayload(activeDevice.id, canonicalPayload);

    const intentPayload = {
      version: "1", transactionId, senderDeviceId: activeDevice.id,
      receiverUserId: resolvedUser!.id, amount: parseFloat(amtStr),
      currency: "INR", nonce, sequenceNumber, createdAt, expiresAt, signature
    };

    const res = await fetchApi('/api/payments/offline/queue', {
      method: 'POST',
      body: JSON.stringify(intentPayload)
    });

    if (res.ok) {
      const data = await res.json();
      setSuccess({
        publicReference: data.publicReference,
        amount: data.amount,
        counterpartyName: data.receiverName,
        counterpartyHandle: data.receiverHandle,
        mode: data.mode,
        status: data.status
      });
    } else {
      const data = await res.json();
      throw new Error(data.message || 'Offline intent failed');
    }
  };

  const handleSend = async () => {
    setSendError('');
    setSending(true);
    try {
      if (mode === 'ONLINE') {
        await handleOnlineSend();
      } else {
        await handleOfflineSend();
      }
    } catch (e: unknown) {
      setSendError(e instanceof Error ? e.message : 'Error occurred');
    } finally {
      setSending(false);
    }
  };

  if (loading || !user) return null;

  if (success) {
    return (
      <div className="flex-1 flex items-center justify-center p-4 animate-fade-in">
        <div className="glass-panel p-8 max-w-md w-full text-center relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/10 rounded-full blur-2xl -translate-y-1/2 translate-x-1/2"></div>
          
          <div className="relative z-10">
            <div className="w-20 h-20 bg-emerald-500/10 border border-emerald-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-10 h-10 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            
            <h2 className="text-3xl font-bold mb-2 text-white">
              {success.status === 'QUEUED' ? 'Payment Queued' : 'Payment Sent!'}
            </h2>
            
            <p className="text-gray-400 mb-6">
              You {success.status === 'QUEUED' ? 'queued' : 'sent'} <span className="text-white font-bold">₹{success.amount.toFixed(2)}</span> to {success.counterpartyName} (@{success.counterpartyHandle})
            </p>
            
            <div className="bg-[#0a0b10]/80 p-4 rounded-xl text-sm text-gray-400 mb-8 space-y-3 text-left border border-white/5">
              <div className="flex justify-between border-b border-white/5 pb-2">
                <span>Reference</span>
                <span className="font-mono text-white">{success.publicReference.substring(0,16)}...</span>
              </div>
              <div className="flex justify-between border-b border-white/5 pb-2">
                <span>Mode</span>
                <span className="font-mono text-white">{success.mode || 'ONLINE'}</span>
              </div>
              <div className="flex justify-between">
                <span>Status</span>
                <span className="font-mono text-emerald-400 font-semibold">{success.status || 'SETTLED'}</span>
              </div>
            </div>
            
            <div className="flex gap-4">
              <button onClick={() => { setSuccess(null); setHandle(''); setAmount(''); setResolvedUser(null); }} className="btn-secondary flex-1">
                Send Another
              </button>
              <Link href="/dashboard" className="btn-primary flex-1">
                Done
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in pb-20 max-w-xl mx-auto w-full">
      <div className="flex items-center gap-4 mb-10">
        <Link href="/dashboard" className="p-3 bg-white/5 border border-white/10 rounded-full hover:bg-white/10 transition">
          <svg className="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </Link>
        <h1 className="text-3xl font-bold">Send Money</h1>
      </div>

      <div className="glass-panel p-2 mb-8 flex rounded-2xl relative">
        <button 
          className={`flex-1 py-3 text-center font-bold text-sm transition-all rounded-xl z-10 ${mode === 'ONLINE' ? 'text-white' : 'text-gray-500 hover:text-gray-300'}`}
          onClick={() => setMode('ONLINE')}
        >
          ONLINE
        </button>
        <button 
          className={`flex-1 py-3 text-center font-bold text-sm transition-all rounded-xl z-10 ${mode === 'OFFLINE' ? 'text-white' : 'text-gray-500 hover:text-gray-300'}`}
          onClick={() => setMode('OFFLINE')}
        >
          OFFLINE RELAY
        </button>
        <div 
          className="absolute top-2 bottom-2 w-[calc(50%-8px)] rounded-xl transition-all duration-300 ease-out"
          style={{
            background: mode === 'ONLINE' ? 'linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-secondary) 100%)' : 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            left: mode === 'ONLINE' ? '8px' : 'calc(50%)'
          }}
        ></div>
      </div>

      <div className="glass-panel p-8 relative overflow-hidden">
        {mode === 'OFFLINE' && (
          <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-500/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none"></div>
        )}
        {mode === 'ONLINE' && (
          <div className="absolute top-0 right-0 w-64 h-64 bg-blue-500/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none"></div>
        )}

        <div className="relative z-10">
          {mode === 'OFFLINE' && (
            <div className="bg-emerald-500/10 border border-emerald-500/20 p-4 rounded-xl mb-8 flex gap-3">
              <svg className="w-6 h-6 text-emerald-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              <p className="text-emerald-400/90 text-sm leading-relaxed">
                <strong>Offline Relay Mode:</strong> The payment will be cryptographically signed locally and queued against your Offline Reserved Balance. It will be settled later via the mesh network.
              </p>
            </div>
          )}

          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-400 mb-2">Receiver&apos;s Handle</label>
              <div className="flex gap-3">
                <div className="relative flex-1">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <span className="text-gray-500 font-medium">@</span>
                  </div>
                  <input
                    type="text"
                    value={handle}
                    onChange={e => {setHandle(e.target.value); setResolvedUser(null);}}
                    placeholder="payvia_handle"
                    className="w-full bg-[#0a0b10] border border-white/10 rounded-xl py-3 pl-10 pr-4 focus:outline-none focus:border-blue-500 transition font-medium text-white"
                  />
                </div>
                <button
                  onClick={resolveHandle}
                  disabled={resolving || !handle || resolvedUser !== null}
                  className="btn-secondary disabled:opacity-50 !py-3"
                >
                  {resolving ? 'Searching...' : 'Find User'}
                </button>
              </div>
              {resolveError && <p className="text-red-400 text-sm mt-2 flex items-center gap-1"><svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg> {resolveError}</p>}
            </div>

            {resolvedUser && (
              <div className="bg-blue-500/5 border border-blue-500/20 p-4 rounded-xl flex items-center justify-between animate-fade-in">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center font-bold text-xl text-white shadow-lg shadow-blue-500/30">
                    {resolvedUser.fullName.charAt(0)}
                  </div>
                  <div>
                    <div className="font-semibold text-white">{resolvedUser.fullName}</div>
                    <div className="text-blue-400 text-sm font-mono">@{resolvedUser.payviaHandle}</div>
                  </div>
                </div>
                <div className="w-8 h-8 rounded-full bg-green-500/20 text-green-400 flex items-center justify-center">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                </div>
              </div>
            )}

            {resolvedUser && (
              <div className="animate-fade-in">
                <label className="block text-sm font-medium text-gray-400 mb-2">Amount</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <span className="text-gray-400 font-medium text-xl">₹</span>
                  </div>
                  <input
                    type="number"
                    value={amount}
                    onChange={e => setAmount(e.target.value)}
                    placeholder="0.00"
                    step="0.01"
                    className="w-full bg-[#0a0b10] border border-white/10 rounded-xl py-4 pl-10 pr-4 focus:outline-none focus:border-blue-500 transition text-3xl font-bold text-white tracking-tighter"
                  />
                </div>
              </div>
            )}

            {sendError && (
              <div className="bg-red-500/10 border border-red-500/20 p-4 rounded-xl text-red-400 text-sm animate-fade-in flex items-center gap-2">
                <svg className="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                {sendError}
              </div>
            )}

            {resolvedUser && (
              <button
                onClick={handleSend}
                disabled={sending || !amount || parseFloat(amount) <= 0}
                className={`w-full py-4 text-lg font-bold rounded-xl mt-4 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed ${mode === 'ONLINE' ? 'btn-primary' : 'bg-emerald-600 hover:bg-emerald-700 text-white'}`}
              >
                {sending ? 'Processing...' : (mode === 'OFFLINE' ? 'Queue Offline Payment' : 'Send Money')}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
