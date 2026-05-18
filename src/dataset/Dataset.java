package dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Dataset {
    private final List<GraphSample> samples;

    public Dataset(List<GraphSample> samples) {
        this.samples = Collections.unmodifiableList(new ArrayList<>(samples));
    }

    public List<GraphSample> getSamples() {
        return samples;
    }
}