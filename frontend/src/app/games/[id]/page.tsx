import Link from "next/link";
import { ExternalLink, Heart } from "lucide-react";
import { PriceChart } from "@/components/ui/PriceChart";

// ─── Mock game data ───────────────────────────────────────────────────────────

const GAME = {
    appId: 1091500,
    name: "Cyberpunk 2077",
    developer: "CD Projekt Red",
    publisher: "CD Projekt",
    releaseDate: "December 10, 2020",
    description:
        "Cyberpunk 2077 is an open-world, action-adventure RPG set in the dark future of Night City — a dangerous megalopolis obsessed with power, glamour and body modification. Play as V, a mercenary outlaw going after a one-of-a-kind implant that is the key to immortality. You'll face brutal street action and make choices that ripple through the entire game.",
    genres: ["RPG", "Open World", "Sci-fi", "Action"],
    tags: ["Cyberpunk", "Open World", "RPG", "Story Rich", "Dark Future", "Character Customization", "Singleplayer", "First-Person"],
    reviewScore: 87,
    reviewCount: "567,234",
    platforms: {
        Steam: {
            price: 699,
            originalPrice: 3499,
            discount: 80,
            url: "https://store.steampowered.com/app/1091500",
            isAllTimeLow: true,
            color: "#3b82f6",
        },
        Epic: {
            price: 999,
            originalPrice: 3299,
            discount: 70,
            url: "https://store.epicgames.com/en-US/p/cyberpunk-2077",
            isAllTimeLow: false,
            color: "#a855f7",
        },
        GOG: {
            price: 649,
            originalPrice: 2799,
            discount: 77,
            url: "https://www.gog.com/game/cyberpunk_2077",
            isAllTimeLow: true,
            color: "#FF9933",
        },
    },
};

const formatINR = (price: number) =>
    new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(price);

// ─── Platform card ────────────────────────────────────────────────────────────

type PlatformData = { price: number; originalPrice: number; discount: number; url: string; isAllTimeLow: boolean; color: string };

