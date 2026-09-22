'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';

export default function Home() {
  const [apiStatus, setApiStatus] = useState<'LOADING' | 'CONNECTED' | 'UNAVAILABLE'>('LOADING');

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
        const res = await fetch(`${apiUrl}/health`);
        if (res.ok) {
          const data = await res.json();
          if (data.status === 'UP') {
            setApiStatus('CONNECTED');
          } else {
            setApiStatus('UNAVAILABLE');
          }
        } else {
          setApiStatus('UNAVAILABLE');
        }
      } catch {
        setApiStatus('UNAVAILABLE');
      }
    };

    fetchHealth();
  }, []);

  return (
    <div className="flex flex-col items-center justify-center min-h-[85vh] text-center px-4 animate-fade-in relative z-10">
      <div className="max-w-4xl mx-auto flex flex-col items-center">
        
        {/* Status Badge */}
        <div className="mb-8 glass-panel px-4 py-2 rounded-full inline-flex items-center gap-2">
          {apiStatus === 'LOADING' && (
            <>
              <div className="w-2 h-2 rounded-full bg-yellow-500 animate-pulse"></div>
              <span className="text-sm font-medium text-gray-300">Connecting to Network...</span>
            </>
          )}
          {apiStatus === 'CONNECTED' && (
            <>
              <div className="w-2 h-2 rounded-full bg-green-500"></div>
              <span className="text-sm font-medium text-gray-300">Payvia Network Online</span>
            </>
          )}
          {apiStatus === 'UNAVAILABLE' && (
            <>
              <div className="w-2 h-2 rounded-full bg-red-500"></div>
              <span className="text-sm font-medium text-gray-300">Network Offline</span>
            </>
          )}
        </div>

        {/* Hero Section */}
        <h1 className="text-6xl md:text-8xl font-bold mb-6 tracking-tighter">
          Payments <br />
          <span className="text-gradient">Find a Way.</span>
        </h1>
        
        <p className="text-xl md:text-2xl text-gray-400 mb-10 max-w-2xl font-light">
          The world&apos;s first resilient payment network. Send and receive funds seamlessly, even when the internet goes dark.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 mb-16">
          <Link href="/register" className="btn-primary text-lg px-8 py-4">
            Join the Network
          </Link>
          <Link href="/network" className="btn-secondary text-lg px-8 py-4">
            View Live Topology
          </Link>
        </div>

        {/* Hero Image / Placeholder */}
        <div className="w-full max-w-5xl rounded-xl overflow-hidden glass-panel border-gray-800 shadow-sm relative flex items-center justify-center bg-[#0d0f14]" style={{ height: '400px' }}>
          <div className="text-center text-gray-500">
            <svg className="w-16 h-16 mx-auto mb-4 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
            </svg>
            <p className="text-sm font-medium tracking-wide uppercase">PayVia Core Architecture</p>
          </div>
        </div>

      </div>
    </div>
  );
}
