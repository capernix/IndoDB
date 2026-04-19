"use client";

import Link from "next/link";

const FEATURED_DEALS = [
    {
        appId: 1091500,
        name: "Cyberpunk 2077",
        platform: "Steam",
        genres: ["RPG", "Open World"],
        originalPrice: 3499,
        currentPrice: 699,
        discount: 80,
        isAllTimeLow: true,
        rating: 87,
    },
    {
        appId: 292030,
        name: "The Witcher 3",
        platform: "Steam",
        genres: ["RPG", "Action"],
        originalPrice: 999,
        currentPrice: 199,
        discount: 80,
        isAllTimeLow: false,
        rating: 97,
    },
    {
        appId: 1145360,
        name: "Hades",
        platform: "Steam",
        genres: ["Roguelite", "Action"],
        originalPrice: 849,
        currentPrice: 424,
        discount: 50,
        isAllTimeLow: false,
        rating: 98,
    },
    {
        appId: 990080,
        name: "Hogwarts Legacy",
        platform: "Steam",
        genres: ["Action RPG", "Adventure"],
        originalPrice: 3999,
        currentPrice: 1199,
        discount: 70,
        isAllTimeLow: true,
        rating: 84,
    },
];

const formatINR = (price: number) =>
    new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
    }).format(price);

function GameCard({ game }: { game: (typeof FEATURED_DEALS)[0] }) {
    return (
        <div className="group relative overflow-hidden rounded-xl border border-white/[0.06] bg-[#0d0f11] transition-all duration-300 hover:border-white/[0.12]">
            {/* Cover image */}
            <div className="relative overflow-hidden" style={{ aspectRatio: "460 / 215" }}>
                <img
                    src={`https://cdn.akamai.steamstatic.com/steam/apps/${game.appId}/header.jpg`}
                    alt={game.name}
                    loading="lazy"
                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                />

                {/* Discount badge */}
                <div className="absolute right-2 top-2 rounded bg-emerald-500 px-1.5 py-0.5 text-[11px] font-bold text-white">
                    -{game.discount}%
                </div>

                {/* All-time low badge */}
                {game.isAllTimeLow && (
                    <div className="absolute left-2 top-2 rounded border border-[#FF9933]/40 bg-[#FF9933]/10 px-2 py-0.5 text-[9px] font-semibold uppercase tracking-widest text-[#FF9933]">
                        All-time Low
                    </div>
                )}
            </div>

            {/* Body */}
            <div className="p-3.5">
                {/* Platform + rating */}
                <div className="mb-2 flex items-center justify-between">
                    <span className="text-[10px] font-semibold uppercase tracking-widest text-neutral-600">
                        {game.platform}
                    </span>
                    <span className="text-[10px] text-neutral-600">{game.rating}% positive</span>
                </div>

                {/* Title */}
                <h3 className="mb-2 truncate text-sm font-semibold text-white">{game.name}</h3>

                {/* Genres */}
                <div className="mb-3 flex flex-wrap gap-1">
                    {game.genres.map((g) => (
                        <span
                            key={g}
                            className="rounded-full bg-white/[0.04] px-2 py-0.5 text-[10px] text-neutral-600"
                        >
                            {g}
                        </span>
                    ))}
                </div>

                {/* Price */}
                <div className="flex items-baseline gap-2">
                    <span className="text-[15px] font-bold text-white">
                        {formatINR(game.currentPrice)}
                    </span>
                    <span className="text-xs text-neutral-600 line-through">
                        {formatINR(game.originalPrice)}
                    </span>
                </div>
            </div>
        </div>
    );
}

export function FeaturedDeals() {
    return (
        <section className="w-full bg-[#080A0C]">
            <div className="mx-auto max-w-7xl px-6 py-20 md:px-16">
                {/* Header */}
                <div className="mb-10 flex items-baseline justify-between">
                    <h2
                        className="font-black text-white"
                        style={{
                            fontSize: "clamp(28px, 3.5vw, 48px)",
                            letterSpacing: "-0.03em",
                            lineHeight: 1,
                        }}
                    >
                        Don&apos;t miss these.
                    </h2>
                    <Link
                        href="/deals"
                        className="text-sm text-neutral-500 transition-colors hover:text-white"
                    >
                        View all deals →
                    </Link>
                </div>

                {/* Grid */}
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    {FEATURED_DEALS.map((game) => (
                        <GameCard key={game.appId} game={game} />
                    ))}
                </div>
            </div>
        </section>
    );
}
