package app;

import dataset.Dataset;
import dataset.GraphSample;
import dataset.ProtobufDatasetWriter;
import dataset.SyntheticDatasetGenerator;
import distance.DistanceResult;
import distance.GraphEditDistance;
import graph.AttributedGraph;
import graph.Edge;
import graph.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VisualizerExporter {
    public static void main(String[] args) throws Exception {
        SyntheticDatasetGenerator generator = new SyntheticDatasetGenerator();
        GraphEditDistance distance = new GraphEditDistance();
        Dataset dataset = generator.generate(5, 42L);

        Files.createDirectories(Path.of("out"));
        new ProtobufDatasetWriter().write(dataset, Path.of("out/dataset.pb"));
        String jsonData = buildJson(dataset, generator, distance);
        Files.writeString(Path.of("out/visualizer_data.json"), jsonData);
        
        System.out.println("Dataset written to out/dataset.pb");
        System.out.println("Data exported to out/visualizer_data.json");
    }

    public static String buildJson(Dataset dataset, SyntheticDatasetGenerator generator, GraphEditDistance distance) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        json.append("  \"prototypes\": {\n");
        String[] labels = {"b", "d", "h", "k"};
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            AttributedGraph g = generator.prototype(label);
            json.append("    \"").append(label).append("\": ");
            appendGraphJson(json, g, 4);
            if (i < labels.length - 1) json.append(",");
            json.append("\n");
        }
        json.append("  },\n");

        json.append("  \"samples\": [\n");
        int count = 0;
        int total = dataset.getSamples().size();
        for (GraphSample sample : dataset.getSamples()) {
            json.append("    {\n");
            json.append("      \"id\": \"").append(sample.getSampleId()).append("\",\n");
            json.append("      \"expected\": \"").append(sample.getClassLabel()).append("\",\n");
            json.append("      \"graph\": ");
            appendGraphJson(json, sample.getGraph(), 6);
            json.append(",\n");

            json.append("      \"results\": [\n");
            for (int i = 0; i < labels.length; i++) {
                String label = labels[i];
                DistanceResult res = distance.distance(sample.getGraph(), generator.prototype(label));
                json.append("        { \"label\": \"").append(label).append("\", \"distance\": ").append(res.getDistance()).append(" }");
                if (i < labels.length - 1) json.append(",");
                json.append("\n");
            }
            json.append("      ]\n");
            json.append("    }");
            if (++count < total) json.append(",");
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}");
        return json.toString();
    }

    private static void appendGraphJson(StringBuilder json, AttributedGraph g, int indent) {
        String space = " ".repeat(indent);
        json.append("{\n");
        json.append(space).append("  \"nodes\": [\n");
        var nodes = g.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            json.append(space).append("    { \"id\": ").append(n.getId()).append(", \"label\": \"").append(n.getLabel()).append("\" }");
            if (i < nodes.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append(space).append("  ],\n");
        json.append(space).append("  \"edges\": [\n");
        var edges = g.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            json.append(space).append("    { \"source\": ").append(e.getSourceId()).append(", \"target\": ").append(e.getTargetId()).append(", \"label\": \"").append(e.getLabel()).append("\" }");
            if (i < edges.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append(space).append("  ]\n");
        json.append(space).append("}");
    }
}
