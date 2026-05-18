package app;

import dataset.SyntheticDatasetGenerator;
import distance.DistanceResult;
import distance.GraphEditDistance;
import graph.AttributedGraph;

public final class TestCases {
    public static void main(String[] args) {
        SyntheticDatasetGenerator generator = new SyntheticDatasetGenerator();
        GraphEditDistance distance = new GraphEditDistance();

        AttributedGraph b1 = generator.prototype("b");
        AttributedGraph b2 = generator.prototype("b");
        AttributedGraph d = generator.prototype("d");

        DistanceResult same = distance.distance(b1, b2);
        DistanceResult different = distance.distance(b1, d);

        check(same.getDistance() == 0.0, "Expected identical prototypes to have zero distance");
        check(different.getDistance() > same.getDistance(), "Expected different prototypes to have a larger distance");

        AttributedGraph noisyB = generator.generate(1, 11L).getSamples().get(0).getGraph();
        DistanceResult noisyResult = distance.symmetricDistance(noisyB, b1);
        check(noisyResult.getDistance() >= 0.0, "Distance must be non-negative");

        System.out.println("All GED checks passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}