function PlatformCard({ name, data, isBest }: { name: string; data: PlatformData; isBest: boolean }) {
    return (
        <div
            className="relative flex flex-col rounded-2xl p-8 transition-all duration-300"
            style={{
                background: "#0d0f11",
                border: isBest ? `1.5px solid ${data.color}50` : "1px solid rgba(255,255,255,0.07)",
                boxShadow: isBest ? `0 0 40px ${data.color}18` : "none",
            }}
        >
            {/* Badges */}
            <div className="absolute -top-3.5 left-0 right-0 flex justify-between px-6">
                {isBest ? (
                    <span className="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-widest"
                        style={{ background: data.color, color: data.color === "#FF9933" ? "#000" : "#fff" }}>
                        Best Price
                    </span>
                ) : <span />}
                {data.isAllTimeLow && (
                    <span className="rounded-full px-3 py-1 text-[11px] font-semibold uppercase tracking-widest"
                        style={{ background: "rgba(255,153,51,0.12)", color: "#FF9933", border: "1px solid rgba(255,153,51,0.3)" }}>
                        All-Time Low
                    </span>
                )}
            </div>

            {/* Platform label */}
            <p className="mb-8 font-bold uppercase tracking-widest" style={{ fontSize: 13, color: data.color }}>
                {name}
            </p>

            {/* Price */}
            <p className="font-black text-white" style={{ fontSize: 48, letterSpacing: "-0.03em", lineHeight: 1 }}>
                {formatINR(data.price)}
            </p>

            {/* Original + discount */}
            <div className="mt-3 mb-10 flex items-center gap-3">
                <span style={{ fontSize: 16, color: "#404040", textDecoration: "line-through" }}>
                    {formatINR(data.originalPrice)}
                </span>
                <span className="rounded-md px-2 py-0.5 font-bold"
                    style={{ fontSize: 13, background: "rgba(34,197,94,0.12)", color: "#22c55e" }}>
                    -{data.discount}%
                </span>
            </div>

            {/* CTA */}
            <Link
                href={data.url}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center justify-center gap-2 rounded-xl py-4 font-semibold transition-opacity hover:opacity-80 mt-auto"
                style={{
                    background: isBest ? data.color : "rgba(255,255,255,0.06)",
                    color: isBest && data.color === "#FF9933" ? "#000" : "#fff",
                    fontSize: 15,
                }}
            >
                Visit {name}
                <ExternalLink className="h-4 w-4" />
            </Link>
        </div>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default async function GameDetailPage({ params }: { params: Promise<{ id: string }> }) {
    await params;

    const game = GAME;
    const platforms = Object.entries(game.platforms) as [string, PlatformData][];
    const bestPrice = Math.min(...platforms.map(([, d]) => d.price));

    return (
        <div className="min-h-screen bg-[#080A0C]">

            {/* ── Hero ─────────────────────────────────────────────────────── */}
            <div className="relative w-full" style={{ height: 600 }}>
                <img
                    src={`https://cdn.akamai.steamstatic.com/steam/apps/${game.appId}/library_hero.jpg`}
                    alt={game.name}
                    className="h-full w-full object-cover object-center"
                />
                {/* Gradients */}
                <div className="absolute inset-0" style={{
                    background: "linear-gradient(to top, #080A0C 0%, rgba(8,10,12,0.55) 55%, rgba(8,10,12,0.05) 100%)"
                }} />
                <div className="absolute inset-0" style={{
                    background: "linear-gradient(to right, rgba(8,10,12,0.9) 0%, rgba(8,10,12,0.4) 50%, transparent 80%)"
                }} />

                {/* Content */}
                <div className="absolute bottom-0 left-0 right-0">
                    <div style={{ maxWidth: 1280, margin: "0 auto", padding: "0 80px 64px" }}>
                        <div className="flex flex-wrap gap-2 mb-5">
                            {game.genres.map(g => (
                                <span key={g} className="rounded-full px-3 py-1 text-xs font-medium"
                                    style={{ background: "rgba(255,255,255,0.12)", color: "#d4d4d4" }}>
                                    {g}
                                </span>
                            ))}
                        </div>
                        <h1 className="font-black text-white mb-5"
                            style={{ fontSize: "clamp(40px, 6vw, 72px)", letterSpacing: "-0.03em", lineHeight: 1.02 }}>
                            {game.name}
                        </h1>
                        <div className="flex flex-wrap items-center gap-3 text-[15px]" style={{ color: "#737373" }}>
                            <span>
                                <span style={{ color: "#22c55e", fontWeight: 700 }}>{game.reviewScore}%</span>
                                {" "}positive · {game.reviewCount} reviews
                            </span>
                            <span>·</span>
                            <span>{game.developer}</span>
                            <span>·</span>
                            <span>{game.releaseDate}</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* ── Body ─────────────────────────────────────────────────────── */}
            <div style={{ maxWidth: 1280, margin: "0 auto", padding: "0 80px 128px" }}>

                {/* Wishlist */}
                <div className="flex justify-end pt-10 pb-12">
                    <button
                        className="flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold transition-colors hover:bg-white/10"
                        style={{ border: "1px solid rgba(255,255,255,0.1)", color: "#a3a3a3" }}
                    >
                        <Heart className="h-4 w-4" />
                        Add to Wishlist
                    </button>
                </div>

                {/* ── Price cards ───────────────────────────────────────────── */}
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-3 mb-24">
                    {platforms.map(([name, data]) => (
                        <PlatformCard key={name} name={name} data={data} isBest={data.price === bestPrice} />
                    ))}
                </div>

                {/* ── Price history ─────────────────────────────────────────── */}
                <div className="rounded-2xl p-10 mb-24"
                    style={{ background: "#0d0f11", border: "1px solid rgba(255,255,255,0.06)" }}>
                    <h2 className="font-black text-white mb-2"
                        style={{ fontSize: 28, letterSpacing: "-0.02em" }}>
                        Price History
                    </h2>
                    <p className="mb-10 text-[14px]" style={{ color: "#737373" }}>
                        12 months of price tracking across all platforms · INR
                    </p>
                    <PriceChart />
                </div>

                {/* ── About + meta ──────────────────────────────────────────── */}
                <div className="grid grid-cols-1 gap-12 lg:grid-cols-3">
                    {/* Description */}
                    <div className="lg:col-span-2">
                        <h2 className="font-black text-white mb-6"
                            style={{ fontSize: 28, letterSpacing: "-0.02em" }}>
                            About
                        </h2>
                        <p style={{ fontSize: 17, color: "#a3a3a3", lineHeight: 1.85 }}>
                            {game.description}
                        </p>
                        <div className="mt-8 flex flex-wrap gap-2">
                            {game.tags.map(tag => (
                                <span key={tag} className="rounded-md px-3 py-1.5 text-[13px]"
                                    style={{ background: "rgba(255,255,255,0.04)", color: "#737373", border: "1px solid rgba(255,255,255,0.06)" }}>
                                    {tag}
                                </span>
                            ))}
                        </div>
                    </div>

                    {/* Metadata */}
                    <div className="rounded-2xl p-8 h-fit"
                        style={{ background: "#0d0f11", border: "1px solid rgba(255,255,255,0.06)" }}>
                        {[
                            { label: "Developer",    value: game.developer },
                            { label: "Publisher",    value: game.publisher },
                            { label: "Release Date", value: game.releaseDate },
                            { label: "Review Score", value: `${game.reviewScore}% positive` },
                            { label: "Reviews",      value: game.reviewCount },
                        ].map(({ label, value }) => (
                            <div key={label} className="flex justify-between py-4"
                                style={{ borderBottom: "1px solid rgba(255,255,255,0.05)" }}>
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
