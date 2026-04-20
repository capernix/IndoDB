"use client";

import Link from "next/link";
import { useState } from "react";
import { queueSteamGameSync, syncFullGameData, syncVisibleHomepageGames, type ItadSyncResponse, type SteamSyncResponse } from "@/lib/api";

const examples = [
    { appId: "292030", title: "The Witcher 3" },
    { appId: "1091500", title: "Cyberpunk 2077" },
    { appId: "271590", title: "Grand Theft Auto V" },
    { appId: "1145360", title: "Hades" },
];

type SyncState =
    | { status: "idle" }
    | { status: "loading"; message: string }
    | { status: "queued"; appId: string }
    | { status: "visible-sync"; message: string }
    | { status: "success"; steam: SteamSyncResponse; itad: ItadSyncResponse }
    | { status: "error"; message: string };

const formatINR = (price: number | null) => {
    if (price === null || price === undefined) return "Unavailable";
    if (price === 0) return "Free";

    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
    }).format(price);
};

export default function ImportPage() {
    const [steamAppId, setSteamAppId] = useState("292030");
    const [syncState, setSyncState] = useState<SyncState>({ status: "idle" });

    const queueOnly = async () => {
        const appId = steamAppId.trim();
        if (!/^\d+$/.test(appId)) {
            setSyncState({ status: "error", message: "Enter a numeric Steam app id." });
            return;
        }

        try {
            await queueSteamGameSync(appId, { source: "USER_IMPORT", priority: "HIGH", includeItad: true });
            setSyncState({ status: "queued", appId });
        } catch (error) {
            setSyncState({
                status: "error",
                message: error instanceof Error ? error.message : "Queue request failed.",
            });
        }
    };

    const syncVisibleNow = async () => {
        try {
            setSyncState({ status: "loading", message: "Syncing visible homepage games (Steam + ITAD)..." });
            const result = await syncVisibleHomepageGames({ limit: 8, includeItad: true, queueOnly: false });
            const failedCount = result.failedAppIds.length;
            const message = failedCount > 0
                ? `Visible sync completed. Synced ${result.syncedAppIds.length}/${result.totalCandidates} games (${failedCount} failed).`
                : `Visible sync completed. Synced ${result.syncedAppIds.length} games.`;
            setSyncState({ status: "visible-sync", message });
        } catch (error) {
            setSyncState({
                status: "error",
                message: error instanceof Error ? error.message : "Visible sync failed.",
            });
        }
    };

    const handleSync = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        const appId = steamAppId.trim();
        if (!/^\d+$/.test(appId)) {
            setSyncState({ status: "error", message: "Enter a numeric Steam app id." });
            return;
        }

        try {
            setSyncState({ status: "loading", message: "Fetching Steam metadata and INR pricing..." });
            const result = await syncFullGameData(appId);
            setSyncState({ status: "success", ...result });
        } catch (error) {
            setSyncState({
                status: "error",
                message: error instanceof Error ? error.message : "Sync failed.",
            });
        }
    };

    return (
        <main className="min-h-screen bg-[#080A0C] px-6 pb-24 text-white" style={{ paddingTop: "7.5rem" }}>
            <section className="mx-auto max-w-4xl">
                <p className="mb-3 text-sm font-semibold uppercase tracking-widest text-[#FF9933]">
                    Data Import
                </p>
                <h1 className="max-w-3xl text-5xl font-black leading-tight tracking-tight md:text-6xl">
                    Pull real store prices into IndoDB.
                </h1>
                <p className="mt-6 max-w-2xl text-base leading-7 text-neutral-400">
                    Enter a Steam app id. IndoDB will sync Steam metadata first, then use ITAD to attach Epic and GOG INR prices when available.
                </p>

                <form onSubmit={handleSync} className="mt-12 flex flex-col gap-4 sm:flex-row">
                    <input
                        value={steamAppId}
                        onChange={(event) => setSteamAppId(event.target.value)}
                        inputMode="numeric"
                        placeholder="292030"
                        className="h-14 flex-1 rounded-lg border border-white/10 bg-white/[0.04] px-5 text-lg font-semibold text-white outline-none transition-colors focus:border-[#FF9933]"
                    />
                    <button
                        type="submit"
                        disabled={syncState.status === "loading"}
                        className="h-14 rounded-lg bg-[#FF9933] px-8 text-sm font-black uppercase tracking-widest text-black transition-opacity hover:opacity-85 disabled:opacity-50"
                    >
                        {syncState.status === "loading" ? "Syncing" : "Sync Game"}
                    </button>
                    <button
                        type="button"
                        onClick={queueOnly}
                        className="h-14 rounded-lg border border-white/20 px-6 text-sm font-bold uppercase tracking-widest text-white transition-colors hover:border-[#FF9933]/70"
                    >
                        Queue Async Sync
                    </button>
                    <button
                        type="button"
                        onClick={syncVisibleNow}
                        className="h-14 rounded-lg border border-emerald-400/40 px-6 text-sm font-bold uppercase tracking-widest text-emerald-200 transition-colors hover:border-emerald-300"
                    >
                        Sync Visible Home
                    </button>
                </form>

                <div className="mt-5 flex flex-wrap gap-2">
                    {examples.map((example) => (
                        <button
                            key={example.appId}
                            type="button"
                            onClick={() => setSteamAppId(example.appId)}
                            className="rounded-md border border-white/10 px-3 py-2 text-sm text-neutral-400 transition-colors hover:border-[#FF9933]/50 hover:text-white"
                        >
                            {example.title}
                        </button>
                    ))}
                </div>

                {syncState.status === "loading" && (
                    <div className="mt-12 rounded-lg border border-white/10 bg-white/[0.03] p-6 text-neutral-300">
                        {syncState.message}
                    </div>
                )}

                {syncState.status === "error" && (
                    <div className="mt-12 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-red-200">
                        {syncState.message}
                    </div>
                )}

                {syncState.status === "queued" && (
                    <div className="mt-12 rounded-lg border border-emerald-500/30 bg-emerald-500/10 p-6 text-emerald-200">
                        Sync queued for app id {syncState.appId}. Background worker will ingest Steam and ITAD data shortly.
                    </div>
                )}

                {syncState.status === "visible-sync" && (
                    <div className="mt-12 rounded-lg border border-emerald-500/30 bg-emerald-500/10 p-6 text-emerald-200">
                        {syncState.message}
                    </div>
                )}

                {syncState.status === "success" && (
                    <div className="mt-12 rounded-lg border border-white/10 bg-white/[0.03] p-6">
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                            <div>
                                <p className="text-sm uppercase tracking-widest text-neutral-500">Synced</p>
                                <h2 className="mt-2 text-3xl font-black">{syncState.steam.game.title}</h2>
                                <p className="mt-2 text-neutral-400">
                                    Steam price: {formatINR(syncState.steam.game.currentPrice)}
                                </p>
                            </div>
                            <Link
                                href={`/games/${syncState.steam.game.gameId}`}
                                className="rounded-lg border border-white/10 px-5 py-3 text-center text-sm font-bold text-neutral-200 transition-colors hover:border-[#FF9933]/50 hover:text-white"
                            >
                                Open Game Page
                            </Link>
                        </div>

                        <div className="mt-8 border-t border-white/10 pt-6">
                            <p className="text-sm font-semibold text-neutral-300">{syncState.itad.result.message}</p>
                            <p className="mt-2 text-sm text-neutral-500">
                                ITAD ID: {syncState.itad.result.itadId ?? "Not linked"}
                            </p>

                            <div className="mt-5 grid gap-3 sm:grid-cols-2">
                                {syncState.itad.result.prices.map((price) => (
                                    <div key={price.platform} className="rounded-lg border border-white/10 bg-black/20 p-4">
                                        <p className="text-sm font-bold uppercase tracking-widest text-[#FF9933]">
                                            {price.platform}
                                        </p>
                                        <p className="mt-3 text-2xl font-black">{formatINR(price.currentPrice)}</p>
                                        {price.discountPercentage > 0 && (
                                            <p className="mt-1 text-sm text-emerald-400">
                                                {price.discountPercentage}% off from {formatINR(price.originalPrice)}
                                            </p>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}
            </section>
        </main>
    );
}
