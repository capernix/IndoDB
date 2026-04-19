"use client";
import React from "react";

export const ShimmerButton = ({ text }: { text: string }) => {
    return (
        <button className="relative inline-flex h-10 overflow-hidden rounded-full p-[1px] focus:outline-none focus:ring-2 focus:ring-[#FF9933] focus:ring-offset-2 focus:ring-offset-[#080A0C] transition-all hover:scale-105 active:scale-95">
            {/* The Animated Gradient Layer */}
            <span className="absolute inset-[-1000%] animate-[spin_2s_linear_infinite] bg-[conic-gradient(from_90deg_at_50%_50%,#FF9933_0%,#080A0C_50%,#FF9933_100%)]" />

            {/* The Button Body */}
            <span className="inline-flex h-full w-full cursor-pointer items-center justify-center rounded-full bg-[#080A0C] px-5 py-1 text-sm font-medium text-white backdrop-blur-3xl">
                {text}
            </span>
        </button>
    );
};
