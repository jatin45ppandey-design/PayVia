"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import { useAuth } from "@/context/AuthContext";
import { fetchApi } from '@/lib/api';

type Node = {
  id: string;
  nodeName: string;
  nodeType: string;
  active: boolean;
  hasInternet: boolean;
  xPosition: number;
  yPosition: number;
  storedPacketsCount: number;
};

type Edge = {
  id: string;
  sourceNodeId: string;
  targetNodeId: string;
};

type RelayEvent = {
  id: string;
  packetId: string;
  fromNodeId: string | null;
  toNodeId: string | null;
  eventType: string;
  hopNumber: number;
};

export default function NetworkPage() {
  const { } = useAuth();
  const [nodes, setNodes] = useState<Node[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [events, setEvents] = useState<RelayEvent[]>([]);
  const [activePackets, setActivePackets] = useState(0);
  const [autoRun, setAutoRun] = useState(false);
  
  const autoRunRef = useRef(autoRun);
  useEffect(() => {
    autoRunRef.current = autoRun;
  }, [autoRun]);

  const fetchNetwork = useCallback(async () => {
    try {
      const data = await fetchApi("/api/relay/network");
      setNodes(data.nodes);
      setEdges(data.edges);
    } catch (e) {
      console.error(e);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchNetwork();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const resetNetwork = async () => {
    setAutoRun(false);
    try {
      await fetchApi("/api/relay/network/reset", { method: "POST" });
    } catch (e) {
      console.error(e);
    }
    setEvents([]);
    fetchNetwork();
  };

  const nextStep = useCallback(async () => {
    try {
      const data = await fetchApi("/api/relay/simulation/step", { method: "POST" });
      setEvents(data.events);
      setActivePackets(data.activePacketsRemaining);
      fetchNetwork(); 
    } catch (e) {
      console.error(e);
    }
  }, [fetchNetwork]);

  useEffect(() => {
    if (!autoRun) return;
    
    const interval = setInterval(() => {
      if (autoRunRef.current) {
        nextStep();
      }
    }, 2000);
    
    return () => clearInterval(interval);
  }, [autoRun, nextStep]);

  const toggleNode = async (id: string) => {
    try {
      await fetchApi(`/api/relay/nodes/${id}/toggle`, { method: "POST" });
      fetchNetwork();
    } catch (e) {
      console.error(e);
    }
  };

  const toggleInternet = async (id: string) => {
    try {
      await fetchApi(`/api/relay/nodes/${id}/internet`, { method: "POST" });
      fetchNetwork();
    } catch (e) {
      console.error(e);
    }
  };

  const flushBridgeQueue = async () => {
    try {
      const data = await fetchApi("/api/relay/bridge/flush", { method: "POST" });
      alert(`Flushed ${data.length} packets. Outcomes:\n${data.map((r: { packetId: string; outcome: string }) => `${r.packetId.substring(0,8)}: ${r.outcome}`).join('\n')}`);
      fetchNetwork();
    } catch (e) {
      console.error(e);
      alert("Error flushing bridge");
    }
  };

  const injectFailure = async () => {
    try {
      await fetchApi("/api/relay/testing/inject-failure", { method: "POST" });
      alert("Next settlement attempt will simulate a retryable failure!");
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="animate-fade-in w-full h-[calc(100vh-100px)] flex flex-col pb-4">
      
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-3">
            <svg className="w-8 h-8 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
            </svg>
            Live Mesh Network
          </h1>
          <p className="text-gray-400 mt-1">Simulate store-and-forward offline relay protocols.</p>
        </div>
        <div className="flex gap-3">
          <button onClick={resetNetwork} className="btn-secondary text-sm !py-2 !px-4 text-red-400 hover:text-red-300 hover:border-red-500/50">Reset</button>
          <button onClick={injectFailure} className="btn-secondary text-sm !py-2 !px-4 text-purple-400 hover:text-purple-300 hover:border-purple-500/50">Simulate Failure</button>
          <button onClick={flushBridgeQueue} className="btn-secondary text-sm !py-2 !px-4 text-emerald-400 hover:text-emerald-300 hover:border-emerald-500/50">Flush Bridge</button>
          <button onClick={nextStep} className="btn-secondary text-sm !py-2 !px-4 hover:bg-blue-500/10 hover:border-blue-500/50">Next Step</button>
          <button 
            onClick={() => setAutoRun(!autoRun)} 
            className={`text-sm px-4 py-2 rounded-full font-medium transition-all ${autoRun ? 'bg-amber-500 text-black' : 'bg-blue-600 text-white hover:bg-blue-500'}`}>
            {autoRun ? 'Stop Simulation' : 'Auto Run'}
          </button>
        </div>
      </div>

      <div className="flex gap-6 flex-grow h-full min-h-[500px]">
        {/* Graph Viewer */}
        <div className="w-3/4 glass-panel relative overflow-hidden flex-grow h-full">
          
          <svg className="absolute inset-0 w-full h-full pointer-events-none">
            {edges.map(e => {
              const s = nodes.find(n => n.id === e.sourceNodeId);
              const t = nodes.find(n => n.id === e.targetNodeId);
              if (!s || !t) return null;
              return (
                <line key={e.id} x1={s.xPosition} y1={s.yPosition} x2={t.xPosition} y2={t.yPosition} 
                      stroke="rgba(255,255,255,0.15)" strokeWidth="2" strokeDasharray="4,4" />
              );
            })}
            
            {/* Animations */}
            {events.map((ev, i) => {
              if (ev.eventType === 'FORWARDED' || ev.eventType === 'BRIDGE_REACHED') {
                const s = nodes.find(n => n.id === ev.fromNodeId);
                const t = nodes.find(n => n.id === ev.toNodeId);
                if (!s || !t) return null;
                return (
                  <circle key={i} r="3" fill="#64748B">
                    <animateMotion 
                      path={`M ${s.xPosition},${s.yPosition} L ${t.xPosition},${t.yPosition}`}
                      dur="1.5s" fill="freeze" />
                  </circle>
                );
              }
              return null;
            })}
          </svg>

          {nodes.map(n => {
            let bgColor = 'bg-gray-800';
            let borderColor = 'border-gray-700';
            
            if (n.active) {
              if (n.nodeType === 'BRIDGE') {
                bgColor = 'bg-[#1e1b4b]';
                borderColor = 'border-indigo-500/50';
              } else if (n.nodeType === 'SENDER') {
                bgColor = 'bg-[#0f172a]';
                borderColor = 'border-blue-500/50';
              } else {
                bgColor = 'bg-[#064e3b]';
                borderColor = 'border-emerald-500/50';
              }
            }

            return (
              <div key={n.id} 
                   className={`absolute w-12 h-12 -ml-6 -mt-6 rounded-full flex flex-col items-center justify-center cursor-pointer transition-all border ${bgColor} ${borderColor} z-10`}
                   style={{ left: n.xPosition, top: n.yPosition }}
                   onClick={() => toggleNode(n.id)}>
                <span className="text-gray-300 text-[10px] font-medium truncate max-w-[40px]">{n.nodeName}</span>
                
                {n.storedPacketsCount > 0 && (
                  <span className="absolute -top-2 -right-2 bg-rose-500 text-white text-[10px] w-5 h-5 rounded-full flex items-center justify-center font-bold border border-white/20">
                    {n.storedPacketsCount}
                  </span>
                )}
                
                {n.nodeType === 'BRIDGE' && (
                  <button 
                    onClick={(e) => { e.stopPropagation(); toggleInternet(n.id); }}
                    className={`absolute -bottom-6 text-[9px] px-2 py-0.5 rounded-full whitespace-nowrap font-bold tracking-wider transition-colors border border-white/20 ${n.hasInternet ? 'bg-emerald-500/80 text-white' : 'bg-red-500/80 text-white'}`}>
                    {n.hasInternet ? 'ONLINE' : 'OFFLINE'}
                  </button>
                )}
              </div>
            );
          })}
        </div>

        {/* Event Logs */}
        <div className="w-1/4 glass-panel flex flex-col h-full overflow-hidden">
          <div className="p-4 border-b border-white/10 bg-white/5">
            <h3 className="font-semibold text-white flex items-center gap-2">
              <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
              </svg>
              Simulation Logs
            </h3>
            <p className="text-xs text-gray-400 mt-1">Active Packets: {activePackets}</p>
          </div>
          
          <div className="p-4 overflow-y-auto flex-grow space-y-3 custom-scrollbar">
            {events.length === 0 ? (
              <p className="text-sm text-gray-500 italic text-center mt-10">No events in this step.</p>
            ) : (
              events.map((e, i) => {
                let eventColor = 'text-blue-400';
                let bgTint = 'bg-blue-500/5 border-blue-500/20';
                
                if (e.eventType === 'EXPIRED' || e.eventType === 'DROPPED') {
                  eventColor = 'text-red-400';
                  bgTint = 'bg-red-500/5 border-red-500/20';
                } else if (e.eventType === 'BRIDGE_REACHED') {
                  eventColor = 'text-purple-400';
                  bgTint = 'bg-purple-500/5 border-purple-500/20';
                } else if (e.eventType === 'STORED') {
                  eventColor = 'text-emerald-400';
                  bgTint = 'bg-emerald-500/5 border-emerald-500/20';
                }

                return (
                  <div key={i} className={`text-xs p-3 rounded-xl border flex flex-col gap-1.5 transition-all animate-fade-in ${bgTint}`}>
                    <div className="flex justify-between items-center">
                      <span className="font-mono text-gray-400 bg-black/20 px-1.5 py-0.5 rounded">{e.packetId.substring(0,8)}</span>
                      <span className={`font-bold tracking-wider text-[10px] uppercase ${eventColor}`}>
                        {e.eventType}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center mt-1">
                      {e.fromNodeId && e.toNodeId ? (
                        <span className="text-gray-300 flex items-center gap-1.5">
                          <span className="truncate max-w-[60px]">{nodes.find(n => n.id === e.fromNodeId)?.nodeName}</span>
                          <svg className="w-3 h-3 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
                          <span className="truncate max-w-[60px]">{nodes.find(n => n.id === e.toNodeId)?.nodeName}</span>
                        </span>
                      ) : (
                        <span className="text-gray-500 italic">No movement</span>
                      )}
                      <span className="text-gray-500 text-[10px]">Hop {e.hopNumber}</span>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
