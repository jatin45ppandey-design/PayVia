'use client';

import { useEffect, useState } from 'react';
import { fetchApi } from '@/lib/api';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';

interface Transaction {
  id: string;
  publicReference: string;
  counterpartyName: string;
  counterpartyHandle: string;
  type: string;
  amount: number;
  currency: string;
  mode: string;
  status: string;
  date: string;
}

export default function TransactionsPage() {
  const { user, loading } = useAuth();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loadingTx, setLoadingTx] = useState(true);

  useEffect(() => {
    if (user) {
      fetchApi('/api/payments/history')
        .then(res => res.json())
        .then((data) => setTransactions(data))
        .catch(console.error)
        .finally(() => setLoadingTx(false));
    }
  }, [user]);

  if (loading || (!user && loading)) {
    return (
      <div className="flex-1 flex justify-center items-center">
        <div className="w-8 h-8 rounded-full border-2 border-blue-500 border-t-transparent animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in pb-20 w-full max-w-5xl mx-auto">
      <div className="flex items-center gap-4 mb-10">
        <Link href="/dashboard" className="p-3 bg-white/5 border border-white/10 rounded-full hover:bg-white/10 transition">
          <svg className="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </Link>
        <h1 className="text-3xl font-bold">Transaction History</h1>
      </div>

      <div className="glass-panel overflow-hidden">
        {loadingTx ? (
          <div className="p-12 text-center text-gray-400">Loading transactions...</div>
        ) : transactions.length === 0 ? (
          <div className="p-12 text-center text-gray-400 flex flex-col items-center justify-center">
            <svg className="w-12 h-12 mb-4 text-white/20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p>No transactions found.</p>
          </div>
        ) : (
          <ul className="divide-y divide-white/5">
            {transactions.map((tx) => (
              <li key={tx.publicReference} className="p-6 hover:bg-white/[0.02] transition cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between group relative overflow-hidden">
                <div className="flex items-center space-x-5 relative z-10">
                  <div className={`flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center shadow-lg ${
                    tx.type === 'SENT' ? 'bg-orange-500/10 text-orange-400 border border-orange-500/20' : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  }`}>
                    {tx.type === 'SENT' ? (
                       <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                         <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
                       </svg>
                    ) : (
                       <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                         <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                       </svg>
                    )}
                  </div>
                  <div>
                    <p className="font-semibold text-white text-lg flex items-center gap-2">
                      {tx.type === 'SENT' ? 'Sent to' : 'Received from'} {tx.counterpartyName}
                      <span className="text-sm font-normal text-gray-500">@{tx.counterpartyHandle}</span>
                    </p>
                    <p className="text-sm text-gray-400 mt-1">{new Date(tx.date).toLocaleString(undefined, {
                      year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                    })}</p>
                  </div>
                </div>
                
                <div className="mt-4 sm:mt-0 text-left sm:text-right relative z-10 flex flex-col items-end">
                  <p className={`text-2xl font-bold tracking-tight mb-2 ${
                    tx.type === 'SENT' ? 'text-white' : 'text-emerald-400'
                  }`}>
                    {tx.type === 'SENT' ? '-' : '+'}₹{tx.amount.toFixed(2)}
                  </p>
                  
                  <div className="flex items-center sm:justify-end gap-2">
                    <span className="text-xs font-semibold uppercase tracking-wider px-2 py-1 rounded-md bg-blue-500/10 text-blue-400 border border-blue-500/20">
                      {tx.mode}
                    </span>
                    <span className={`text-xs font-semibold uppercase tracking-wider px-2 py-1 rounded-md border ${
                      tx.status === 'SETTLED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 
                      tx.status === 'FAILED' ? 'bg-red-500/10 text-red-400 border-red-500/20' : 
                      'bg-yellow-500/10 text-yellow-400 border-yellow-500/20'
                    }`}>
                      {tx.status}
                    </span>
                  </div>
                  
                  <p className="text-xs text-gray-500 mt-2 font-mono opacity-0 group-hover:opacity-100 transition-opacity">
                    Ref: {tx.publicReference}
                  </p>
                  
                  {tx.status === 'QUEUED' && (
                    <div className="mt-3">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          fetchApi(`/api/payments/${tx.id}/relay`, { method: 'POST' })
                            .then(() => {
                              fetchApi('/api/payments/history')
                                .then(res => res.json())
                                .then((data) => setTransactions(data))
                                .catch(console.error);
                            });
                        }}
                        className="text-xs bg-emerald-600/20 hover:bg-emerald-600/40 text-emerald-400 px-4 py-1.5 rounded-full border border-emerald-500/30 transition font-semibold"
                      >
                        Start Mesh Relay
                      </button>
                    </div>
                  )}
                </div>

                {/* Hover gradient effect */}
                <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/[0.02] to-transparent -translate-x-[100%] group-hover:translate-x-[100%] transition-transform duration-1000 ease-in-out"></div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
