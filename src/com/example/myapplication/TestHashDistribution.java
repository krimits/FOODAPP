package com.example.myapplication;

public class TestHashDistribution {
    public static void main(String[] args) {
        String[] storeNames = {
            "Burger House",
            "Sushi Zen", 
            "Healthy Bites",
            "Street Gyros"
        };
        
        int numWorkers = 3; // Υποθέτοντας 3 workers
        
        System.out.println("Hash distribution of stores to workers:");
        System.out.println("=====================================");
        
        for (String storeName : storeNames) {
            int workerId = Math.abs(storeName.hashCode()) % numWorkers;
            System.out.println(storeName + " -> Worker " + workerId + " (hash: " + storeName.hashCode() + ")");
        }
    }
}
