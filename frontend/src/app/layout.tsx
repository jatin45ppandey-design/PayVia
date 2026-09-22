import type { Metadata } from "next";
import { AuthProvider } from "@/context/AuthContext";
import Navigation from "@/components/Navigation";
import "./globals.css";

export const metadata: Metadata = {
  title: "Payvia",
  description: "Payments Find a Way.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="app-container">
        <AuthProvider>
          <Navigation />
          <div className="pt-24 px-4 w-full max-w-7xl mx-auto flex-grow flex flex-col">
            {children}
          </div>
        </AuthProvider>
      </body>
    </html>
  );
}