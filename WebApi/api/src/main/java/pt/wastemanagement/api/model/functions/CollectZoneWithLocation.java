package pt.wastemanagement.api.model.functions;

import pt.wastemanagement.api.model.CollectZone;

public class CollectZoneWithLocation extends CollectZone {
    public final float latitude, longitude;

    public CollectZoneWithLocation(int collectZoneId, int routeId, int pickOrder, String active,
                                   float latitude, float longitude) {
        super(collectZoneId, routeId, pickOrder, active);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
