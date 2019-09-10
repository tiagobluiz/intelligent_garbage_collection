package pt.wastemanagement.api.views.output;

import pt.wastemanagement.api.model.functions.RouteStatistics;
import pt.wastemanagement.api.model.functions.RouteWithStationNameAndLocation;

public class GetRoute {
    public int numContainers, numCollectZones, numCollects, routeId;
    public String startPointStationName, finishPointStationName, active;
    public float startPointLatitude, startPointLongitude, finishPointLatitude, finishPointLongitude;

    public GetRoute(RouteWithStationNameAndLocation routeInfo, RouteStatistics routeStatistics) {
        if(routeStatistics != null){
            this.numContainers = routeStatistics.numContainers;
            this.numCollectZones = routeStatistics.numCollectZones;
            this.numCollects = routeStatistics.numCollects;
        }
        if(routeInfo != null) {
            this.routeId = routeInfo.routeId;
            this.active = routeInfo.active.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false);
            this.startPointStationName = routeInfo.startPointStationName;
            this.finishPointStationName = routeInfo.finishPointStationName;
            this.startPointLatitude = routeInfo.startPointLatitude;
            this.startPointLongitude = routeInfo.startPointLongitude;
            this.finishPointLatitude = routeInfo.finishPointLatitude;
            this.finishPointLongitude = routeInfo.finishPointLongitude;
        }
    }
}
