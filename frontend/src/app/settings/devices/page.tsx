'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { fetchApi } from '@/lib/api';
import { generateKeyPairAndExport, storeKeyPair, clearKeyPair } from '@/lib/crypto';
import Link from 'next/link';

interface Device {
  id: string;
  deviceName: string;
  deviceType: string;
  status: string;
  registeredAt: string;
}

export default function DevicesPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [devices, setDevices] = useState<Device[]>([]);
  const [registering, setRegistering] = useState(false);
  const [error, setError] = useState('');


  useEffect(() => {
    if (!loading && !user) {
      router.push('/login');
    } else if (user) {
      const load = async () => {
        try {
          const res = await fetchApi('/api/devices');
          if (res.ok) {
            setDevices(await res.json());
          }
        } catch (err) {
          console.error(err);
        }
      };
      load();
    }
  }, [user, loading, router]);

  const registerDevice = async () => {
    setRegistering(true);
    setError('');
    try {
      const { keyPair, publicKeyBase64 } = await generateKeyPairAndExport();
      
      const res = await fetchApi('/api/devices/register', {
        method: 'POST',
        body: JSON.stringify({
          deviceName: navigator.userAgent.substring(0, 50),
          publicKey: publicKeyBase64,
          keyAlgorithm: 'ECDSA-P256'
        })
      });

      if (res.ok) {
        const deviceData = await res.json();
        await storeKeyPair(deviceData.id, keyPair);
        const resDevices = await fetchApi('/api/devices');
        if (resDevices.ok) {
          setDevices(await resDevices.json());
        }
      } else {
        const data = await res.json();
        setError(data.message || 'Failed to register device');
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error registering device');
    } finally {
      setRegistering(false);
    }
  };

  const revokeDevice = async (id: string) => {
    try {
      const res = await fetchApi('/api/devices/' + id + '/revoke', { method: 'POST' });
      if (res.ok) {
        const resDevices = await fetchApi('/api/devices');
        if (resDevices.ok) {
          setDevices(await resDevices.json());
        }
        await clearKeyPair(id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading || !user) return <div className="min-h-screen bg-gray-900 flex items-center justify-center"><div className="animate-spin text-white">Loading...</div></div>;

  return (
    <div className="min-h-screen bg-gray-900 text-white p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-emerald-400">
            Registered Devices
          </h1>
          <Link href="/dashboard" className="px-4 py-2 bg-gray-800 rounded-xl hover:bg-gray-700 transition">
            Back to Dashboard
          </Link>
        </div>

        {error && <div className="bg-red-500/10 border border-red-500 text-red-500 p-4 rounded-xl mb-6">{error}</div>}

        <div className="bg-gray-800 rounded-2xl p-6 mb-8 border border-gray-700">
          <h2 className="text-xl font-semibold mb-4">Register This Browser</h2>
          <p className="text-gray-400 mb-6">
            Register this browser device to enable offline payments. This will generate a secure cryptographic key pair that never leaves your device.
          </p>
          <button
            onClick={registerDevice}
            disabled={registering}
            className="px-6 py-3 bg-blue-600 hover:bg-blue-700 rounded-xl font-semibold transition disabled:opacity-50"
          >
            {registering ? 'Registering...' : 'Register Device'}
          </button>
        </div>

        <div className="space-y-4">
          <h2 className="text-xl font-semibold mb-4">Your Devices</h2>
          {devices.length === 0 ? (
            <p className="text-gray-400">No devices registered.</p>
          ) : (
            devices.map(d => (
              <div key={d.id} className="bg-gray-800 p-6 rounded-2xl border border-gray-700 flex justify-between items-center">
                <div>
                  <div className="font-semibold">{d.deviceName}</div>
                  <div className="text-sm text-gray-400 mt-1">
                    Type: {d.deviceType} | Status: <span className={d.status === 'ACTIVE' ? 'text-emerald-400' : 'text-red-400'}>{d.status}</span>
                  </div>
                  <div className="text-xs text-gray-500 mt-1">Registered: {new Date(d.registeredAt).toLocaleString()}</div>
                </div>
                {d.status === 'ACTIVE' && (
                  <button
                    onClick={() => revokeDevice(d.id)}
                    className="px-4 py-2 bg-red-600/20 text-red-500 hover:bg-red-600/40 rounded-xl transition"
                  >
                    Revoke
                  </button>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
