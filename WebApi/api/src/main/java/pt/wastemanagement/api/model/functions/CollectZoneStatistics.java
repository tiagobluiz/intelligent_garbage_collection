package pt.wastemanagement.api.model.functions;

public class CollectZoneStatistics {
    public final int collectZoneId,  numContainers;

    public CollectZoneStatistics(int collectZoneId, int numContainers) {
        this.collectZoneId = collectZoneId;
        this.numContainers = numContainers;
    }
}
