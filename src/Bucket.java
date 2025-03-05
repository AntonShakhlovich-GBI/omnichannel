import java.util.LinkedList;

public class Bucket {
    public TIER getTier() {
        return tier;
    }

    private final TIER tier;
    private final int size;
    private final LinkedList<Product> products = new LinkedList<>();

    public Bucket(TIER tier, int size) {
        this.tier = tier;
        this.size = size;
    }

    public void addProduct(Product product) {
        product.setTier(tier);
        products.add(product);
    }

    public void addProductFirst(Product product) {
        product.setTier(tier);
        products.addFirst(product);
    }

    public Product getLastProductAndRemove() {
        return products.removeLast();
    }

    public Product squeezeProduct(Product product) {
        product.setTier(tier);
        products.addFirst(product);
        if (products.size() > size) {
            return products.removeLast();
        }
        return null;
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public boolean isFull() {
        return products.size() == size;
    }

    public LinkedList<Product> getProducts() {
        return products;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "tier=" + tier +
                ", size=" + size +
                ", products=" + products +
                '}';
    }
}
