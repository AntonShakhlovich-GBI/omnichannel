import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // Settings
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter page size (int): ");
        int pageSize = scanner.nextInt();

        System.out.print("Enter original distribution rate (double): ");
        double originalDistributionRate = scanner.nextDouble();

        System.out.print("Enter multiplier (double): ");
        double multiplier = scanner.nextDouble();

        System.out.print("Enter visibility limit (double): ");
        double visibilityLimit = scanner.nextDouble();

        ArrayList<Product> products = generateProducts(pageSize, originalDistributionRate);
        for (Product product : products) {
            System.out.println(product.toColoredString());
        }

        ArrayList<Product> finalProductList = new ArrayList<>(pageSize);
        while (finalProductList.size() < pageSize) {
            finalProductList.add(null); // Placeholder elements
        }

        int originalP1Score = getScore(products, TYPE.P1);
        int originalP3Score = getScore(products, TYPE.P3);
        int overallScore = originalP1Score + originalP3Score;
        double originalVisibility = (double) originalP1Score / overallScore;
        double targetVisibility = Math.min(originalVisibility * multiplier, visibilityLimit);

        long startTime = System.nanoTime();

        // Check If Reordering Algorithm need to be triggered
        Map<TIER, Bucket> bucketsWithMinimalScore = generateP3BucketsMinimalScore(products);
        int minimalP3Score = 0;
        for (Bucket bucket : bucketsWithMinimalScore.values()) {
            minimalP3Score += getScore(bucket.getProducts(), TYPE.P3);
        }

        double maximumVisibility = 1 - (double) minimalP3Score / overallScore;

        Map<TIER, Bucket> buckets;
        if (maximumVisibility < targetVisibility) {
            buckets = bucketsWithMinimalScore;
            System.out.println("Burying Algorithm triggered.");
        } else {
            buckets = generateP3BucketsSmart(products, originalP3Score, originalVisibility, targetVisibility, overallScore);
            System.out.println("Smart Algorithm triggered.");
        }

        fillP3Product(finalProductList, buckets);
        fillP1Product(finalProductList, products);

        System.out.println("Original P1 Score: " + originalP1Score);
        System.out.println("Original P3 Score: " + originalP3Score);
        System.out.println("Original Visibility: " + originalVisibility);
        System.out.println("Multiplier: " + multiplier);
        System.out.println("Target Visibility: " + targetVisibility);

        int finalP1Score = getScore(finalProductList, TYPE.P1);
        int finalP3Score = getScore(finalProductList, TYPE.P3);
        int finalOverallScore = finalP1Score + finalP3Score;
        double finalVisibility = (double) finalP1Score / finalOverallScore;

        System.out.println("Final P1 Score: " + finalP1Score);
        System.out.println("Final P3 Score: " + finalP3Score);
        System.out.println("Final Visibility: " + finalVisibility);

        for (int i = 0; i < finalProductList.size(); i++) {
            System.out.println(i + ": " + finalProductList.get(i).toColoredString());
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Execution time: " + duration / 1_000_000 + " ms");
    }

    private static Map<TIER, Bucket> generateP3BucketsMinimalScore(ArrayList<Product> products) {
        Map<TIER, Bucket> buckets = generateBuckets(products);
        Bucket currentBucket = buckets.values().stream().max(Comparator.comparingInt(b -> b.getTier().ordinal())).orElse(null);

        for (int i = products.size() - 1; i >= 0; i--) {
            Product product = products.get(i);
            if (product.getType() == TYPE.P3) {
                if (currentBucket.isFull()) {
                    currentBucket = buckets.get(currentBucket.getTier().previous());
                }
                Product clonedProduct = product.clone();
                currentBucket.addProductFirst(clonedProduct);
            }
        }
        return buckets;
    }

    private static Map<TIER, Bucket> generateP3BucketsSmart(ArrayList<Product> products, int originalP3Score, double originalVisibility, double targetVisibility, double overallScore) {
        Map<TIER, Bucket> buckets = generateBuckets(products);

        for (Product product : products) {
            if (product.getType() == TYPE.P3) {
                buckets.get(product.getTier()).addProduct(product);
            }
        }

        int currentP3Score = originalP3Score;
        double currentVisibility = originalVisibility;
        double previousVisibility = 0;

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

        return buckets;
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

    private static int getScore(List<Product> products, TYPE productType) {
        int sum = 0;
        for (Product p : products) {
            if (p.getType() == productType) {
                sum += p.getScore();
            }
        }
        return sum;
    }

    private static Map<TIER, Bucket> generateBuckets(ArrayList<Product> products) {

        return products.stream()
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
                product.setCurrentIndex(resultIndex);
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
                Product p1product = p1products.get(productIndex);
                p1product.setCurrentIndex(i);
                finalProductList.set(i, p1product);
                productIndex++;
            }
        }
    }
}