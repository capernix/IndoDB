"use client";

import Link from "next/link";

// ─── Types ───────────────────────────────────────────────────────────────────

// ─── Data ────────────────────────────────────────────────────────────────────

import { useEffect, useState } from "react";
import { getTrendingGames, getBiggestDeals, getHottestGames, Game as ApiGame } from "@/lib/api";

// ─── Data ────────────────────────────────────────────────────────────────────

export function useGamesData() {
    const [trending, setTrending] = useState<ApiGame[]>([]);
    const [deals, setDeals] = useState<ApiGame[]>([]);
    const [hottest, setHottest] = useState<ApiGame[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            getTrendingGames(8),
            getBiggestDeals(8),
            getHottestGames(8)
        ]).then(([trendingData, dealsData, hottestData]) => {
            
            // Helper to get max discount from a game's prices
            const getMaxDiscount = (game: ApiGame) => 
                game.prices ? Math.max(...game.prices.map(p => p.discountPercentage)) : 0;

            // Explicitly ensure the UI renders largest discounts first
            dealsData.sort((a, b) => getMaxDiscount(b) - getMaxDiscount(a));

            setTrending(trendingData);
            setDeals(dealsData);
            setHottest(hottestData);
            setLoading(false);
        }).catch(err => {
            console.error("Failed to fetch games:", err);
            setLoading(false);
        });
    }, []);

    return { trending, deals, hottest, loading };
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

const formatINR = (price: number) =>
    new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
    }).format(price);


// ─── Row ─────────────────────────────────────────────────────────────────────


function GameRow({ game, rank }: { game: ApiGame; rank: number }) {
    // Find the best deal or just default to first price
    const priceValue = (value: number | null | undefined) => value ?? Number.POSITIVE_INFINITY;
    const bestPriceInfo = game.prices?.reduce((prev, curr) => 
        (priceValue(curr.currentPrice) < priceValue(prev.currentPrice)) ? curr : prev
    , game.prices[0]);

    const price = bestPriceInfo?.currentPrice ?? 0;
    const originalPrice = bestPriceInfo?.originalPrice ?? 0;
    const discount = bestPriceInfo?.discountPercentage ?? 0;
    const platformName = bestPriceInfo?.platform?.name ?? "Steam";
    const isLive = game.dataSource === "STEAM_API" || game.dataSource === "ITAD_API";
    const freshnessLabel = isLive ? "Live" : "Seeded";
    const freshnessColor = isLive ? "text-emerald-400" : "text-amber-300";

    return (
        <Link href={`/games/${game.id}`} className="group flex items-center gap-6 border-b border-white/[0.05] py-8 last:border-0 hover:bg-white/[0.03] transition-colors -mx-4 px-4 rounded-lg cursor-pointer">
            {/* Rank */}
            <span className="w-6 flex-shrink-0 text-right text-sm tabular-nums text-neutral-700 font-medium">
                {rank}
            </span>

            {/* Thumbnail */}
            <div
                className="flex-shrink-0 overflow-hidden rounded-md bg-neutral-900"
                style={{ width: 160, height: 75 }}
            >
                {game.headerImageUrl && (
                    <img
                        src={game.headerImageUrl}
                        alt={game.title}
                        loading="lazy"
                        className="h-full w-full object-cover"
                    />
                )}
            </div>

            {/* Name + Platform */}
            <div className="min-w-0 flex-1">
                <p className="text-[18px] font-semibold text-neutral-100 group-hover:text-white transition-colors">
                    {game.title}
                </p>
                <p className="text-[12px] text-neutral-600 mt-1 uppercase tracking-widest">
                    {platformName}
                </p>
                <p className={`text-[11px] mt-1 uppercase tracking-wider ${freshnessColor}`}>
                    {freshnessLabel}
                </p>
            </div>

            {/* Original price */}
            <span className="hidden sm:block text-[14px] text-neutral-600 line-through tabular-nums w-24 text-right">
                {originalPrice > 0 ? formatINR(originalPrice) : ""}
            </span>

            {/* Discount */}
            <span className="w-20 flex-shrink-0 text-right text-[16px] font-bold text-emerald-400 tabular-nums">
                {discount > 0 ? `-${discount}%` : ""}
            </span>

            {/* Current price */}
            <span className="w-28 flex-shrink-0 text-right text-[20px] font-bold text-white tabular-nums">
                {price === 0 && (discount > 0 || originalPrice === 0) ? "Free" : formatINR(price)}
            </span>
        </Link>
    );
}

// ─── List ────────────────────────────────────────────────────────────────────

function GameList({ title, games, href }: { title: string; games: ApiGame[]; href: string }) {
    return (
        <div>
            {/* Header */}
            <div className="flex items-center justify-between border-b border-white/[0.08] pb-4 mb-2">
                <Link
                    href={href}
                    className="text-[28px] font-black text-white hover:text-[#FF9933] transition-colors"
                    style={{ letterSpacing: "-0.03em" }}
                >
                    {title} <span className="text-neutral-600 font-normal text-lg">→</span>
                </Link>

                <div className="hidden sm:flex items-center gap-5 text-[11px] uppercase tracking-widest text-neutral-600">
                    <span className="w-24 text-right">Original</span>
                    <span className="w-16 text-right">Off</span>
                    <span className="w-24 text-right">INR Price</span>
                </div>
            </div>

            {/* Rows */}
            <div>
                {games.map((game, i) => (
                    <GameRow key={`${game.id}-${i}`} game={game} rank={i + 1} />
                ))}
            </div>
        </div>
    );
}

// ─── Section ─────────────────────────────────────────────────────────────────

const LISTS = [
    { title: "🔥 Trending Games",    games: [], href: "/trending" },
    { title: "💰 Biggest Discounts", games: [], href: "/deals" },
    { title: "🌶️ Hottest Games",    games: [], href: "/hottest" },
];

export function GameLists() {
    const { trending, deals, hottest, loading } = useGamesData();

    if (loading) {
        return (
            <section className="w-full bg-[#080A0C] py-32 flex flex-col items-center">
                <div className="text-center text-neutral-500 font-medium">Fetching real-time data...</div>
            </section>
        );
    }

    const liveLists = [
        { title: "🔥 Trending Now",      games: trending, href: "/trending" },
        { title: "💰 Biggest Discounts", games: deals,    href: "/deals" },
        { title: "🌶️ Hottest Games",    games: hottest,   href: "/hottest" },
    ];

    return (
        <section className="w-full bg-[#080A0C] py-16 flex flex-col items-center">
            <div className="w-full max-w-6xl px-6 space-y-40">
                {liveLists.map((list) => (
                    <GameList key={list.title} {...list} />
                ))}
            </div>
        </section>
    );
}
