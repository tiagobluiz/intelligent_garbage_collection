package pt.wastemanagement.api.model;

import java.time.LocalDateTime;

public class RouteCollection {
    public int routeId;
    public String truckPlate;
    public LocalDateTime startDate, finishDate;

    public RouteCollection(int routeId, LocalDateTime startDate, LocalDateTime finishDate, String truckPlate) {
        this.routeId = routeId;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.truckPlate = truckPlate;
    }
}
