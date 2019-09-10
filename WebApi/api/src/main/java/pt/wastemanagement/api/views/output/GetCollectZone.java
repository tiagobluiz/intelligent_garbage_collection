package pt.wastemanagement.api.views.output;

import pt.wastemanagement.api.model.functions.CollectZoneStatistics;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocation;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocationAndOccupationInfo;

public class GetCollectZone {
    public int collectZoneId, routeId, pickOrder,  numContainers;
    public String  active; // T | F
    public float latitude, longitude;
    public short generalOccupation, plasticOccupation, paperOccupation, glassOccupation;

    public GetCollectZone(CollectZoneWithLocationAndOccupationInfo collectZoneWithLocationAndOccupationInfo, CollectZoneStatistics collectZoneStatistics){
        if(collectZoneStatistics != null){
            this.collectZoneId = collectZoneStatistics.collectZoneId;
            this.numContainers = collectZoneStatistics.numContainers;
        }
        if(collectZoneWithLocationAndOccupationInfo != null){
            this.latitude = collectZoneWithLocationAndOccupationInfo.latitude;
            this.longitude = collectZoneWithLocationAndOccupationInfo.longitude;
            this.collectZoneId = collectZoneWithLocationAndOccupationInfo.collectZoneId;
            this.routeId = collectZoneWithLocationAndOccupationInfo.routeId;
            this.pickOrder = collectZoneWithLocationAndOccupationInfo.pickOrder;
            this.active = collectZoneWithLocationAndOccupationInfo.active.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false);
            this.generalOccupation = collectZoneWithLocationAndOccupationInfo.generalOccupation;
            this.plasticOccupation = collectZoneWithLocationAndOccupationInfo.plasticOccupation;
            this.paperOccupation = collectZoneWithLocationAndOccupationInfo.paperOccupation;
            this.glassOccupation = collectZoneWithLocationAndOccupationInfo.glassOccupation;
        }
    }
}
