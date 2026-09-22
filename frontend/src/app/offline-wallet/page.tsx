'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { fetchApi } from '@/lib/api';

interface Wallet {
  availableBalance: number;
  offlineReservedBalance: number;
  totalBalance: number;
  currency: string;
}

export default function OfflineWalletPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [reserveAmount, setReserveAmount] = useState('');
  const [releaseAmount, setReleaseAmount] = useState('');
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!loading && !user) {
      router.push('/login');
    } else if (user) {
      const load = async () => {
        try {
          const res = await fetchApi('/api/wallet');
          if (res.ok) {
            setWallet(await res.json());
          }
        } catch (err) {
          console.error('Failed to load wallet', err);
        }
      };
      load();
    }
  }, [user, loading, router]);

  const handleReserve = async () => {
    setError('');
    setSuccess('');
    setProcessing(true);
    try {
      const res = await fetchApi('/api/wallet/offline/reserve', {
        method: 'POST',
        body: JSON.stringify({ amount: parseFloat(reserveAmount) })
      });
      if (res.ok) {
        setWallet(await res.json());
        setReserveAmount('');
        setSuccess('Successfully reserved funds for offline use.');
      } else {
        const data = await res.json();
        setError(data.message || 'Reserve failed');
      }
    } catch {
      setError('Reserve failed');
    } finally {
      setProcessing(false);
    }
  };

  const handleRelease = async () => {
    setError('');
    setSuccess('');
    setProcessing(true);
    try {
      const res = await fetchApi('/api/wallet/offline/release', {
        method: 'POST',
        body: JSON.stringify({ amount: parseFloat(releaseAmount) })
      });
      if (res.ok) {
        setWallet(await res.json());
        setReleaseAmount('');
        setSuccess('Successfully released offline funds to online balance.');
      } else {
        const data = await res.json();
        setError(data.message || 'Release failed');
      }
    } catch {
      setError('Release failed');
    } finally {
      setProcessing(false);
    }
  };

  if (loading || !user) return (
    <div className="flex-1 flex justify-center items-center">
      <div className="w-8 h-8 rounded-full border-2 border-purple-500 border-t-transparent animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-fade-in pb-20 max-w-4xl mx-auto w-full">
      <div className="mb-10 text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-purple-500/10 text-purple-400 mb-4">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z"/>
          </svg>
        </div>
        <h1 className="text-4xl font-bold mb-4">Offline Wallet</h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Manage your offline funds. Reserve money here to make payments when you don&apos;t have internet access via the Payvia Mesh Network.
        </p>
      </div>

      {error && (
        <div className="mb-8 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-center">
          {error}
        </div>
      )}
      
      {success && (
        <div className="mb-8 p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-center">
          {success}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        
        {/* Reserve Panel */}
        <div className="glass-panel p-8 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/10 rounded-full blur-2xl -translate-y-1/2 translate-x-1/2"></div>
          
          <div className="relative z-10 flex flex-col h-full">
            <h2 className="text-2xl font-bold mb-2 text-white">Reserve Funds</h2>
            <p className="text-gray-400 text-sm mb-6 flex-grow">
              Move funds from your online balance to your offline wallet. Available online: 
              <span className="text-white ml-1 font-medium">₹{wallet?.availableBalance?.toFixed(2) || '0.00'}</span>
            </p>
            
            <div className="space-y-4">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <span className="text-gray-500 font-medium">₹</span>
                </div>
                <input 
                  type="number" 
                  value={reserveAmount}
                  onChange={e => setReserveAmount(e.target.value)}
                  placeholder="0.00"
                  className="w-full bg-[#0a0b10] border border-white/10 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:border-blue-500 transition font-medium"
                />
              </div>
              
              <button 
                onClick={handleReserve}
                disabled={processing || !reserveAmount || parseFloat(reserveAmount) <= 0}
                className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {processing ? 'Processing...' : 'Reserve for Offline Use'}
              </button>
            </div>
          </div>
        </div>

        {/* Release Panel */}
        <div className="glass-panel p-8 relative overflow-hidden group">
          <div className="absolute bottom-0 left-0 w-32 h-32 bg-purple-500/10 rounded-full blur-2xl translate-y-1/2 -translate-x-1/2"></div>
          
          <div className="relative z-10 flex flex-col h-full">
            <h2 className="text-2xl font-bold mb-2 text-white">Release Funds</h2>
            <p className="text-gray-400 text-sm mb-6 flex-grow">
              Move funds back to your main online wallet from your offline reserve. Currently reserved:
              <span className="text-white ml-1 font-medium">₹{wallet?.offlineReservedBalance?.toFixed(2) || '0.00'}</span>
            </p>
            
            <div className="space-y-4">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <span className="text-gray-500 font-medium">₹</span>
                </div>
                <input 
                  type="number" 
                  value={releaseAmount}
                  onChange={e => setReleaseAmount(e.target.value)}
                  placeholder="0.00"
                  className="w-full bg-[#0a0b10] border border-white/10 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:border-purple-500 transition font-medium"
                />
              </div>
              
              <button 
                onClick={handleRelease}
                disabled={processing || !releaseAmount || parseFloat(releaseAmount) <= 0}
                className="w-full btn-secondary disabled:opacity-50 disabled:cursor-not-allowed hover:bg-purple-500/10 hover:border-purple-500/30 hover:text-purple-400"
              >
                {processing ? 'Processing...' : 'Release to Online Wallet'}
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
