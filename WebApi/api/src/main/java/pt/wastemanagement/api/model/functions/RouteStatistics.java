package pt.wastemanagement.api.model.functions;

public class RouteStatistics {
    public final int routeId, numContainers, numCollectZones, numCollects;

    public RouteStatistics(int routeId, int numContainers, int numCollectZones, int numCollects) {
        this.routeId = routeId;
        this.numContainers = numContainers;
        this.numCollectZones = numCollectZones;
        this.numCollects = numCollects;
    }
}
