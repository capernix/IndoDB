"use client";

import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend,
} from "recharts";

export type PriceHistoryPoint = {
    date: string;
    steam?: number | null;
    epic?: number | null;
    gog?: number | null;
};

type PriceChartProps = {
    data: PriceHistoryPoint[];
};

const formatINR = (value: number) =>
    new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
    }).format(value);

function formatDateLabel(value: string) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleDateString("en-IN", { month: "short", day: "numeric" });
}

function formatTooltipDate(value: string) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleDateString("en-IN", {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function CustomTooltip({ active, payload, label }: any) {
    if (!active || !payload?.length) return null;
    return (
        <div
            style={{
                background: "#111316",
                border: "1px solid rgba(255,255,255,0.08)",
                borderRadius: 10,
                padding: "12px 16px",
                minWidth: 160,
            }}
        >
            <p style={{ color: "#737373", fontSize: 12, marginBottom: 10 }}>{formatTooltipDate(label)}</p>
            {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
            {payload.filter((p: any) => typeof p.value === "number").map((p: any) => (
                <p
                    key={p.dataKey}
                    style={{ color: p.color, fontSize: 14, fontWeight: 700, marginBottom: 4 }}
                >
                    {p.name}: {formatINR(p.value)}
                </p>
            ))}
        </div>
    );
}

export function PriceChart({ data }: PriceChartProps) {
    if (!data.length) {
        return (
            <div className="rounded-xl border border-white/10 bg-white/[0.02] px-6 py-10 text-sm text-neutral-400">
                Not enough price history yet. Sync this game a few times to build a trend line.
            </div>
        );
    }

    return (
        <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={data} margin={{ top: 10, right: 4, left: 8, bottom: 0 }}>
                <defs>
                    <linearGradient id="steamGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.25} />
                        <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="epicGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#94a3b8" stopOpacity={0.2} />
                        <stop offset="95%" stopColor="#94a3b8" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="gogGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#FF9933" stopOpacity={0.2} />
                        <stop offset="95%" stopColor="#FF9933" stopOpacity={0} />
                    </linearGradient>
                </defs>

                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" />

                <XAxis
                    dataKey="date"
                    tickFormatter={formatDateLabel}
                    tick={{ fill: "#737373", fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    minTickGap={28}
                />
                <YAxis
                    tickFormatter={(v) => `₹${(v / 1000).toFixed(1)}k`}
                    tick={{ fill: "#737373", fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    width={54}
                />

                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ color: "#a3a3a3", fontSize: 13, paddingTop: 20 }} />

                <Area
                    type="stepAfter"
                    dataKey="steam"
                    name="Steam"
                    stroke="#3b82f6"
                    fill="url(#steamGrad)"
                    strokeWidth={2}
                    dot={false}
                    connectNulls
                />
                <Area
                    type="stepAfter"
                    dataKey="epic"
                    name="Epic"
                    stroke="#94a3b8"
                    fill="url(#epicGrad)"
                    strokeWidth={2}
                    dot={false}
                    connectNulls
                />
                <Area
                    type="stepAfter"
                    dataKey="gog"
                    name="GOG"
                    stroke="#FF9933"
                    fill="url(#gogGrad)"
                    strokeWidth={2}
                    dot={false}
                    connectNulls
                />
            </AreaChart>
        </ResponsiveContainer>
    );
}
