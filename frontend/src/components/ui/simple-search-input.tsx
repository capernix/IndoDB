"use client";

import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useRef, useState } from "react";
import { cn } from "@/lib/utils";
import { ArrowRight } from "lucide-react";

export function SimpleSearchInput({
    placeholders,
    onChange,
    onSubmit,
}: {
    placeholders: string[];
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
}) {
    const [currentPlaceholder, setCurrentPlaceholder] = useState(0);
    const [value, setValue] = useState("");
    const intervalRef = useRef<NodeJS.Timeout | null>(null);

    const startAnimation = () => {
        intervalRef.current = setInterval(() => {
            setCurrentPlaceholder((prev) => (prev + 1) % placeholders.length);
        }, 3000);
    };

    const handleVisibilityChange = () => {
        if (document.visibilityState !== "visible" && intervalRef.current) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
        } else if (document.visibilityState === "visible") {
            startAnimation();
        }
    };

    useEffect(() => {
        startAnimation();
        document.addEventListener("visibilitychange", handleVisibilityChange);

        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
            }
            document.removeEventListener("visibilitychange", handleVisibilityChange);
        };
    }, [placeholders]);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        onSubmit && onSubmit(e);
    };

    const [isFocused, setIsFocused] = useState(false);

    return (
        <form
            className={cn(
                "w-full relative max-w-2xl mx-auto rounded-full overflow-hidden transition-all duration-300",
                "bg-white/5 backdrop-blur-lg border border-white/10 shadow-2xl",
                isFocused && "ring-2 ring-[#FF9933]/50 shadow-[0_0_30px_rgba(255,153,51,0.3)]"
            )}
            onSubmit={handleSubmit}
        >
            <input
                onChange={(e) => {
                    setValue(e.target.value);
                    onChange && onChange(e);
                }}
                onFocus={() => setIsFocused(true)}
                onBlur={() => setIsFocused(false)}
                value={value}
                type="text"
                style={{ textIndent: '1rem' }}
                className={cn(
                    "w-full relative text-sm sm:text-base z-50 border-none text-white bg-transparent h-14 rounded-full focus:outline-none focus:ring-0 pl-10 sm:pl-16 pr-20"
                )}
            />

            <button
                disabled={!value}
                type="submit"
                className="absolute right-2 top-1/2 z-50 -translate-y-1/2 h-10 w-10 rounded-full disabled:bg-gray-100 bg-gradient-to-r from-[#FF9933] to-[#FF4D4D] transition duration-200 flex items-center justify-center disabled:opacity-50 hover:scale-110 hover:shadow-lg hover:shadow-orange-500/50"
            >
                <ArrowRight className="text-white h-4 w-4" />
            </button>

            <div className="absolute inset-0 flex items-center rounded-full pointer-events-none">
                <AnimatePresence mode="wait">
                    {!value && (
                        <motion.p
                            initial={{
                                y: 5,
                                opacity: 0,
                            }}
                            key={`current-placeholder-${currentPlaceholder}`}
                            animate={{
                                y: 0,
                                opacity: 1,
                            }}
                            exit={{
                                y: -15,
                                opacity: 0,
                            }}
                            transition={{
                                duration: 0.3,
                                ease: "linear",
                            }}
                            style={{ marginLeft: '1rem' }}
                            className="text-zinc-500 text-sm sm:text-base font-normal pl-10 sm:pl-16 text-left w-[calc(100%-2rem)] truncate"
                        >
                            {placeholders[currentPlaceholder]}
                        </motion.p>
                    )}
                </AnimatePresence>
            </div>
        </form>
    );
}
