public class Product {
    private final int originalIndex;
    private int currentIndex;
    private final String name;
    private TIER tier;
    private final TYPE type;

    public Product(int originalIndex, String name, TYPE type) {
        this.originalIndex = originalIndex;
        this.name = name;
        this.tier = TIER.getTIER(originalIndex);
        this.type = type;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public String getName() {
        return name;
    }

    public TIER getTier() {
        return tier;
    }

    public TYPE getType() {
        return type;
    }

    public Integer getScore() {
        return tier.score;
    }

    public void setTier(TIER tier) {
        this.tier = tier;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    @Override
    public String toString() {
        return "Product{" +
                "originalIndex=" + originalIndex +
                ", name='" + name + '\'' +
                ", tier=" + tier +
                ", type=" + type +
                ", score=" + getScore() +
                '}';
    }
}
