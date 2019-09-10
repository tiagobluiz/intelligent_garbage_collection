package pt.wastemanagement.api.model;

public class RouteDropZone{
    public final int routeId, dropZoneId;

    public RouteDropZone(int routeId, int dropZoneId) {
        this.routeId = routeId;
        this.dropZoneId = dropZoneId;
    }
}
