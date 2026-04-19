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

// ─── Mock price history data ──────────────────────────────────────────────────

const HISTORY = [
    { month: "Apr '24", steam: 3499, epic: 3299, gog: 2799 },
    { month: "May '24", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Jun '24", steam: 1049, epic: 989,  gog: 840  },
    { month: "Jul '24", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Aug '24", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Sep '24", steam: 699,  epic: 999,  gog: 649  },
    { month: "Oct '24", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Nov '24", steam: 699,  epic: 999,  gog: 649  },
    { month: "Dec '24", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Jan '25", steam: 699,  epic: 3299, gog: 2799 },
    { month: "Feb '25", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Mar '25", steam: 3499, epic: 3299, gog: 2799 },
    { month: "Apr '25", steam: 699,  epic: 3299, gog: 649  },
];

// ─── Helpers ─────────────────────────────────────────────────────────────────

const formatINR = (value: number) =>
    new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
    }).format(value);

// ─── Custom tooltip ───────────────────────────────────────────────────────────

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
            <p style={{ color: "#737373", fontSize: 12, marginBottom: 10 }}>{label}</p>
            {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
            {payload.map((p: any) => (
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

// ─── Chart component ──────────────────────────────────────────────────────────

export function PriceChart() {
    return (
        <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={HISTORY} margin={{ top: 10, right: 4, left: 8, bottom: 0 }}>
                <defs>
                    <linearGradient id="steamGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.25} />
                        <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="epicGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#a855f7" stopOpacity={0.2} />
                        <stop offset="95%" stopColor="#a855f7" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="gogGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#FF9933" stopOpacity={0.2} />
                        <stop offset="95%" stopColor="#FF9933" stopOpacity={0} />
                    </linearGradient>
                </defs>

                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" />

                <XAxis
                    dataKey="month"
                    tick={{ fill: "#737373", fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                />
                <YAxis
                    tickFormatter={(v) => `₹${(v / 1000).toFixed(1)}k`}
                    tick={{ fill: "#737373", fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    width={54}
                />

                <Tooltip content={<CustomTooltip />} />
                <Legend
                    wrapperStyle={{ color: "#a3a3a3", fontSize: 13, paddingTop: 20 }}
                />

                <Area
                    type="stepAfter"
                    dataKey="steam"
                    name="Steam"
                    stroke="#3b82f6"
                    fill="url(#steamGrad)"
                    strokeWidth={2}
                    dot={false}
                />
                <Area
                    type="stepAfter"
                    dataKey="epic"
                    name="Epic"
                    stroke="#a855f7"
                    fill="url(#epicGrad)"
                    strokeWidth={2}
                    dot={false}
                />
                <Area
                    type="stepAfter"
                    dataKey="gog"
                    name="GOG"
                    stroke="#FF9933"
                    fill="url(#gogGrad)"
                    strokeWidth={2}
                    dot={false}
                />
            </AreaChart>
        </ResponsiveContainer>
    );
}
