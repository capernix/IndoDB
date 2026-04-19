"use client";

import React, { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import Link from "next/link";
import { LogOut, User } from "lucide-react";
import { getUser, clearAuth, isLoggedIn } from "@/lib/auth";
import type { AuthUser } from "@/lib/auth";

export const FloatingNav = ({ className }: { className?: string }) => {
    const navItems = [
        { name: "Home",     link: "/" },
        { name: "Deals",    link: "/deals" },
        { name: "Trending", link: "/trending" },
        { name: "Voting",   link: "/voting" },
        { name: "Import",   link: "/import" },
    ];

    const [user, setUser]       = useState<AuthUser | null>(null);
    const [menuOpen, setMenuOpen] = useState(false);

    useEffect(() => {
        if (isLoggedIn()) setUser(getUser());
    }, []);

    const handleLogout = () => {
        clearAuth();
        setUser(null);
        setMenuOpen(false);
        window.location.href = "/";
    };

    return (
        <nav className={cn("fixed top-0 inset-x-0 z-[5000] border-b border-white/10 bg-[#0a0a0a]/80 backdrop-blur-md", className)}>
            <div className="px-12 h-16 flex items-center">

                {/* Left: Logo + Nav */}
                <div className="flex items-center gap-8">
                    <Link href="/" className="flex items-center">
                        <span className="font-black text-white tracking-tight text-lg" style={{ letterSpacing: "-0.03em" }}>
                            Indo<span className="text-[#FF9933]">DB</span>
                        </span>
                    </Link>

                    <div className="hidden md:flex items-center gap-8">
                        {navItems.map((item) => (
                            <Link
                                key={item.name}
                                href={item.link}
                                className="text-sm font-medium text-neutral-400 hover:text-[#FF9933] transition-colors"
                            >
                                {item.name}
                            </Link>
                        ))}
                    </div>
                </div>

                <div className="flex-1" />

                {/* Right: Auth */}
                {user ? (
                    <div className="relative">
                        <button
                            onClick={() => setMenuOpen((v) => !v)}
                            className="flex items-center gap-2.5 rounded-full px-4 py-2 text-sm font-medium transition-colors hover:bg-white/5"
                            style={{ border: "1px solid rgba(255,255,255,0.1)", color: "#d4d4d4" }}
                        >
                            <div className="flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold text-black"
                                style={{ background: "#FF9933" }}>
                                {user.username[0].toUpperCase()}
                            </div>
                            {user.username}
                        </button>

                        {menuOpen && (
                            <div
                                className="absolute right-0 mt-2 rounded-xl py-2 w-48"
                                style={{ background: "#0d0f11", border: "1px solid rgba(255,255,255,0.08)", boxShadow: "0 16px 48px rgba(0,0,0,0.6)" }}
                            >
                                <Link href="/wishlist" onClick={() => setMenuOpen(false)}
                                    className="flex items-center gap-3 px-4 py-2.5 text-sm text-neutral-400 hover:text-white hover:bg-white/5 transition-colors">
                                    <User className="h-4 w-4" /> My Wishlist
                                </Link>
                                <button onClick={handleLogout}
                                    className="flex w-full items-center gap-3 px-4 py-2.5 text-sm text-neutral-400 hover:text-red-400 hover:bg-white/5 transition-colors">
                                    <LogOut className="h-4 w-4" /> Sign Out
                                </button>
                            </div>
                        )}
                    </div>
                ) : (
                    <Link
                        href="/login"
                        className="rounded-full px-5 py-2 text-sm font-semibold transition-colors hover:bg-[#FF9933] hover:text-black"
                        style={{ border: "1px solid rgba(255,255,255,0.15)", color: "#d4d4d4" }}
                    >
                        Sign In
                    </Link>
                )}
            </div>
        </nav>
    );
};
