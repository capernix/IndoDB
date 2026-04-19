"use client";
import React from "react";
import Link from "next/link";
import { ArrowRight } from "lucide-react";

export function IndoDBHero() {
    return (
        <div
            className="min-h-screen w-full relative flex flex-col justify-center pt-16 overflow-hidden"
            style={{
                background: `
                    radial-gradient(ellipse 70% 65% at 20% 30%, rgba(255, 153, 51, 0.11) 0%, transparent 70%),
                    radial-gradient(ellipse 55% 55% at 85% 10%, rgba(124, 58, 237, 0.08) 0%, transparent 65%),
                    #080A0C
                `,
            }}
        >
            <div className="max-w-7xl mx-auto w-full px-6 md:px-16 py-24">

                {/* Hero headline — Remix-style large, left-aligned, font-black */}
                <h1
                    className="font-black text-white mb-8"
                    style={{
                        fontSize: "clamp(56px, 7.5vw, 96px)",
                        lineHeight: 0.95,
                        letterSpacing: "-0.04em",
                    }}
                >
                    The smartest way<br />
                    to{" "}
                    <span className="text-[#FF9933]">buy games</span>
                    <br />
                    in India.
                </h1>

                {/* Subtitle */}
                <p
                    className="mb-10 text-[17px] font-normal leading-relaxed text-neutral-500"
                    style={{ maxWidth: "46ch" }}
                >
                    Real-time prices across Steam, Epic, and GOG —
                    tracked in INR. Never overpay for a game again.
                </p>



            </div>
        </div>
    );
}
