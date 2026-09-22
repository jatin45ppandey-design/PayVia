'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { fetchApi } from '@/lib/api';
import Link from 'next/link';

interface Wallet {
  availableBalance: number;
  offlineReservedBalance: number;
  totalBalance: number;
  currency: string;
}

export default function DashboardPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [wallet, setWallet] = useState<Wallet | null>(null);

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
          console.error(err);
        }
      };
      load();
    }
  }, [user, loading, router]);

  if (loading || !user) return (
    <div className="flex-1 flex justify-center items-center">
      <div className="w-8 h-8 rounded-full border-2 border-blue-500 border-t-transparent animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-fade-in pb-20">
      
      {/* Header Section */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 mb-12">
        <div>
          <h1 className="text-4xl font-bold mb-2">
            Welcome back, <span className="text-gradient">{user.fullName.split(' ')[0]}</span>
          </h1>
          <p className="text-gray-400 font-mono text-sm">@{user.payviaHandle}</p>
        </div>
        <div className="flex gap-4">
          <Link href="/settings/devices" className="btn-secondary">
            Manage Devices
          </Link>
          <Link href="/transactions" className="btn-secondary">
            History
          </Link>
        </div>
      </div>

      {/* Main Balance Card */}
      <div className="glass-panel p-8 md:p-12 mb-10 relative overflow-hidden group">
        <div className="relative z-10">
          <p className="text-gray-400 font-medium tracking-wide text-sm uppercase mb-2">Total Balance</p>
          <h2 className="text-5xl md:text-6xl font-bold tracking-tight mb-10">
            <span className="text-gray-400 text-3xl mr-2">₹</span>
            {wallet?.totalBalance?.toLocaleString(undefined, {minimumFractionDigits: 2}) || '0.00'}
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-[#12141c] rounded-xl p-6 border border-white/5">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-1.5 h-1.5 rounded-full bg-emerald-500"></div>
                <p className="text-gray-400 text-sm font-medium">Online Available</p>
              </div>
              <p className="text-2xl font-semibold">₹{wallet?.availableBalance?.toLocaleString(undefined, {minimumFractionDigits: 2}) || '0.00'}</p>
            </div>
            
            <div className="bg-[#12141c] rounded-xl p-6 border border-white/5">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-1.5 h-1.5 rounded-full bg-blue-500"></div>
                <p className="text-gray-400 text-sm font-medium">Offline Reserved</p>
              </div>
              <p className="text-2xl font-semibold">₹{wallet?.offlineReservedBalance?.toLocaleString(undefined, {minimumFractionDigits: 2}) || '0.00'}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <h3 className="text-xl font-semibold mb-6">Quick Actions</h3>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        
        <Link href="/send" className="glass-panel p-6 flex flex-col items-center justify-center gap-4 hover:border-blue-500/30 hover:bg-blue-500/5 transition-all group">
          <div className="w-16 h-16 rounded-full bg-blue-500/10 flex items-center justify-center text-blue-400 group-hover:scale-110 group-hover:bg-blue-500 group-hover:text-white transition-all duration-300">
            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m0-16l-4 4m4-4l4 4"/>
            </svg>
          </div>
          <span className="font-medium text-lg">Send Money</span>
        </Link>
        
        <Link href="/receive" className="glass-panel p-6 flex flex-col items-center justify-center gap-4 hover:border-emerald-500/30 hover:bg-emerald-500/5 transition-all group">
          <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center text-emerald-400 group-hover:scale-110 group-hover:bg-emerald-500 group-hover:text-white transition-all duration-300">
            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 20V4m0 16l4-4m-4 4l-4-4"/>
            </svg>
          </div>
          <span className="font-medium text-lg">Receive Money</span>
        </Link>
        
        <Link href="/offline-wallet" className="glass-panel p-6 flex flex-col items-center justify-center gap-4 hover:border-purple-500/30 hover:bg-purple-500/5 transition-all group">
          <div className="w-16 h-16 rounded-full bg-purple-500/10 flex items-center justify-center text-purple-400 group-hover:scale-110 group-hover:bg-purple-500 group-hover:text-white transition-all duration-300">
            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z"/>
            </svg>
          </div>
          <span className="font-medium text-lg">Offline Wallet</span>
        </Link>

      </div>
    </div>
  );
}
