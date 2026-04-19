import { IndoDBHero } from "@/components/layout/IndoDBHero";
import { GameMarquee } from "@/components/ui/GameMarquee";
import { GameLists } from "@/components/layout/GameLists";

export default function Home() {
    return (
        <>
            <IndoDBHero />
            <GameMarquee />
            <GameLists />
        </>
    );
}
