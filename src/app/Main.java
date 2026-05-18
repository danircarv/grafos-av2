package app;

import dataset.Dataset;
import dataset.GraphSample;
import dataset.ProtobufDatasetWriter;
import dataset.SyntheticDatasetGenerator;
import distance.DistanceResult;
import distance.GraphEditDistance;
import graph.AttributedGraph;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Main {
    public static void main(String[] args) throws Exception {
        SyntheticDatasetGenerator generator = new SyntheticDatasetGenerator();
        GraphEditDistance distance = new GraphEditDistance();

        if (args.length > 0 && args[0].equalsIgnoreCase("generate-dataset")) {
            int samplesPerClass = args.length > 1 ? Integer.parseInt(args[1]) : 250;
            Path output = Path.of(args.length > 2 ? args[2] : "out/dataset.pb");
            Dataset dataset = generator.generate(samplesPerClass, 42L);
            new ProtobufDatasetWriter().write(dataset, output);
            System.out.println("Dataset written to " + output.toAbsolutePath());
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("demo")) {
            runDemo(generator, distance);
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("web")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
            new LocalWebServer(port).start();
            return;
        }

        System.out.println("Usage:");
        System.out.println("  java app.Main demo");
        System.out.println("  java app.Main generate-dataset [samplesPerClass] [output.pb]");
        System.out.println("  java app.Main web [port]");
    }

    private static void runDemo(SyntheticDatasetGenerator generator, GraphEditDistance distance) {
        Map<String, AttributedGraph> prototypes = new LinkedHashMap<>();
        prototypes.put("b", generator.prototype("b"));
        prototypes.put("d", generator.prototype("d"));
        prototypes.put("h", generator.prototype("h"));
        prototypes.put("k", generator.prototype("k"));

        Dataset dataset = generator.generate(3, 7L);
        for (GraphSample sample : dataset.getSamples()) {
            DistanceResult best = null;
            String bestLabel = "";
            for (Map.Entry<String, AttributedGraph> entry : prototypes.entrySet()) {
                DistanceResult result = distance.distance(sample.getGraph(), entry.getValue());
                if (best == null || result.getDistance() < best.getDistance()) {
                    best = result;
                    bestLabel = entry.getKey();
                }
            }
            System.out.println(sample.getSampleId() + " expected=" + sample.getClassLabel() + " predicted=" + bestLabel + " distance=" + best.getDistance());
        }
    }
}