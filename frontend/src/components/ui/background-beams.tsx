"use client";
import { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

export const BackgroundBeams = ({ className }: { className?: string }) => {
    const beams = [
        {
            initialX: 10,
            translateX: 10,
            duration: 7,
            repeatDelay: 3,
            delay: 2,
        },
        {
            initialX: 600,
            translateX: 600,
            duration: 3,
            repeatDelay: 3,
            delay: 4,
        },
        {
            initialX: 100,
            translateX: 100,
            duration: 7,
            repeatDelay: 7,
            className: "h-6",
        },
        {
            initialX: 400,
            translateX: 400,
            duration: 5,
            repeatDelay: 14,
            delay: 4,
        },
        {
            initialX: 800,
            translateX: 800,
            duration: 11,
            repeatDelay: 2,
            className: "h-20",
        },
        {
            initialX: 1000,
            translateX: 1000,
            duration: 4,
            repeatDelay: 2,
            className: "h-12",
        },
        {
            initialX: 1200,
            translateX: 1200,
            duration: 6,
            repeatDelay: 4,
            delay: 2,
            className: "h-6",
        },
    ];

    return (
        <div
            className={cn(
                "absolute inset-0 flex items-center justify-center bg-transparent overflow-hidden",
                className
            )}
        >
            <svg
                className="absolute inset-0 h-full w-full pointer-events-none"
                width="100%"
                height="100%"
                xmlns="http://www.w3.org/2000/svg"
            >
                <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stopColor="#FF9933" stopOpacity="0" />
                        <stop offset="50%" stopColor="#FF9933" stopOpacity="0.3" />
                        <stop offset="100%" stopColor="#FF9933" stopOpacity="0" />
                    </linearGradient>
                </defs>
                {beams.map((beam, index) => (
                    <motion.rect
                        key={`beam-${index}`}
                        x={beam.initialX}
                        y="0"
                        width="2"
                        height="100%"
                        fill="url(#gradient)"
                        initial={{ x: beam.initialX, opacity: 0 }}
                        animate={{
                            x: beam.translateX,
                            opacity: [0, 1, 1, 0],
                        }}
                        transition={{
                            duration: beam.duration || 7,
                            repeat: Infinity,
                            repeatDelay: beam.repeatDelay || 0,
                            delay: beam.delay || 0,
                            ease: "linear",
                        }}
                        className={beam.className}
                    />
                ))}
            </svg>
        </div>
    );
};
