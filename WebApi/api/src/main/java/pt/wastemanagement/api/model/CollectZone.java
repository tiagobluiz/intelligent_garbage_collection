package pt.wastemanagement.api.model;

public class CollectZone {
    public int collectZoneId, routeId, pickOrder;
    public String active; // T | F

    public CollectZone(int collectZoneId, int routeId, int pickOrder, String active) {
        this.collectZoneId = collectZoneId;
        this.routeId = routeId;
        this.pickOrder = pickOrder;
        this.active = active;
    }
}
