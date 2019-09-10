package pt.wastemanagement.api.model;

public class Route {
    public  int routeId, startPoint, finishPoint;
    public String active; // 'T' | 'F'

    public Route(int routeId, int startPoint, int finishPoint, String active) {
        this.routeId = routeId;
        this.startPoint = startPoint;
        this.finishPoint = finishPoint;
        this.active = active;
    }
}
