import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { FloatingNav } from "@/components/layout/FloatingNav";
import { Footer } from "@/components/layout/Footer";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: "IndoDB - India's Game Price Intelligence",
  description: "Find the best deals on PC games across Steam, Epic, and GOG. Track prices, compare stores, and get notified about discounts in India.",
  keywords: ["game deals", "steam sale", "epic games", "gog", "india gaming", "PC games", "price tracker"],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body className={`${inter.variable} font-sans antialiased`}>
        <FloatingNav />
        <main>{children}</main>
        <Footer />
      </body>
    </html>
  );
}
