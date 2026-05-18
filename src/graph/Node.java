package graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Node {
    private final int id;
    private final String label;
    private final String branchLabel;
    private final Map<String, String> attributes;

    public Node(int id, String label) {
        this(id, label, "", Map.of());
    }

    public Node(int id, String label, String branchLabel, Map<String, String> attributes) {
        this.id = id;
        this.label = label == null ? "" : label;
        this.branchLabel = branchLabel == null ? "" : branchLabel;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes == null ? Map.of() : attributes));
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getBranchLabel() {
        return branchLabel;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String attribute(String key) {
        return attributes.get(key);
    }

    public Node withLabel(String newLabel) {
        return new Node(id, newLabel, branchLabel, attributes);
    }

    public Node withBranchLabel(String newBranchLabel) {
        return new Node(id, label, newBranchLabel, attributes);
    }

    public Node withAttribute(String key, String value) {
        Map<String, String> copy = new LinkedHashMap<>(attributes);
        copy.put(key, value);
        return new Node(id, label, branchLabel, copy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Node node)) {
            return false;
        }
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", branchLabel='" + branchLabel + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}