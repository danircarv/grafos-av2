package graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Edge {
    private final int sourceId;
    private final int targetId;
    private final String label;
    private final Map<String, String> attributes;

    public Edge(int sourceId, int targetId, String label) {
        this(sourceId, targetId, label, Map.of());
    }

    public Edge(int sourceId, int targetId, String label, Map<String, String> attributes) {
        if (sourceId == targetId) {
            throw new IllegalArgumentException("Self-loops are not used in the reconstructed paper algorithm");
        }
        if (sourceId < targetId) {
            this.sourceId = sourceId;
            this.targetId = targetId;
        } else {
            this.sourceId = targetId;
            this.targetId = sourceId;
        }
        this.label = label == null ? "" : label;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes == null ? Map.of() : attributes));
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public String getLabel() {
        return label;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Edge withLabel(String newLabel) {
        return new Edge(sourceId, targetId, newLabel, attributes);
    }

    public Edge withAttribute(String key, String value) {
        Map<String, String> copy = new LinkedHashMap<>(attributes);
        copy.put(key, value);
        return new Edge(sourceId, targetId, label, copy);
    }

    public String key() {
        return sourceId + "-" + targetId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Edge edge)) {
            return false;
        }
        return sourceId == edge.sourceId && targetId == edge.targetId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, targetId);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "sourceId=" + sourceId +
                ", targetId=" + targetId +
                ", label='" + label + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}