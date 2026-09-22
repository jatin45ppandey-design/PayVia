'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useState, useEffect } from 'react';
import './Navigation.css';

export default function Navigation() {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 10);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <nav className={`navbar ${scrolled ? 'navbar-scrolled' : ''}`}>
      <div className="navbar-container">
        <Link href="/" className="navbar-logo">
          <div className="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" strokeWidth="2"/>
              <path d="M8 12L11 15L16 9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span className="logo-text">Payvia</span>
        </Link>

        <div className="navbar-links">
          {user ? (
            <>
              <Link href="/dashboard" className={`nav-link ${pathname === '/dashboard' ? 'active' : ''}`}>Dashboard</Link>
              <Link href="/send" className={`nav-link ${pathname === '/send' ? 'active' : ''}`}>Send</Link>
              <Link href="/receive" className={`nav-link ${pathname === '/receive' ? 'active' : ''}`}>Receive</Link>
              <Link href="/transactions" className={`nav-link ${pathname.startsWith('/transactions') ? 'active' : ''}`}>Transactions</Link>
              <Link href="/offline-wallet" className={`nav-link ${pathname === '/offline-wallet' ? 'active' : ''}`}>Offline</Link>
              <Link href="/network" className={`nav-link ${pathname === '/network' ? 'active' : ''}`}>Network</Link>
              <Link href="/admin" className={`nav-link ${pathname === '/admin' ? 'active' : ''}`}>Admin</Link>
              <div className="user-menu">
                <span className="user-handle">{user.payviaHandle}</span>
                <button onClick={logout} className="nav-btn-logout">Logout</button>
              </div>
            </>
          ) : (
            <>
              <Link href="/login" className="btn-secondary">Login</Link>
              <Link href="/register" className="btn-primary">Register</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
