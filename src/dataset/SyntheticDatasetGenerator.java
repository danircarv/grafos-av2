package dataset;

import graph.AttributedGraph;
import graph.Edge;
import graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class SyntheticDatasetGenerator {
    private static final List<String> CLASS_LABELS = List.of("b", "d", "h", "k");
    private static final List<String> NODE_VOCABULARY = List.of(
            "stem", "loop", "junction", "curve", "tail", "diagonal_up", "diagonal_down", "hook", "terminal"
    );
    private static final List<String> EDGE_VOCABULARY = List.of(
            "support", "curve", "branch", "stem", "arc", "diagonal_up", "diagonal_down"
    );

    public Dataset generate(int samplesPerClass, long seed) {
        if (samplesPerClass <= 0) {
            throw new IllegalArgumentException("samplesPerClass must be positive");
        }
        Random random = new Random(seed);
        List<GraphSample> samples = new ArrayList<>();
        for (String classLabel : CLASS_LABELS) {
            AttributedGraph canonical = prototype(classLabel);
            for (int index = 0; index < samplesPerClass; index++) {
                double noiseLevel = 0.05 + (0.35 * random.nextDouble());
                AttributedGraph noisy = mutate(canonical, noiseLevel, random, classLabel, index);
                samples.add(new GraphSample(classLabel + "_" + index, classLabel, noiseLevel, noisy));
            }
        }
        return new Dataset(samples);
    }

    public AttributedGraph prototype(String classLabel) {
        return switch (classLabel) {
            case "b" -> prototypeB();
            case "d" -> prototypeD();
            case "h" -> prototypeH();
            case "k" -> prototypeK();
            default -> throw new IllegalArgumentException("Unknown class label: " + classLabel);
        };
    }

    private AttributedGraph mutate(AttributedGraph canonical, double noiseLevel, Random random, String classLabel, int index) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : canonical.getNodes()) {
            nodes.add(node);
        }
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : canonical.getEdges()) {
            edges.add(edge);
        }

        int mutations = Math.max(1, (int) Math.round(noiseLevel * (nodes.size() + edges.size())));
        for (int mutation = 0; mutation < mutations; mutation++) {
            int choice = random.nextInt(5);
            if (choice == 0) {
                substituteNodeLabel(nodes, random);
            } else if (choice == 1) {
                substituteEdgeLabel(edges, random);
            } else if (choice == 2) {
                insertNodeOnEdge(nodes, edges, random);
            } else if (choice == 3) {
                deleteLeafNode(nodes, edges, random);
            } else {
                addRandomEdge(nodes, edges, random);
            }
        }

        if (nodes.isEmpty()) {
            return canonical.copyWithId(classLabel + "_recovered_" + index);
        }

        return rebuild(classLabel + "_noise_" + index, classLabel, nodes, edges);
    }

    private AttributedGraph rebuild(String id, String classLabel, List<Node> nodes, List<Edge> edges) {
        AttributedGraph.Builder builder = AttributedGraph.builder(id, classLabel);
        for (Node node : nodes) {
            builder.addNode(node);
        }
        for (Edge edge : edges) {
            if (containsNode(nodes, edge.getSourceId()) && containsNode(nodes, edge.getTargetId())) {
                builder.addEdge(edge);
            }
        }
        return builder.build();
    }

    private void substituteNodeLabel(List<Node> nodes, Random random) {
        if (nodes.isEmpty()) {
            return;
        }
        int index = random.nextInt(nodes.size());
        Node node = nodes.get(index);
        nodes.set(index, node.withLabel(randomLabelExcluding(NODE_VOCABULARY, node.getLabel(), random)));
    }

    private void substituteEdgeLabel(List<Edge> edges, Random random) {
        if (edges.isEmpty()) {
            return;
        }
        int index = random.nextInt(edges.size());
        Edge edge = edges.get(index);
        edges.set(index, edge.withLabel(randomLabelExcluding(EDGE_VOCABULARY, edge.getLabel(), random)));
    }

    private void insertNodeOnEdge(List<Node> nodes, List<Edge> edges, Random random) {
        if (edges.isEmpty()) {
            return;
        }
        int edgeIndex = random.nextInt(edges.size());
        Edge edge = edges.remove(edgeIndex);
        int newNodeId = nextNodeId(nodes);
        Node newNode = new Node(newNodeId, randomLabelFrom(NODE_VOCABULARY, random));
        nodes.add(newNode);
        edges.add(new Edge(edge.getSourceId(), newNodeId, randomLabelFrom(EDGE_VOCABULARY, random)));
        edges.add(new Edge(newNodeId, edge.getTargetId(), randomLabelFrom(EDGE_VOCABULARY, random)));
    }

    private void deleteLeafNode(List<Node> nodes, List<Edge> edges, Random random) {
        List<Node> leaves = new ArrayList<>();
        for (Node node : nodes) {
            int degree = degree(node.getId(), edges);
            if (degree <= 1) {
                leaves.add(node);
            }
        }
        if (leaves.isEmpty()) {
            return;
        }
        Node victim = leaves.get(random.nextInt(leaves.size()));
        nodes.removeIf(node -> node.getId() == victim.getId());
        edges.removeIf(edge -> edge.getSourceId() == victim.getId() || edge.getTargetId() == victim.getId());
    }

    private void addRandomEdge(List<Node> nodes, List<Edge> edges, Random random) {
        if (nodes.size() < 2) {
            return;
        }
        Node first = nodes.get(random.nextInt(nodes.size()));
        Node second = nodes.get(random.nextInt(nodes.size()));
        if (first.getId() == second.getId()) {
            return;
        }
        if (containsEdge(edges, first.getId(), second.getId())) {
            return;
        }
        edges.add(new Edge(first.getId(), second.getId(), randomLabelFrom(EDGE_VOCABULARY, random)));
    }

    private int degree(int nodeId, List<Edge> edges) {
        int degree = 0;
        for (Edge edge : edges) {
            if (edge.getSourceId() == nodeId || edge.getTargetId() == nodeId) {
                degree++;
            }
        }
        return degree;
    }

    private boolean containsNode(List<Node> nodes, int nodeId) {
        for (Node node : nodes) {
            if (node.getId() == nodeId) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEdge(List<Edge> edges, int firstId, int secondId) {
        int sourceId = Math.min(firstId, secondId);
        int targetId = Math.max(firstId, secondId);
        for (Edge edge : edges) {
            if (edge.getSourceId() == sourceId && edge.getTargetId() == targetId) {
                return true;
            }
        }
        return false;
    }

    private int nextNodeId(List<Node> nodes) {
        int max = 0;
        for (Node node : nodes) {
            if (node.getId() > max) {
                max = node.getId();
            }
        }
        return max + 1;
    }

    private String randomLabelFrom(List<String> labels, Random random) {
        return labels.get(random.nextInt(labels.size()));
    }

    private String randomLabelExcluding(List<String> labels, String excluded, Random random) {
        if (labels.size() == 1) {
            return labels.get(0);
        }
        String chosen;
        do {
            chosen = randomLabelFrom(labels, random);
        } while (Objects.equals(chosen, excluded));
        return chosen;
    }

    private AttributedGraph prototypeB() {
        AttributedGraph.Builder builder = AttributedGraph.builder("prototype_b", "b");
        builder.addNode(new Node(1, "stem"));
        builder.addNode(new Node(2, "loop"));
        builder.addNode(new Node(3, "loop"));
        builder.addNode(new Node(4, "tail"));
        builder.addEdge(new Edge(1, 2, "stem"));
        builder.addEdge(new Edge(2, 3, "curve"));
        builder.addEdge(new Edge(3, 4, "tail"));
        builder.addEdge(new Edge(2, 4, "branch"));
        return builder.build();
    }

    private AttributedGraph prototypeD() {
        AttributedGraph.Builder builder = AttributedGraph.builder("prototype_d", "d");
        builder.addNode(new Node(1, "stem"));
        builder.addNode(new Node(2, "loop"));
        builder.addNode(new Node(3, "loop"));
        builder.addNode(new Node(4, "tail"));
        builder.addEdge(new Edge(1, 2, "stem"));
        builder.addEdge(new Edge(2, 3, "curve"));
        builder.addEdge(new Edge(1, 4, "branch"));
        builder.addEdge(new Edge(2, 4, "tail"));
        return builder.build();
    }

    private AttributedGraph prototypeH() {
        AttributedGraph.Builder builder = AttributedGraph.builder("prototype_h", "h");
        builder.addNode(new Node(1, "stem"));
        builder.addNode(new Node(2, "junction"));
        builder.addNode(new Node(3, "arch"));
        builder.addNode(new Node(4, "leg"));
        builder.addEdge(new Edge(1, 2, "stem"));
        builder.addEdge(new Edge(2, 3, "arch"));
        builder.addEdge(new Edge(2, 4, "branch"));
        return builder.build();
    }

    private AttributedGraph prototypeK() {
        AttributedGraph.Builder builder = AttributedGraph.builder("prototype_k", "k");
        builder.addNode(new Node(1, "stem"));
        builder.addNode(new Node(2, "junction"));
        builder.addNode(new Node(3, "diagonal_up"));
        builder.addNode(new Node(4, "diagonal_down"));
        builder.addEdge(new Edge(1, 2, "stem"));
        builder.addEdge(new Edge(2, 3, "diagonal_up"));
        builder.addEdge(new Edge(2, 4, "diagonal_down"));
        return builder.build();
    }
}