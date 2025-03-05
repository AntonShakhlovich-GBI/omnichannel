import java.util.Arrays;

public class Product implements Cloneable {
    private final int originalIndex;
    private int currentIndex;
    private final String name;
    private TIER tier;
    private final TYPE type;

    public Product(int originalIndex, String name, TYPE type) {
        this.originalIndex = originalIndex;
        this.currentIndex = originalIndex;
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

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
        this.tier = Arrays.stream(TIER.values())
                .filter(tier -> tier.startIndex <= currentIndex && tier.endIndex >= currentIndex)
                .findAny().orElse(tier);
    }

    @Override
    public String toString() {
        return "Product{" +
                "originalIndex=" + originalIndex +
                ", " + tier +
                ", " + type +
                ", score=" + getScore() +
                '}';
    }

    @Override
    public Product clone() {
        try {
            Product clone = (Product) super.clone();
            clone.tier = this.tier;
            clone.currentIndex = this.currentIndex;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static final String RESET = "\u001B[0m";  // Reset color
    private static final String RED = "\u001B[31m";   // Red
    private static final String GREEN = "\u001B[32m"; // Green

    public String toColoredString() {
        if (currentIndex < originalIndex) {
            return GREEN + this + RESET;
        } else if (currentIndex > originalIndex) {
            return RED + this + RESET;
        }
        return this.toString();
    }
}
