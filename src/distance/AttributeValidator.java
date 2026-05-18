package distance;

import graph.AttributedGraph;
import graph.Edge;
import graph.Node;

import java.util.Map;

public final class AttributeValidator {
    private final boolean strictLabels;

    public AttributeValidator(boolean strictLabels) {
        this.strictLabels = strictLabels;
    }

    public boolean isValidMapping(AttributedGraph source, AttributedGraph target, Map<Integer, Integer> mapping) {
        for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
            if (entry.getValue() == null || entry.getValue() < 0) {
                continue;
            }
            Node sourceNode = source.nodeById(entry.getKey()).orElse(null);
            Node targetNode = target.nodeById(entry.getValue()).orElse(null);
            if (sourceNode == null || targetNode == null) {
                return false;
            }
            if (strictLabels && !sourceNode.getLabel().equals(targetNode.getLabel())) {
                return false;
            }
        }

        for (Edge sourceEdge : source.getEdges()) {
            Integer mappedSource = mapping.get(sourceEdge.getSourceId());
            Integer mappedTarget = mapping.get(sourceEdge.getTargetId());
            if (mappedSource == null || mappedTarget == null) {
                continue;
            }
            if (mappedSource.equals(mappedTarget)) {
                return false;
            }
            if (target.edgeBetween(mappedSource, mappedTarget).isPresent()) {
                continue;
            }
        }

        return true;
    }
}