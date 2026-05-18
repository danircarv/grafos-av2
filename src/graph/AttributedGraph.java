package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class AttributedGraph {
    private final String id;
    private final String classLabel;
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final Map<Integer, Node> nodeIndex;
    private final Map<String, Edge> edgeIndex;

    private AttributedGraph(String id, String classLabel, List<Node> nodes, List<Edge> edges) {
        this.id = id == null ? "" : id;
        this.classLabel = classLabel == null ? "" : classLabel;
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
        this.nodeIndex = new LinkedHashMap<>();
        for (Node node : nodes) {
            nodeIndex.put(node.getId(), node);
        }
        this.edgeIndex = new LinkedHashMap<>();
        for (Edge edge : edges) {
            edgeIndex.put(edge.key(), edge);
        }
    }

    public String getId() {
        return id;
    }

    public String getClassLabel() {
        return classLabel;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int edgeCount() {
        return edges.size();
    }

    public Optional<Node> nodeById(int id) {
        return Optional.ofNullable(nodeIndex.get(id));
    }

    public Optional<Edge> edgeBetween(int firstId, int secondId) {
        int sourceId = Math.min(firstId, secondId);
        int targetId = Math.max(firstId, secondId);
        return Optional.ofNullable(edgeIndex.get(sourceId + "-" + targetId));
    }

    public boolean containsEdge(int firstId, int secondId) {
        return edgeBetween(firstId, secondId).isPresent();
    }

    public int degree(int nodeId) {
        int degree = 0;
        for (Edge edge : edges) {
            if (edge.getSourceId() == nodeId || edge.getTargetId() == nodeId) {
                degree++;
            }
        }
        return degree;
    }

    public Set<Integer> nodeIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(nodeIndex.keySet()));
    }

    public List<Node> nodesSortedByDegreeDescending() {
        List<Node> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator
                .comparingInt((Node node) -> degree(node.getId()))
                .reversed()
                .thenComparingInt(Node::getId));
        return sorted;
    }

    public AttributedGraph copyWithId(String newId) {
        return builder(newId, classLabel).addAllNodes(nodes).addAllEdges(edges).build();
    }

    public Builder toBuilder() {
        return builder(id, classLabel).addAllNodes(nodes).addAllEdges(edges);
    }

    public static Builder builder(String id, String classLabel) {
        return new Builder(id, classLabel);
    }

    public static final class Builder {
        private final String id;
        private final String classLabel;
        private final List<Node> nodes = new ArrayList<>();
        private final List<Edge> edges = new ArrayList<>();

        private Builder(String id, String classLabel) {
            this.id = id == null ? "" : id;
            this.classLabel = classLabel == null ? "" : classLabel;
        }

        public Builder addNode(Node node) {
            nodes.add(Objects.requireNonNull(node, "node"));
            return this;
        }

        public Builder addEdge(Edge edge) {
            edges.add(Objects.requireNonNull(edge, "edge"));
            return this;
        }

        public Builder addAllNodes(List<Node> nodes) {
            for (Node node : nodes) {
                addNode(node);
            }
            return this;
        }

        public Builder addAllEdges(List<Edge> edges) {
            for (Edge edge : edges) {
                addEdge(edge);
            }
            return this;
        }

        public AttributedGraph build() {
            nodes.sort(Comparator.comparingInt(Node::getId));
            edges.sort(Comparator.comparingInt(Edge::getSourceId).thenComparingInt(Edge::getTargetId));
            return new AttributedGraph(id, classLabel, nodes, edges);
        }
    }
}