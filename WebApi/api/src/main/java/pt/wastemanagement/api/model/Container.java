package pt.wastemanagement.api.model;

import java.time.LocalDateTime;

public class Container{
    public final int containerId, collectZoneId, configurationId;
    public final float latitude, longitude;
    public final short height, battery, temperature, occupation;
    public final String active, containerType, iotId;
    public final LocalDateTime lastReadDate;

    public Container(int containerId, String iotId, String active, float latitude, float longitude, short height,
                     String containerType, LocalDateTime lastReadDate, short temperature, short occupation, int collectZoneId, int configurationId, short battery) {
        this.containerId = containerId;
        this.iotId = iotId;
        this.lastReadDate = lastReadDate;
        this.collectZoneId = collectZoneId;
        this.configurationId = configurationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.battery = battery;
        this.temperature = temperature;
        this.occupation = occupation;
        this.active = active;
        this.containerType = containerType;
    }
}
