package pt.wastemanagement.api.model.functions;

import pt.wastemanagement.api.model.CollectZone;

public class CollectZoneWithLocationAndOccupationInfo extends CollectZoneWithLocation {
    public final short generalOccupation, plasticOccupation, paperOccupation, glassOccupation;

    public CollectZoneWithLocationAndOccupationInfo(int collectZoneId, int routeId, int pickOrder, String active, float latitude,
                                                    float longitude, short generalOccupation, short plasticOccupation,
                                                    short paperOccupation, short glassOccupation) {
        super(collectZoneId, routeId, pickOrder, active, latitude, longitude);
        this.generalOccupation = generalOccupation;
        this.plasticOccupation = plasticOccupation;
        this.paperOccupation = paperOccupation;
        this.glassOccupation = glassOccupation;
    }
}

