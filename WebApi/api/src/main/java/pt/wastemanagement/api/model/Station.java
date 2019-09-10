package pt.wastemanagement.api.model;

public class Station {
    public final int stationId;
    public final float latitude, longitude;
    public final String stationName, stationType;

    public Station(int stationId, String stationName, float latitude, float longitude, String stationType) {
        this.stationName = stationName;
        this.stationId = stationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stationType = stationType;
    }
}
