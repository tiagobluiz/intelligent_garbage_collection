package pt.wastemanagement.api.model.functions;

public class ContainerStatistics {
    public final int containerId, numCollects, numWashes;

    public ContainerStatistics(int containerId, int numWashes, int numCollects) {
        this.containerId = containerId;
        this.numCollects = numCollects;
        this.numWashes = numWashes;
    }
}
