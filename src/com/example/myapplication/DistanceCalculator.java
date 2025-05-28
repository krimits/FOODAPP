package com.example.myapplication;

public class DistanceCalculator {
    public static void main(String[] args) {
        // Η θέση σου
        double userLat = 37.96;
        double userLon = 23.74;
        
        // Τα καταστήματα από το JSON
        Store[] stores = {
            new Store("Burger House", 37.9838, 23.7275),
            new Store("Sushi Zen", 37.975, 23.735),
            new Store("Healthy Bites", 37.9801, 23.7285),
            new Store("Street Gyros", 37.991, 23.7301)
        };
        
        System.out.println("Distance calculation from your location (37.96, 23.74):");
        System.out.println("=========================================================");
        
        for (Store store : stores) {
            double distance = calculateDistance(userLat, userLon, store.latitude, store.longitude);
            String status = distance <= 5.0 ? "✅ SHOULD APPEAR" : "❌ TOO FAR";
            System.out.printf("%-15s: %.2f km %s\n", store.name, distance, status);
        }
    }
    
    // Haversine formula για υπολογισμό απόστασης
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in km
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return R * c;
    }
    
    static class Store {
        String name;
        double latitude;
        double longitude;
        
        Store(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
