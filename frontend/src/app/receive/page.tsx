'use client';

import { useAuth } from '@/context/AuthContext';
import Link from 'next/link';
import { QRCodeSVG } from 'qrcode.react';
import { useState } from 'react';

export default function ReceivePage() {
  const { user, loading } = useAuth();
  const [copied, setCopied] = useState(false);

  if (loading || !user) {
    return (
      <div className="flex-1 flex justify-center items-center">
        <div className="w-8 h-8 rounded-full border-2 border-blue-500 border-t-transparent animate-spin"></div>
      </div>
    );
  }

  const qrData = JSON.stringify({
    type: 'PAYVIA_ID',
    version: 1,
    handle: user.payviaHandle
  });

  const handleCopy = () => {
    navigator.clipboard.writeText(user.payviaHandle);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="animate-fade-in pb-20 w-full flex justify-center items-center min-h-[70vh]">
      <div className="max-w-md w-full">
        
        <div className="flex items-center gap-4 mb-8">
          <Link href="/dashboard" className="p-3 bg-white/5 border border-white/10 rounded-full hover:bg-white/10 transition">
            <svg className="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </Link>
          <h1 className="text-3xl font-bold">Receive Money</h1>
        </div>

        <div className="glass-panel p-8 flex flex-col items-center text-center relative overflow-hidden group shadow-2xl">
          <div className="absolute top-0 right-0 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
          <div className="absolute bottom-0 left-0 w-64 h-64 bg-emerald-500/10 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2"></div>
          
          <div className="relative z-10 w-full flex flex-col items-center">
            
            <div className="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center font-bold text-2xl text-white mb-4">
              {user.fullName.charAt(0)}
            </div>

            <div>
              <p className="text-gray-400 font-medium uppercase tracking-wider text-xs mb-1">Scan to Pay</p>
              <h2 className="text-3xl font-bold text-white mb-8">{user.fullName}</h2>
            </div>

            <div className="p-4 bg-white/95 rounded-2xl shadow-xl mb-8 transform group-hover:scale-105 transition-transform duration-500">
              <QRCodeSVG value={qrData} size={220} level="M" fgColor="#0a0b10" />
            </div>

            <div className="w-full">
              <p className="text-gray-400 text-sm mb-2 text-left ml-1">Your Payvia Handle</p>
              <div className="flex items-center bg-[#0a0b10] rounded-xl border border-white/10 overflow-hidden shadow-inner">
                <div className="pl-4 flex items-center text-gray-500 font-medium">@</div>
                <div className="px-1 py-4 flex-1 text-left font-mono font-medium text-white truncate text-lg">
                  {user.payviaHandle}
                </div>
                <button 
                  onClick={handleCopy}
                  className="px-6 py-4 bg-white/5 hover:bg-white/10 text-blue-400 font-semibold transition border-l border-white/5 h-full flex items-center justify-center"
                >
                  {copied ? 'Copied!' : 'Copy'}
                </button>
              </div>
            </div>
            
          </div>
        </div>
      </div>
    </div>
  );
}
