package pricing;

import exceptions.DistanceNotDefinedException;

import java.util.HashMap;
import java.util.Map;

public class DistanceTable {
    private final Map<String, Integer> km = new HashMap<>();

    public DistanceTable add(String source, String destination, int distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }
        km.put(key(source, destination), distanceKm);
        return this;
    }

    public int distanceBetween(String source, String destination) throws DistanceNotDefinedException {
        if (source.equalsIgnoreCase(destination)) {
            return 0;
        }
        Integer distance = km.get(key(source, destination));
        if (distance == null) {
            throw new DistanceNotDefinedException(source, destination);
        }
        return distance;
    }

    private static String key(String source, String destination) {
        String a = source.trim().toLowerCase();
        String b = destination.trim().toLowerCase();
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    public static DistanceTable defaultTable() {
        return new DistanceTable()
                .add("Faisal", "Maadi", 12)
                .add("Maadi", "Dokki", 8)
                .add("Maadi", "Nasr City", 15)
                .add("Maadi", "Heliopolis", 17)
                .add("Dokki", "Faisal", 7)
                .add("Dokki", "Nasr City", 16)
                .add("Dokki", "Heliopolis", 18)
                .add("Faisal", "Nasr City", 20)
                .add("Faisal", "Heliopolis", 22)
                .add("Nasr City", "Heliopolis", 6);
    }
}
