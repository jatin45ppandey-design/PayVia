'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { fetchApi } from '@/lib/api';

interface SystemMetrics {
  totalUsers: number;
  totalWallets: number;
  totalTransactions: number;
  totalSystemLiquidity: number;
  offlineReservedLiquidity: number;
  activeDevices: number;
}

interface NetworkMetrics {
  totalNodes: number;
  activeRelays: number;
  bridgeNodes: number;
  queuedPackets: number;
  inTransitPackets: number;
  droppedPackets: number;
}

export default function AdminDashboardPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);
  const [network, setNetwork] = useState<NetworkMetrics | null>(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  const loadAdminData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [metricsData, networkData] = await Promise.all([
        fetchApi('/api/admin/metrics'),
        fetchApi('/api/admin/network')
      ]);
      setMetrics(metricsData);
      setNetwork(networkData);
    } catch (err) {
      console.error(err);
      setError('An error occurred while fetching admin data.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!loading && !user) {
      router.push('/login');
      return;
    }

    if (user && user.role !== 'ROLE_ADMIN') {
      router.push('/dashboard');
      return;
    }

    if (user && user.role === 'ROLE_ADMIN') {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      loadAdminData();
    }
  }, [user, loading, router, loadAdminData]);

  if (loading || isLoading) return (
    <div className="flex-1 flex justify-center items-center min-h-[50vh]">
      <div className="w-8 h-8 rounded-full border-2 border-red-500 border-t-transparent animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-fade-in pb-20 w-full">
      <div className="mb-12 flex justify-between items-end border-b border-white/10 pb-6">
        <div>
          <div className="inline-block px-3 py-1 bg-red-500/20 text-red-400 text-xs font-bold uppercase tracking-wider rounded-full mb-3">
            Admin Access Granted
          </div>
          <h1 className="text-4xl font-bold">System Dashboard</h1>
          <p className="text-gray-400 mt-2">Monitor Payvia&apos;s global network and financial invariants.</p>
        </div>
        <button onClick={loadAdminData} className="btn-secondary text-sm px-4 py-2 flex items-center gap-2">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          Refresh
        </button>
      </div>

      {error && (
        <div className="mb-8 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-center">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        
        {/* Financial Metrics */}
        <div className="glass-panel p-8">
          <h2 className="text-xl font-bold mb-6 flex items-center gap-3">
            <svg className="w-5 h-5 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Financial Overview
          </h2>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Total System Liquidity</p>
              <p className="text-2xl font-bold text-white">₹{metrics?.totalSystemLiquidity?.toLocaleString(undefined, {minimumFractionDigits:2}) || '0.00'}</p>
            </div>
            
            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Offline Reserved</p>
              <p className="text-2xl font-bold text-purple-400">₹{metrics?.offlineReservedLiquidity?.toLocaleString(undefined, {minimumFractionDigits:2}) || '0.00'}</p>
            </div>
            
            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Total Users</p>
              <p className="text-2xl font-bold text-white">{metrics?.totalUsers || 0}</p>
            </div>

            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Total Wallets</p>
              <p className="text-2xl font-bold text-white">{metrics?.totalWallets || 0}</p>
            </div>
          </div>
        </div>

        {/* Network Metrics */}
        <div className="glass-panel p-8">
          <h2 className="text-xl font-bold mb-6 flex items-center gap-3">
            <svg className="w-5 h-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
            </svg>
            Mesh Network Status
          </h2>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Total Nodes</p>
              <p className="text-2xl font-bold text-blue-400">{network?.totalNodes || 0}</p>
            </div>
            
            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Bridge Nodes</p>
              <p className="text-2xl font-bold text-emerald-400">{network?.bridgeNodes || 0}</p>
            </div>
            
            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Packets in Transit</p>
              <p className="text-2xl font-bold text-yellow-400">{network?.inTransitPackets || 0}</p>
            </div>

            <div className="bg-[#0a0b10] border border-white/5 rounded-xl p-5">
              <p className="text-gray-500 text-sm font-medium mb-1">Dropped Packets</p>
              <p className="text-2xl font-bold text-red-400">{network?.droppedPackets || 0}</p>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
