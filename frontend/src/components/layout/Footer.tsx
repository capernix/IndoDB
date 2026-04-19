import Link from "next/link";

const LINKS = {
    Product: [
        { label: "Home",           href: "/" },
        { label: "Deals",          href: "/deals" },
        { label: "Trending",       href: "/trending" },
        { label: "Voting",         href: "/voting" },
        { label: "Wishlist",       href: "/wishlist" },
    ],
    Discover: [
        { label: "Browse Games",   href: "/games" },
        { label: "Top Discounts",  href: "/deals" },
        { label: "Free Games",     href: "/free" },
        { label: "All-Time Lows",  href: "/deals?filter=atl" },
    ],
    Platforms: [
        { label: "Steam",          href: "https://store.steampowered.com", external: true },
        { label: "Epic Games",     href: "https://store.epicgames.com",    external: true },
        { label: "GOG",            href: "https://www.gog.com",            external: true },
    ],
    Account: [
        { label: "Sign In",        href: "/login" },
        { label: "Create Account", href: "/register" },
        { label: "My Wishlist",    href: "/wishlist" },
    ],
};

export function Footer() {
    return (
        <footer className="w-full bg-[#080A0C] border-t border-white/[0.06] mt-24">
            <div className="max-w-7xl mx-auto px-8 md:px-20">

                {/* Top section: brand + links */}
                <div className="py-14 flex flex-col lg:flex-row gap-12 lg:gap-20">

                    {/* Brand */}
                    <div className="lg:w-80 flex-shrink-0">
                        <Link href="/">
                            <span style={{ fontSize: 32, fontWeight: 900, letterSpacing: "-0.04em", color: "#fff" }}>
                                Indo<span style={{ color: "#FF9933" }}>DB</span>
                            </span>
                        </Link>
                        <p style={{ fontSize: 15, color: "#737373", lineHeight: 1.7, marginTop: 16 }}>
                            Real-time prices across Steam, Epic &amp; GOG —
                            tracked in INR for Indian gamers.
                        </p>
                        <p style={{ fontSize: 13, color: "#525252", marginTop: 18 }}>
                            🇮🇳 Built for Indian gamers
                        </p>
                    </div>

                    {/* Link columns */}
                    <div className="flex-1 grid grid-cols-2 md:grid-cols-4 gap-12">
                        {Object.entries(LINKS).map(([category, items]) => (
                            <div key={category}>
                                <p style={{ fontSize: 13, fontWeight: 700, letterSpacing: "0.1em", textTransform: "uppercase", color: "#a3a3a3", marginBottom: 18 }}>
                                    {category}
                                </p>
                                <ul style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                                    {items.map((item) => (
                                        <li key={item.label}>
                                            <Link
                                                href={item.href}
                                                target={"external" in item && item.external ? "_blank" : undefined}
                                                rel={"external" in item && item.external ? "noopener noreferrer" : undefined}
                                                style={{ fontSize: 15, color: "#737373" }}
                                                className="hover:text-white transition-colors"
                                            >
                                                {item.label}
                                            </Link>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Bottom bar */}
                <div style={{ borderTop: "1px solid rgba(255,255,255,0.05)", paddingTop: 20, paddingBottom: 20, display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 10 }}>
                    <p style={{ fontSize: 13, color: "#525252" }}>
                        © {new Date().getFullYear()} IndoDB. All rights reserved.
                    </p>
                    <p style={{ fontSize: 13, color: "#525252" }}>
                        Prices in INR · Data from Steam, Epic Games &amp; GOG
                    </p>
                </div>

            </div>
        </footer>
    );
}
