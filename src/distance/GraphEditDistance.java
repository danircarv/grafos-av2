package distance;

import graph.AttributedGraph;

public final class GraphEditDistance {
    private final MatchingEngine engine;

    public GraphEditDistance() {
        this(CostFunction.Weights.defaultWeights(), false);
    }

    public GraphEditDistance(CostFunction.Weights weights, boolean strictValidation) {
        this.engine = new MatchingEngine(new CostFunction(weights), new AttributeValidator(strictValidation));
    }

    public DistanceResult distance(AttributedGraph source, AttributedGraph target) {
        return engine.match(source, target);
    }

    public DistanceResult symmetricDistance(AttributedGraph first, AttributedGraph second) {
        DistanceResult forward = distance(first, second);
        DistanceResult backward = distance(second, first);
        return forward.getDistance() <= backward.getDistance() ? forward : backward;
    }
}