package pt.wastemanagement.api.views.output;

import pt.wastemanagement.api.model.Container;
import pt.wastemanagement.api.model.functions.ContainerStatistics;

public class GetContainer {
    public int numCollects, numWashes;
    public int containerId, collectZoneId, configurationId;
    public float latitude, longitude;
    public short height, battery, temperature, occupation;
    public String containerType, iotId, active, lastReadDate;

    public GetContainer(Container container, ContainerStatistics containerStatistics){
        if(container != null){
            this.containerId = container.containerId;
            this.iotId = container.iotId;
            this.collectZoneId = container.collectZoneId;
            this.configurationId = container.configurationId;
            this.latitude = container.latitude;
            this.longitude = container.longitude;
            this.height = container.height;
            this.battery = container.battery;
            this.temperature = container.temperature;
            this.occupation = container.occupation;
            this.active = container.active.equalsIgnoreCase("T")? Boolean.toString(true) : Boolean.toString(false);
            this.containerType = container.containerType;
            this.lastReadDate = container.lastReadDate != null ? container.lastReadDate.toString() : null;
        }

        if(containerStatistics != null){
            this.numCollects = containerStatistics.numCollects;
            this.numWashes = containerStatistics.numWashes;
        }
    }
}
