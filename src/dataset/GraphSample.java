package dataset;

import graph.AttributedGraph;

import java.util.Objects;

public final class GraphSample {
    private final String sampleId;
    private final String classLabel;
    private final double noiseLevel;
    private final AttributedGraph graph;

    public GraphSample(String sampleId, String classLabel, double noiseLevel, AttributedGraph graph) {
        this.sampleId = sampleId == null ? "" : sampleId;
        this.classLabel = classLabel == null ? "" : classLabel;
        this.noiseLevel = noiseLevel;
        this.graph = Objects.requireNonNull(graph, "graph");
    }

    public String getSampleId() {
        return sampleId;
    }

    public String getClassLabel() {
        return classLabel;
    }

    public double getNoiseLevel() {
        return noiseLevel;
    }

    public AttributedGraph getGraph() {
        return graph;
    }
}