package distance;

import graph.AttributedGraph;
import graph.Edge;
import graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class MatchingEngine {
    private final CostFunction costFunction;
    private final AttributeValidator validator;

    public MatchingEngine(CostFunction costFunction, AttributeValidator validator) {
        this.costFunction = Objects.requireNonNull(costFunction, "costFunction");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public DistanceResult match(AttributedGraph source, AttributedGraph target) {
        List<Node> orderedSourceNodes = new ArrayList<>(source.getNodes());
        orderedSourceNodes.sort(Comparator
                .comparingInt((Node node) -> candidateCount(source, target, node))
                .thenComparingInt((Node node) -> source.degree(node.getId()))
                .reversed()
                .thenComparingInt(Node::getId));

        SearchContext context = new SearchContext(source, target, orderedSourceNodes);
        search(0, context, new LinkedHashMap<>(), new LinkedHashSet<>(), 0.0);

        if (context.bestMapping == null) {
            return new DistanceResult(Double.POSITIVE_INFINITY, Map.of(), List.of("no valid configuration"));
        }
        return new DistanceResult(context.bestCost, context.bestMapping, context.bestOperations);
    }

    private void search(int index, SearchContext context, Map<Integer, Integer> mapping,
                        Set<Integer> usedTargets, double currentCost) {
        if (currentCost >= context.bestCost) {
            return;
        }

        double lowerBound = currentCost + optimisticLowerBound(index, context, mapping, usedTargets);
        if (lowerBound >= context.bestCost) {
            return;
        }

        if (index == context.orderedSourceNodes.size()) {
            if (!validator.isValidMapping(context.source, context.target, mapping)) {
                return;
            }
            double totalCost = currentCost + finalCost(context.source, context.target, mapping, usedTargets);
            if (totalCost < context.bestCost) {
                context.bestCost = totalCost;
                context.bestMapping = new LinkedHashMap<>(mapping);
                context.bestOperations = buildOperations(context.source, context.target, mapping, usedTargets, totalCost);
            }
            return;
        }

        Node sourceNode = context.orderedSourceNodes.get(index);

        double deleteCost = costFunction.nodeDeletionCost(sourceNode);
        mapping.put(sourceNode.getId(), -1);
        search(index + 1, context, mapping, usedTargets, currentCost + deleteCost);
        mapping.remove(sourceNode.getId());

        for (Node targetNode : context.target.getNodes()) {
            if (usedTargets.contains(targetNode.getId())) {
                continue;
            }
            double nodeCost = costFunction.nodeRecognitionCost(context.source, sourceNode, context.target, targetNode)
                    + costFunction.nodeSubstitutionCost(sourceNode, targetNode);

            mapping.put(sourceNode.getId(), targetNode.getId());
            usedTargets.add(targetNode.getId());
            search(index + 1, context, mapping, usedTargets, currentCost + nodeCost);
            usedTargets.remove(targetNode.getId());
            mapping.remove(sourceNode.getId());
        }
    }

    private double optimisticLowerBound(int index, SearchContext context, Map<Integer, Integer> mapping, Set<Integer> usedTargets) {
        double bound = 0.0;
        for (int i = index; i < context.orderedSourceNodes.size(); i++) {
            Node sourceNode = context.orderedSourceNodes.get(i);
            double bestNodeCost = costFunction.nodeDeletionCost(sourceNode);
            for (Node targetNode : context.target.getNodes()) {
                if (usedTargets.contains(targetNode.getId())) {
                    continue;
                }
                double candidate = costFunction.nodeRecognitionCost(context.source, sourceNode, context.target, targetNode)
                        + costFunction.nodeSubstitutionCost(sourceNode, targetNode);
                if (candidate < bestNodeCost) {
                    bestNodeCost = candidate;
                }
            }
            bound += bestNodeCost;
        }

        Set<Integer> mappedTargets = new HashSet<>();
        for (Integer targetId : mapping.values()) {
            if (targetId != null && targetId >= 0) {
                mappedTargets.add(targetId);
            }
        }
        for (Node targetNode : context.target.getNodes()) {
            if (!mappedTargets.contains(targetNode.getId()) && !usedTargets.contains(targetNode.getId())) {
                bound += costFunction.nodeInsertionCost(targetNode);
            }
        }

        int remainingSourceEdges = 0;
        for (Edge edge : context.source.getEdges()) {
            if (!mapping.containsKey(edge.getSourceId()) || !mapping.containsKey(edge.getTargetId())) {
                remainingSourceEdges++;
            }
        }
        int remainingTargetEdges = 0;
        for (Edge edge : context.target.getEdges()) {
            if (!mappedTargets.contains(edge.getSourceId()) || !mappedTargets.contains(edge.getTargetId())) {
                remainingTargetEdges++;
            }
        }
        bound += Math.abs(remainingSourceEdges - remainingTargetEdges) * 0.5;
        return bound;
    }

    private double finalCost(AttributedGraph source, AttributedGraph target, Map<Integer, Integer> mapping, Set<Integer> usedTargets) {
        Set<Integer> matchedTargetNodes = new LinkedHashSet<>();
        for (Integer mappedTarget : mapping.values()) {
            if (mappedTarget != null && mappedTarget >= 0) {
                matchedTargetNodes.add(mappedTarget);
            }
        }

        double nodeInsertion = 0.0;
        for (Node targetNode : target.getNodes()) {
            if (!matchedTargetNodes.contains(targetNode.getId())) {
                nodeInsertion += costFunction.nodeInsertionCost(targetNode);
            }
        }

        double branchDeletion = 0.0;
        double branchInsertion = 0.0;
        double branchSubstitution = 0.0;

        Set<String> matchedTargetEdges = new HashSet<>();
        for (Edge sourceEdge : source.getEdges()) {
            Integer mappedSource = mapping.get(sourceEdge.getSourceId());
            Integer mappedTarget = mapping.get(sourceEdge.getTargetId());
            if (mappedSource == null || mappedTarget == null || mappedSource < 0 || mappedTarget < 0) {
                branchDeletion += costFunction.branchDeletionCost(sourceEdge);
                continue;
            }
            Edge targetEdge = target.edgeBetween(mappedSource, mappedTarget).orElse(null);
            if (targetEdge == null) {
                branchDeletion += costFunction.branchDeletionCost(sourceEdge);
                continue;
            }
            matchedTargetEdges.add(targetEdge.key());
            branchSubstitution += costFunction.branchSubstitutionCost(sourceEdge, targetEdge);
        }

        for (Edge targetEdge : target.getEdges()) {
            if (!matchedTargetEdges.contains(targetEdge.key())) {
                branchInsertion += costFunction.branchInsertionCost(targetEdge);
            }
        }

        return nodeInsertion + branchInsertion + branchDeletion + branchSubstitution;
    }

    private List<String> buildOperations(AttributedGraph source, AttributedGraph target, Map<Integer, Integer> mapping,
                                         Set<Integer> usedTargets, double totalCost) {
        List<String> operations = new ArrayList<>();
        for (Node sourceNode : source.getNodes()) {
            Integer targetId = mapping.get(sourceNode.getId());
            if (targetId == null || targetId < 0) {
                operations.add("DELETE node " + sourceNode.getId());
            } else {
                operations.add("MATCH node " + sourceNode.getId() + " -> " + targetId);
            }
        }
        for (Node targetNode : target.getNodes()) {
            if (!usedTargets.contains(targetNode.getId())) {
                operations.add("INSERT node " + targetNode.getId());
            }
        }
        operations.add("TOTAL COST = " + totalCost);
        return operations;
    }

    private int candidateCount(AttributedGraph source, AttributedGraph target, Node sourceNode) {
        int count = 0;
        for (Node targetNode : target.getNodes()) {
            double structural = costFunction.nodeRecognitionCost(source, sourceNode, target, targetNode);
            if (structural <= 1.0) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    private static final class SearchContext {
        private final AttributedGraph source;
        private final AttributedGraph target;
        private final List<Node> orderedSourceNodes;
        private double bestCost = Double.POSITIVE_INFINITY;
        private Map<Integer, Integer> bestMapping;
        private List<String> bestOperations = Collections.emptyList();

        private SearchContext(AttributedGraph source, AttributedGraph target, List<Node> orderedSourceNodes) {
            this.source = source;
            this.target = target;
            this.orderedSourceNodes = orderedSourceNodes;
        }
    }
}