import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // Settings
        int pageSize = 32;
        double originalDistributionRate = 0.2;
        double multiplier = 5;
        double maxVisibility = 0.9;

        ArrayList<Product> products = generateProducts(pageSize, originalDistributionRate);

        long startTime = System.nanoTime();

        ArrayList<Product> finalProductList = new ArrayList<>(pageSize);
        while (finalProductList.size() < pageSize) {
            finalProductList.add(null); // Placeholder elements
        }

        Map<TIER, Bucket> buckets = getBuckets(products);

        for (Product product : products) {
            System.out.println(product);
        }

        int originalP1Score = getScore(products, TYPE.P1);
        int originalP3Score = getScore(products, TYPE.P3);
        int overallScore = originalP1Score + originalP3Score;
        double originalVisibility = (double) originalP1Score / overallScore;

        int currentP3Score = originalP3Score;
        double currentVisibility = 1 - (double) currentP3Score / overallScore;
        double previousVisibility = 0;
        double targetVisibility = Math.min(currentVisibility * multiplier, maxVisibility);

        while (currentVisibility < targetVisibility) {
            if (previousVisibility == currentVisibility) {
                break;
            }
            previousVisibility = currentVisibility;
            Product lastProductInTopTier = getLastProductInTopTier(buckets);
            pushProductToNextTier(lastProductInTopTier, buckets);
            currentP3Score = getScore(products, TYPE.P3);
            currentVisibility = 1 - (double) currentP3Score / overallScore;
        }

        fillP3Product(finalProductList, buckets);
        //fillP1Product(finalProductList, products);

        System.out.println(originalP1Score);
        System.out.println(originalP3Score);
        System.out.println(originalVisibility);
        System.out.println(currentVisibility);

        for (int i = 0; i < finalProductList.size(); i++) {
            System.out.println(i + ": " + finalProductList.get(i));
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Execution time: " + duration / 1_000_000 + " ms");
    }

    private static ArrayList<Product> generateProducts(int size, double percentage) {
        ArrayList<Product> products = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TYPE type = Math.random() < percentage ? TYPE.P1 : TYPE.P3;
            Product product = new Product(i, "product" + i, type);
            products.add(product);
        }
        return products;
    }

    private static int getScore(ArrayList<Product> products, TYPE productType) {
        int sum = 0;
        for (Product p : products) {
            if (p.getType() == productType) {
                sum += p.getScore();
            }
        }
        return sum;
    }

    private static Map<TIER, Bucket> getBuckets(ArrayList<Product> products) {
        EnumMap<TIER, Bucket> buckets = products.stream()
                .collect(Collectors.groupingBy(
                        Product::getTier, () -> new EnumMap<>(TIER.class), Collectors.counting()
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new Bucket(e.getKey(), e.getValue().intValue()),
                        (e1, e2) -> e1, () -> new EnumMap<>(TIER.class)
                ));


        for (Product product : products) {
            if (product.getType() == TYPE.P3) {
                buckets.get(product.getTier()).addProduct(product);
            }
        }

        return buckets;
    }

    private static Product getLastProductInTopTier(Map<TIER, Bucket> buckets) {
        for (Map.Entry<TIER, Bucket> entry : buckets.entrySet())
            if (!entry.getValue().isEmpty()) {
                return entry.getValue().getLastProductAndRemove();
            }
        return null;
    }

    private static void pushProductToNextTier(Product product, Map<TIER, Bucket> buckets) {
        TIER nextTier = product.getTier().next();
        Bucket bucket = buckets.get(nextTier);
        if (bucket == null) {
            throw new IllegalArgumentException("No bucket found for tier " + nextTier);
        }
        Product substitutedProduct = bucket.squeezeProduct(product);
        if (substitutedProduct != null) {
            pushProductToNextTier(substitutedProduct, buckets);
        }
    }

    private static void fillP3Product(ArrayList<Product> finalProductList, Map<TIER, Bucket> buckets) {
        for (Bucket bucket : buckets.values()) {
            int productBucketIndex = 0;
            for (Product product : bucket.getProducts()) {
                int indexOffset = product.getTier().startIndex;
                int resultIndex = Math.max(product.getOriginalIndex(), productBucketIndex + indexOffset);
                finalProductList.set(resultIndex, product);
                productBucketIndex++;
            }
        }
    }

    private static void fillP1Product(ArrayList<Product> finalProductList, ArrayList<Product> products) {
        int productIndex = 0;

        ArrayList<Product> p1products = new ArrayList<>();

        for (Product product : products) {
            if (product.getType() == TYPE.P1) {
                p1products.add(product);
            }
        }

        for (int i = 0; i < finalProductList.size(); i++) {
            if (finalProductList.get(i) == null && productIndex < products.size()) {
                finalProductList.set(i, p1products.get(productIndex));
                productIndex++;
            }
        }
    }

    public static int calculateMinScore(int totalProducts) {
        int minScore = 0;
        int remainingProducts = totalProducts;

        // Process tiers from the lowest to the highest
        for (TIER tier : TIER.values()) {
            int tierCapacity = (tier.endIndex - tier.startIndex + 1); // Number of slots in this tier

            if (remainingProducts <= 0) break; // Stop if all products are placed

            // Determine how many products can be placed in this tier
            int productsInThisTier = Math.min(remainingProducts, tierCapacity);

            // Calculate score contribution from this tier
            minScore += productsInThisTier * tier.score;

            // Reduce the remaining products
            remainingProducts -= productsInThisTier;
        }

        return minScore;
    }
}