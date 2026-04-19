"use client";

const ROW_1 = [
    { appId: 1091500, name: "Cyberpunk 2077" },
    { appId: 1245620, name: "Elden Ring" },
    { appId: 1174180, name: "Red Dead Redemption 2" },
    { appId: 2358720, name: "Black Myth: Wukong" },
    { appId: 292030,  name: "The Witcher 3" },
    { appId: 1086940, name: "Baldur's Gate 3" },
    { appId: 990080,  name: "Hogwarts Legacy" },
    { appId: 1593500, name: "God of War" },
];

const ROW_2 = [
    { appId: 1145360, name: "Hades" },
    { appId: 582010,  name: "Monster Hunter: World" },
    { appId: 413150,  name: "Stardew Valley" },
    { appId: 367520,  name: "Hollow Knight" },
    { appId: 271590,  name: "GTA V" },
    { appId: 814380,  name: "Sekiro" },
    { appId: 1928980, name: "Lies of P" },
    { appId: 489830,  name: "Skyrim SE" },
];

function MarqueeTrack({
    games,
    reverse = false,
}: {
    games: { appId: number; name: string }[];
    reverse?: boolean;
}) {
    const doubled = [...games, ...games];

    return (
        <div className="overflow-hidden">
            <div
                className="flex gap-3"
                style={{
                    animation: `${reverse ? "marquee-reverse" : "marquee"} 32s linear infinite`,
                    width: "max-content",
                }}
            >
                {doubled.map((game, i) => (
                    <div
                        key={`${game.appId}-${i}`}
                        className="relative flex-shrink-0 overflow-hidden rounded-lg"
                        style={{ width: 280, height: 131 }}
                    >
                        <img
                            src={`https://cdn.akamai.steamstatic.com/steam/apps/${game.appId}/header.jpg`}
                            alt={game.name}
                            loading="lazy"
                            className="h-full w-full object-cover"
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}

export function GameMarquee() {
    return (
        <section
            className="w-full overflow-hidden py-14"
            style={{
                maskImage:
                    "linear-gradient(to right, transparent 0%, black 8%, black 92%, transparent 100%)",
                WebkitMaskImage:
                    "linear-gradient(to right, transparent 0%, black 8%, black 92%, transparent 100%)",
            }}
        >
            <div className="flex flex-col gap-3">
                <MarqueeTrack games={ROW_1} />
                <MarqueeTrack games={ROW_2} reverse />
            </div>
        </section>
    );
}
