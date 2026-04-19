// API client — wraps fetch with base URL and auth header

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";

async function request<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const res = await fetch(`${BASE_URL}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...options.headers,
        },
        ...options,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(text || `HTTP ${res.status}`);
    }

    return res.json() as Promise<T>;
}

// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthResponse {
    token: string;
    user: {
        id: number;
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
    currentPrice: number;
    originalPrice: number;
    discountPercentage: number;
    currency: string;
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
