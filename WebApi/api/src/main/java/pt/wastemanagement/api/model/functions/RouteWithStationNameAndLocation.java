package pt.wastemanagement.api.model.functions;

/*Apenas para que o nome condiza com a query*/
public class RouteWithStationNameAndLocation {
    public final int routeId;
    public final String active, startPointStationName, finishPointStationName;
    public final float startPointLatitude, startPointLongitude, finishPointLatitude, finishPointLongitude;

    public RouteWithStationNameAndLocation(int routeId, String active,
                                           String startPointStationName, float startPointLatitude, float startPointLongitude,
                                           String finishPointStationName, float finishPointLatitude, float finishPointLongitude) {
        this.routeId = routeId;
        this.active = active;
        this.startPointStationName = startPointStationName;
        this.finishPointStationName = finishPointStationName;
        this.startPointLatitude = startPointLatitude;
        this.startPointLongitude = startPointLongitude;
        this.finishPointLatitude = finishPointLatitude;
        this.finishPointLongitude = finishPointLongitude;
    }
}
