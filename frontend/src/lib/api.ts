// API client — wraps fetch with base URL and auth header

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";

function getAuthToken(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem("indodb_token");
}

async function request<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const token = getAuthToken();

    const res = await fetch(`${BASE_URL}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...options.headers,
        },
        ...options,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(text || `HTTP ${res.status}`);
    }

    if (res.status === 204) {
        return undefined as T;
    }

    const contentType = res.headers.get("content-type") ?? "";
    if (!contentType.includes("application/json")) {
        return undefined as T;
    }

    return res.json() as Promise<T>;
}

// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthResponse {
    token: string;
    type: string;
    user: {
        id: string;
        username: string;
        email: string;
        firstName?: string;
        lastName?: string;
    };
}

export function login(emailOrUsername: string, password: string) {
    return request<AuthResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ emailOrUsername, password }),
    });
}

export function signup(data: {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
}) {
    return request<AuthResponse>("/api/auth/signup", {
        method: "POST",
        body: JSON.stringify(data),
    });
}

// ─── Games ───────────────────────────────────────────────────────────────────

export interface Game {
    id: string;
    title: string;
    description: string;
    steamAppId?: number;
    headerImageUrl: string;
    prices?: GamePrice[];
}

export interface GamePrice {
    platform: { id: number; name: string };
    currentPrice: number | null;
    originalPrice: number | null;
    discountPercentage: number;
    currency: string;
}

export interface WishlistItem {
    id: string;
    game: Game;
    targetPrice: number | null;
    currency: string;
    isActive: boolean;
}

export interface VoteStatus {
    hasVotedThisMonth: boolean;
}

export interface VoteLeaderboardEntry {
    gameId: string;
    gameTitle: string;
    voteCount: number;
}

export function getTrendingGames(limit = 8) {
    return request<Game[]>(`/api/games/trending?limit=${limit}`);
}

export function getBiggestDeals(limit = 8) {
    return request<Game[]>(`/api/games/deals?limit=${limit}`);
}

export function getHottestGames(limit = 8) {
    return request<Game[]>(`/api/games/hottest?limit=${limit}`);
}

export function getGameById(gameId: string) {
    return request<Game>(`/api/games/${gameId}`);
}

export function getGamePriceComparison(gameId: string) {
    return request<GamePrice[]>(`/api/prices/compare/${gameId}`);
}

export function getCurrentUser() {
    return request<AuthResponse["user"]>("/api/auth/me");
}

export function getWishlist() {
    return request<WishlistItem[]>("/api/wishlist");
}

export function addToWishlist(gameId: string, targetPrice?: number) {
    return request<WishlistItem>(`/api/wishlist/${gameId}`, {
        method: "POST",
        body: JSON.stringify(targetPrice === undefined ? {} : { targetPrice }),
    });
}

export function removeFromWishlist(gameId: string) {
    return request<void>(`/api/wishlist/${gameId}`, {
        method: "DELETE",
    });
}

export function castVote(gameId: string) {
    return request(`/api/votes/${gameId}`, {
        method: "POST",
    });
}

export function getVoteStatus() {
    return request<VoteStatus>("/api/votes/status");
}

export function getVoteLeaderboard(limit = 10) {
    return request<VoteLeaderboardEntry[]>(`/api/votes/leaderboard?limit=${limit}`);
}

// ─── Data Sync ───────────────────────────────────────────────────────────────

export interface SteamSyncResponse {
    status: string;
    game: {
        gameId: string;
        steamAppId: number;
        title: string;
        currentPrice: number | null;
        originalPrice: number | null;
        discountPercentage: number;
        currency: string;
        isFree: boolean;
    };
}

export interface ItadSyncResponse {
    status: string;
    result: {
        gameId: string;
        steamAppId: number;
        title: string;
        itadId: string | null;
        linkedItadId: boolean;
        message: string;
        prices: Array<{
            platform: string;
            currentPrice: number;
            originalPrice: number;
            discountPercentage: number;
            currency: string;
        }>;
    };
}

export function syncSteamGame(steamAppId: string) {
    return request<SteamSyncResponse>(`/api/steam/sync/${steamAppId}`, {
        method: "POST",
    });
}

export function syncItadPrices(steamAppId: string) {
    return request<ItadSyncResponse>(`/api/itad/sync/${steamAppId}`, {
        method: "POST",
    });
}

export async function syncFullGameData(steamAppId: string) {
    const steam = await syncSteamGame(steamAppId);
    const itad = await syncItadPrices(steamAppId);

    return { steam, itad };
}
