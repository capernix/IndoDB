import Link from "next/link";
import { BarChart3, ExternalLink, Heart } from "lucide-react";
import { PriceChart, type PriceHistoryPoint } from "@/components/ui/PriceChart";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";

type Game = {
    id: string;
    title: string;
    description?: string | null;
    shortDescription?: string | null;
    developer?: string | null;
    publisher?: string | null;
    releaseDate?: string | null;
    genres?: string[] | null;
    tags?: string[] | null;
    metacriticScore?: number | null;
    steamAppId?: number | null;
    headerImageUrl?: string | null;
};

type GamePrice = {
    platform: {
        id: number;
        name: string;
        type: "STEAM" | "EPIC" | "GOG" | "OTHER";
    };
    currentPrice: number | null;
    originalPrice: number | null;
    discountPercentage: number;
    currency: string;
};

function formatINR(price: number) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
    }).format(price);
}

function resolveStoreUrl(price: GamePrice, game: Game): string | null {
    const type = price.platform.type;
    if (type === "STEAM" && game.steamAppId) {
        return `https://store.steampowered.com/app/${game.steamAppId}`;
    }
    if (type === "EPIC") {
        return "https://store.epicgames.com";
    }
    if (type === "GOG") {
        return "https://www.gog.com";
    }
    return null;
}

function platformAccent(type: GamePrice["platform"]["type"]) {
    if (type === "STEAM") return "#3b82f6";
    if (type === "EPIC") return "#94a3b8";
    if (type === "GOG") return "#FF9933";
    return "#22c55e";
}

