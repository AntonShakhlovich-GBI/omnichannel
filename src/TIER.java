public enum TIER {
    TIER_1(100,0,7),
    TIER_2(67,8,15),
    TIER_3(40,16,23),
    TIER_4(25,24,31),
    TIER_5(17,32,39),
    TIER_6(12,40,55),
    TIER_7(9,56,71),
    TIER_8(7,72,87),
    TIER_9(5,88,103),
    TIER_10(4,104,119);


    public final Integer score;
    public final Integer startIndex;
    public final Integer endIndex;

    TIER(Integer score, Integer startIndex, Integer endIndex) {
        this.startIndex=startIndex;
        this.endIndex=endIndex;
        this.score=score;
    }

    public static TIER getTIER(int index) {
        for (TIER tier : TIER.values()) {
            if (index >= tier.startIndex && index <= tier.endIndex) {
                return tier;
            }
        }
        return null;
    }

    public TIER next() {
        TIER[] tiers = values(); // Get all enum values
        int nextIndex = (this.ordinal() + 1) % tiers.length; // Circular iteration
        return tiers[nextIndex];
    }
}
