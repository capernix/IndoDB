"use client";

import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Search, ArrowRight } from "lucide-react";
import { cn } from "@/lib/utils";

export function HeroSearch() {
    const [query, setQuery] = useState("");
    const [isFocused, setIsFocused] = useState(false);
    const inputRef = useRef<HTMLInputElement>(null);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (query.trim()) {
            // TODO: Navigate to search results
            console.log("Searching for:", query);
        }
    };

    return (
        <section className="relative min-h-screen flex flex-col items-center justify-center px-4">
            {/* Background gradient */}
            <div className="absolute inset-0 bg-gradient-to-b from-[#080A0C] via-[#0d1117] to-[#080A0C]" />

            {/* Subtle grid pattern */}
            <div
                className="absolute inset-0 opacity-[0.03]"
                style={{
                    backgroundImage: `radial-gradient(circle at 1px 1px, white 1px, transparent 0)`,
                    backgroundSize: '40px 40px',
                }}
            />

            {/* Content */}
            <div className="relative z-10 max-w-4xl mx-auto text-center">
                {/* Heading */}
                <motion.h1
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: 0.1 }}
                    className="text-5xl sm:text-6xl md:text-7xl font-bold mb-8"
                >
                    <span className="text-white">Find </span>
                    <span className="gradient-text">Game Deals</span>
                </motion.h1>

                {/* Subtitle - Simplified */}
                <motion.p
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                    className="text-xl text-zinc-400 mb-12 max-w-xl mx-auto font-light"
                >
                    Track prices across Steam, Epic, and GOG.
                </motion.p>

                {/* Search Bar - Redesigned */}
                <motion.form
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: 0.3 }}
                    onSubmit={handleSubmit}
                    className="relative max-w-2xl mx-auto w-full"
                >
                    <div
                        className={cn(
                            "relative flex items-center",
                            "glass-dark rounded-full", // Changed to glass-dark and rounded-full
                            "transition-all duration-300",
                            isFocused && "ring-1 ring-[#FF9933]/50 scale-[1.01]"
                        )}
                    >
                        <Search
                            size={22}
                            className="absolute left-6 text-zinc-500"
                        />
                        <input
                            ref={inputRef}
                            type="text"
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                            onFocus={() => setIsFocused(true)}
                            onBlur={() => setIsFocused(false)}
                            placeholder="Search games..."
                            className={cn(
                                "w-full py-4 pl-16 pr-20", // Adjusted padding
                                "bg-transparent text-white placeholder:text-zinc-600",
                                "text-lg outline-none",
                                "rounded-full font-medium"
                            )}
                        />
                        <button
                            type="submit"
                            className={cn(
                                "absolute right-2",
                                "flex items-center justify-center w-12 h-12 rounded-full", // Circular button
                                "bg-gradient-to-r from-[#FF9933] to-[#FF4D4D]",
                                "text-white shadow-lg shadow-orange-500/20",
                                "hover:opacity-90 hover:scale-105 transition-all",
                                "disabled:opacity-50 disabled:hover:scale-100"
                            )}
                            disabled={!query.trim()}
                        >
                            <ArrowRight size={20} />
                        </button>
                    </div>
                </motion.form>
            </div>
        </section>
    );
}
