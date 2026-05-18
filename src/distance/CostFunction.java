package distance;

import graph.AttributedGraph;
import graph.Edge;
import graph.Node;

import java.util.Objects;

public final class CostFunction {
    public static final class Weights {
        public final double wnr;
        public final double wni;
        public final double wnd;
        public final double wbi;
        public final double wbd;
        public final double wns;
        public final double wbs;

        public Weights(double wnr, double wni, double wnd, double wbi, double wbd, double wns, double wbs) {
            double sum = wnr + wni + wnd + wbi + wbd + wns + wbs;
            if (sum <= 0.0) {
                throw new IllegalArgumentException("Weights must have a positive sum");
            }
            this.wnr = wnr / sum;
            this.wni = wni / sum;
            this.wnd = wnd / sum;
            this.wbi = wbi / sum;
            this.wbd = wbd / sum;
            this.wns = wns / sum;
            this.wbs = wbs / sum;
        }

        public static Weights defaultWeights() {
            return new Weights(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        }
    }

    private final Weights weights;

    public CostFunction(Weights weights) {
        this.weights = Objects.requireNonNull(weights, "weights");
    }

    public Weights getWeights() {
        return weights;
    }

    public double nodeRecognitionCost(AttributedGraph source, Node sourceNode, AttributedGraph target, Node targetNode) {
        double degreeDifference = Math.abs(source.degree(sourceNode.getId()) - target.degree(targetNode.getId()));
        double maxDegree = Math.max(1, Math.max(source.degree(sourceNode.getId()), target.degree(targetNode.getId())));
        double structuralMismatch = degreeDifference / maxDegree;

        double attributeMismatch = attributeMismatch(sourceNode, targetNode);
        return weights.wnr * clamp01((structuralMismatch + attributeMismatch) / 2.0);
    }

    public double nodeInsertionCost(Node node) {
        return weights.wni;
    }

    public double nodeDeletionCost(Node node) {
        return weights.wnd;
    }

    public double branchInsertionCost(Edge edge) {
        return weights.wbi;
    }

    public double branchDeletionCost(Edge edge) {
        return weights.wbd;
    }

    public double nodeSubstitutionCost(Node sourceNode, Node targetNode) {
        if (sourceNode.getLabel().equals(targetNode.getLabel())) {
            return 0.0;
        }
        return weights.wns;
    }

    public double branchSubstitutionCost(Edge sourceEdge, Edge targetEdge) {
        if (sourceEdge.getLabel().equals(targetEdge.getLabel())) {
            return 0.0;
        }
        return weights.wbs;
    }

    public double minNodeOpCost(Node sourceNode, AttributedGraph target) {
        double min = nodeDeletionCost(sourceNode);
        for (Node targetNode : target.getNodes()) {
            double candidate = weights.wnr * 0.5
                    + weights.wns * nodeSubstitutionCost(sourceNode, targetNode)
                    + weights.wnd * 0.0
                    + weights.wni * 0.0;
            min = Math.min(min, candidate);
        }
        return min;
    }

    private double attributeMismatch(Node sourceNode, Node targetNode) {
        if (sourceNode.getAttributes().isEmpty() && targetNode.getAttributes().isEmpty()) {
            return 0.0;
        }
        int total = Math.max(1, Math.max(sourceNode.getAttributes().size(), targetNode.getAttributes().size()));
        int different = 0;
        for (String key : sourceNode.getAttributes().keySet()) {
            String left = sourceNode.getAttributes().get(key);
            String right = targetNode.getAttributes().get(key);
            if (!Objects.equals(left, right)) {
                different++;
            }
        }
        for (String key : targetNode.getAttributes().keySet()) {
            if (!sourceNode.getAttributes().containsKey(key)) {
                different++;
            }
        }
        return clamp01((double) different / total);
    }

    private double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}