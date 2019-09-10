package pt.wastemanagement.api.model.functions;

import pt.wastemanagement.api.model.RouteDropZone;

public class RouteDropZoneWithLocation extends RouteDropZone {
    public final float latitude, longitude;

    public RouteDropZoneWithLocation(int routeId, int dropZoneId, float latitude, float longitude) {
        super(routeId, dropZoneId);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
