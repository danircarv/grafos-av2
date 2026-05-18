package distance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DistanceResult {
    private final double distance;
    private final Map<Integer, Integer> mapping;
    private final List<String> operations;

    public DistanceResult(double distance, Map<Integer, Integer> mapping, List<String> operations) {
        this.distance = distance;
        this.mapping = Collections.unmodifiableMap(new LinkedHashMap<>(mapping));
        this.operations = List.copyOf(operations);
    }

    public double getDistance() {
        return distance;
    }

    public Map<Integer, Integer> getMapping() {
        return mapping;
    }

    public List<String> getOperations() {
        return operations;
    }

    @Override
    public String toString() {
        return "DistanceResult{" +
                "distance=" + distance +
                ", mapping=" + mapping +
                ", operations=" + operations +
                '}';
    }
}