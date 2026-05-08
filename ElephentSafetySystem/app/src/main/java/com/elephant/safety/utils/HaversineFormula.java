package com.elephant.safety.utils;

public class HaversineFormula {

    private static final double EARTH_RADIUS = 6371000; // meters

    // Calculate distance between two points using Haversine formula
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    // Check if user is inside danger zone
    public static boolean isInsideZone(double userLat, double userLon,
                                       double zoneLat, double zoneLon, int radius) {
        double distance = calculateDistance(userLat, userLon, zoneLat, zoneLon);
        return distance <= radius;
    }

    // Get distance to zone boundary
    public static double getDistanceToZone(double userLat, double userLon,
                                           double zoneLat, double zoneLon, int radius) {
        double distanceToCenter = calculateDistance(userLat, userLon, zoneLat, zoneLon);
        return distanceToCenter - radius;
    }
}