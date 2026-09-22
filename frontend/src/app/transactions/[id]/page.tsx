"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { fetchApi } from "@/lib/api";
import { useRouter } from "next/navigation";
import Link from "next/link";

type EventDto = {
  type: string;
  description: string;
  status: string;
  timestamp: string;
};

type TransactionDto = {
  id: string;
  type: string;
  counterpartyName: string;
  counterpartyHandle: string;
  amount: number;
  currency: string;
  date: string;
  mode: string;
  status: string;
  publicReference: string;
};

type TimelineDto = {
  transaction: TransactionDto;
  events: EventDto[];
};

export default function TransactionTimelinePage({ params }: { params: { id: string } }) {
  const { user } = useAuth();
  const router = useRouter();
  const [timeline, setTimeline] = useState<TimelineDto | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!user) {
      router.push("/login");
      return;
    }
    const load = async () => {
      try {
        const data = await fetchApi(`/payments/${params.id}/timeline`);
        setTimeline(data);
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Failed to load timeline");
      }
    };
    load();
  }, [user, router, params.id]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-red-500 font-semibold">{error}</div>
      </div>
    );
  }

  if (!timeline) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-pulse flex flex-col items-center">
          <div className="h-8 w-8 bg-blue-500 rounded-full mb-4"></div>
          <div className="text-gray-500">Loading timeline...</div>
        </div>
      </div>
    );
  }

  const { transaction, events } = timeline;

  const getStatusColor = (status: string) => {
    switch (status) {
      case "SUCCESS":
      case "SETTLED":
        return "bg-green-100 text-green-800 border-green-200";
      case "PROCESSING":
      case "RELAYING":
        return "bg-blue-100 text-blue-800 border-blue-200";
      case "FAILED_RETRYABLE":
      case "RETRYABLE_FAILURE":
        return "bg-amber-100 text-amber-800 border-amber-200";
      case "REJECTED":
      case "FAILED":
      case "INVALID_PACKET":
      case "EXPIRED":
        return "bg-red-100 text-red-800 border-red-200";
      case "QUEUED":
      case "CREATED":
      case "RECEIVED":
        return "bg-gray-100 text-gray-800 border-gray-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto space-y-6">
        
        <div className="flex items-center justify-between">
          <Link href="/dashboard" className="text-blue-600 hover:text-blue-800 font-medium flex items-center gap-2">
            ← Back to Dashboard
          </Link>
          <h1 className="text-2xl font-bold text-gray-900">Transaction Details</h1>
        </div>

        <div className="bg-white shadow rounded-2xl overflow-hidden border border-gray-100">
          <div className="p-6 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">Reference</p>
              <p className="font-mono text-gray-900">{transaction.publicReference}</p>
            </div>
            <div className="text-right">
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(transaction.status)}`}>
                {transaction.status}
              </span>
            </div>
          </div>
          <div className="p-6 flex flex-col md:flex-row justify-between gap-6">
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-500 mb-2">Amount</p>
              <p className={`text-4xl font-bold ${transaction.type === 'RECEIVED' ? 'text-green-600' : 'text-gray-900'}`}>
                {transaction.type === 'RECEIVED' ? '+' : '-'} {transaction.currency} {transaction.amount.toFixed(2)}
              </p>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-500 mb-1">{transaction.type === 'SENT' ? 'To' : 'From'}</p>
              <p className="text-lg font-semibold text-gray-900">{transaction.counterpartyName}</p>
              <p className="text-sm text-gray-500">{transaction.counterpartyHandle}</p>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-500 mb-1">Mode</p>
              <p className="text-gray-900 font-medium">{transaction.mode.replace('_', ' ')}</p>
              <p className="text-xs text-gray-400 mt-1">{new Date(transaction.date).toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="bg-white shadow rounded-2xl p-6 border border-gray-100">
          <h2 className="text-lg font-bold text-gray-900 mb-6">Execution Timeline</h2>
          
          <div className="relative border-l-2 border-gray-200 ml-4 space-y-8 pb-4">
            {events.map((event, index) => (
              <div key={index} className="relative pl-6">
                <div className={`absolute -left-[9px] top-1 w-4 h-4 rounded-full border-2 border-white ${event.status === 'SUCCESS' || event.status === 'SETTLED' ? 'bg-green-500' : event.status === 'REJECTED' || event.status === 'EXPIRED' ? 'bg-red-500' : event.status === 'RETRYABLE_FAILURE' ? 'bg-amber-500' : 'bg-blue-500'}`}></div>
                <div className="flex flex-col sm:flex-row sm:justify-between sm:items-baseline gap-1">
                  <h3 className="font-semibold text-gray-900">{event.type.replace(/_/g, ' ')}</h3>
                  <span className="text-xs text-gray-500 font-mono">{new Date(event.timestamp).toLocaleString()}</span>
                </div>
                <p className="text-sm text-gray-600 mt-1">{event.description}</p>
                {event.status !== 'SUCCESS' && (
                  <span className={`inline-block mt-2 px-2 py-0.5 rounded text-xs font-medium border ${getStatusColor(event.status)}`}>
                    {event.status}
                  </span>
                )}
              </div>
            ))}
          </div>

        </div>
      </div>
    </div>
  );
}