function PlatformCard({
    game,
    price,
    bestPrice,
}: {
    game: Game;
    price: GamePrice;
    bestPrice: number;
}) {
    const current = price.currentPrice ?? 0;
    const original = price.originalPrice ?? current;
    const discount = price.discountPercentage ?? 0;
    const isBest = current === bestPrice;
    const accent = platformAccent(price.platform.type);
    const storeUrl = resolveStoreUrl(price, game);

    return (
        <div
            className="relative flex h-full flex-col rounded-2xl p-8"
            style={{
                background: "#0d0f11",
                border: isBest ? `1.5px solid ${accent}55` : "1px solid rgba(255,255,255,0.07)",
                boxShadow: isBest ? `0 0 40px ${accent}22` : "none",
            }}
        >
            <div className="mb-4 flex min-h-6 items-center justify-between gap-3">
                {isBest ? (
                    <span
                        className="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-widest"
                        style={{ background: accent, color: accent === "#FF9933" ? "#000" : "#fff" }}
                    >
                        Best Price
                    </span>
                ) : (
                    <span />
                )}
                {discount > 0 && (
                    <span
                        className="rounded-full px-3 py-1 text-[11px] font-semibold uppercase tracking-widest"
                        style={{ background: "rgba(34,197,94,0.12)", color: "#22c55e", border: "1px solid rgba(34,197,94,0.35)" }}
                    >
                        -{discount}%
                    </span>
                )}
            </div>

            <p className="mb-6 font-bold uppercase tracking-widest" style={{ fontSize: 13, color: accent }}>
                {price.platform.name}
            </p>

            <p className="font-black text-white" style={{ fontSize: 42, letterSpacing: "-0.03em", lineHeight: 1 }}>
                {current === 0 ? "Free" : formatINR(current)}
            </p>

            <div className="mt-3 mb-10 flex items-center gap-3">
                {original > current && (
                    <span style={{ fontSize: 16, color: "#404040", textDecoration: "line-through" }}>
                        {formatINR(original)}
                    </span>
                )}
            </div>

            {storeUrl ? (
                <Link
                    href={storeUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-auto flex items-center justify-center gap-2 rounded-xl py-4 font-semibold transition-opacity hover:opacity-80"
                    style={{
                        background: isBest ? accent : "rgba(255,255,255,0.06)",
                        color: isBest && accent === "#FF9933" ? "#000" : "#fff",
                        fontSize: 15,
                    }}
                >
                    Visit Store
                    <ExternalLink className="h-4 w-4" />
                </Link>
            ) : (
                <button
                    disabled
                    className="mt-auto rounded-xl py-4 font-semibold"
                    style={{ background: "rgba(255,255,255,0.06)", color: "#737373", fontSize: 15 }}
                >
                    Link Unavailable
                </button>
            )}
        </div>
    );
}

async function fetchGame(id: string): Promise<Game | null> {
    const res = await fetch(`${BASE_URL}/api/games/${id}`, { cache: "no-store" });
    if (!res.ok) return null;
    return (await res.json()) as Game;
}

async function fetchPrices(id: string): Promise<GamePrice[]> {
    const res = await fetch(`${BASE_URL}/api/prices/compare/${id}`, { cache: "no-store" });
    if (!res.ok) return [];
    return (await res.json()) as GamePrice[];
}

async function fetchPriceHistory(id: string): Promise<PriceHistoryPoint[]> {
    const res = await fetch(`${BASE_URL}/api/prices/history/${id}?days=180`, { cache: "no-store" });
    if (!res.ok) return [];
    return (await res.json()) as PriceHistoryPoint[];
}

export default async function GameDetailPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = await params;
    const [game, prices, history] = await Promise.all([fetchGame(id), fetchPrices(id), fetchPriceHistory(id)]);

    if (!game) {
        return (
            <main className="min-h-screen bg-[#080A0C] px-6 pt-32 text-white">
                <div className="mx-auto max-w-3xl rounded-xl border border-white/10 bg-white/[0.03] p-8">
                    <h1 className="text-3xl font-black">Game not found</h1>
                    <p className="mt-4 text-neutral-400">
                        This game is missing from IndoDB. You can sync it from the import page if you have a Steam app ID.
                    </p>
                    <Link href="/import" className="mt-6 inline-block rounded-lg bg-[#FF9933] px-5 py-3 font-bold text-black">
                        Go to Import
                    </Link>
                </div>
            </main>
        );
    }

    const numericPrices = prices
        .map((p) => p.currentPrice)
        .filter((p): p is number => typeof p === "number");
    const bestPrice = numericPrices.length ? Math.min(...numericPrices) : 0;

    const heroImage =
        game.headerImageUrl ||
        (game.steamAppId ? `https://cdn.akamai.steamstatic.com/steam/apps/${game.steamAppId}/library_hero.jpg` : null);

    return (
        <div className="min-h-screen bg-[#080A0C]">
            <div className="relative w-full" style={{ height: 520 }}>
                {heroImage ? (
                    <img src={heroImage} alt={game.title} className="h-full w-full object-cover object-center" />
                ) : (
                    <div className="h-full w-full" style={{ background: "linear-gradient(135deg, #111315, #0b0d0f)" }} />
                )}
                <div
                    className="absolute inset-0"
                    style={{
                        background:
                            "linear-gradient(to top, #080A0C 0%, rgba(8,10,12,0.58) 55%, rgba(8,10,12,0.1) 100%)",
                    }}
                />
                <div className="absolute bottom-0 left-0 right-0">
                    <div style={{ maxWidth: 1280, margin: "0 auto", padding: "0 80px 56px" }}>
                        <div className="mb-5 flex flex-wrap gap-2">
                            {(game.genres ?? []).slice(0, 6).map((g) => (
                                <span
                                    key={g}
                                    className="rounded-full px-3 py-1 text-xs font-medium"
                                    style={{ background: "rgba(255,255,255,0.12)", color: "#d4d4d4" }}
                                >
                                    {g}
                                </span>
                            ))}
                        </div>
                        <h1
                            className="mb-4 font-black text-white"
                            style={{ fontSize: "clamp(40px, 6vw, 72px)", letterSpacing: "-0.03em", lineHeight: 1.02 }}
                        >
                            {game.title}
                        </h1>
                        <div className="flex flex-wrap items-center gap-3 text-[15px]" style={{ color: "#737373" }}>
                            {typeof game.metacriticScore === "number" && (
                                <span
                                    className="inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1"
                                    style={{ borderColor: "rgba(34,197,94,0.3)", background: "rgba(34,197,94,0.1)" }}
                                >
                                    <BarChart3 className="h-3.5 w-3.5" style={{ color: "#22c55e" }} />
                                    <span style={{ color: "#22c55e", fontWeight: 700 }}>{game.metacriticScore}</span>
                                    <span style={{ color: "#86efac", fontSize: 12, fontWeight: 600, letterSpacing: "0.03em" }}>
                                        METACRITIC
                                    </span>
                                </span>
                            )}
                            {game.developer && <span>{game.developer}</span>}
                            {game.releaseDate && <span>{game.releaseDate}</span>}
                        </div>
                    </div>
                </div>
            </div>

            <div style={{ maxWidth: 1280, margin: "0 auto", padding: "0 80px 120px" }}>
                <div className="flex justify-end pb-10 pt-8">
                    <button
                        className="flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold transition-colors hover:bg-white/10"
                        style={{ border: "1px solid rgba(255,255,255,0.1)", color: "#a3a3a3" }}
                    >
                        <Heart className="h-[16px] w-[16px]" />
                        Add to Wishlist
                    </button>
                </div>

                {prices.length > 0 ? (
                    <div className="mb-24 grid grid-cols-1 gap-6 sm:grid-cols-3">
                        {prices.map((price) => (
                            <PlatformCard
                                key={`${price.platform.id}-${price.platform.name}`}
                                game={game}
                                price={price}
                                bestPrice={bestPrice}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="mb-24 rounded-xl border border-white/10 bg-white/[0.03] p-6 text-neutral-300">
                        No synced prices yet for this game. Use Import to fetch Steam and ITAD prices.
                    </div>
                )}

                <div className="mb-24 rounded-2xl p-10" style={{ background: "#0d0f11", border: "1px solid rgba(255,255,255,0.06)" }}>
                    <h2 className="mb-2 font-black text-white" style={{ fontSize: 28, letterSpacing: "-0.02em" }}>
                        Price History
                    </h2>
                    <p className="mb-10 text-[14px]" style={{ color: "#737373" }}>
                        Last 180 days of tracked prices across stores.
                    </p>
                    <PriceChart data={history} />
                </div>

                <div className="grid grid-cols-1 gap-12 lg:grid-cols-3">
                    <div className="lg:col-span-2">
                        <h2 className="mb-6 font-black text-white" style={{ fontSize: 28, letterSpacing: "-0.02em" }}>
                            About
                        </h2>
                        <p style={{ fontSize: 17, color: "#a3a3a3", lineHeight: 1.85 }}>
                            {game.description || game.shortDescription || "No description available yet."}
                        </p>
                        <div className="mt-8 flex flex-wrap gap-2">
                            {(game.tags ?? []).map((tag) => (
                                <span
                                    key={tag}
                                    className="rounded-md px-3 py-1.5 text-[13px]"
                                    style={{ background: "rgba(255,255,255,0.04)", color: "#737373", border: "1px solid rgba(255,255,255,0.06)" }}
                                >
                                    {tag}
                                </span>
                            ))}
                        </div>
                    </div>

                    <div className="h-fit rounded-2xl p-8" style={{ background: "#0d0f11", border: "1px solid rgba(255,255,255,0.06)" }}>
                        {[
                            { label: "Developer", value: game.developer || "-" },
                            { label: "Publisher", value: game.publisher || "-" },
                            { label: "Release Date", value: game.releaseDate || "-" },
                            { label: "Metacritic", value: game.metacriticScore?.toString() || "-" },
                            { label: "Steam App ID", value: game.steamAppId?.toString() || "-" },
                        ].map(({ label, value }) => (
                            <div key={label} className="flex justify-between py-4" style={{ borderBottom: "1px solid rgba(255,255,255,0.05)" }}>
                                <span style={{ fontSize: 14, color: "#525252" }}>{label}</span>
                                <span style={{ fontSize: 14, color: "#d4d4d4", fontWeight: 600 }}>{value}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
