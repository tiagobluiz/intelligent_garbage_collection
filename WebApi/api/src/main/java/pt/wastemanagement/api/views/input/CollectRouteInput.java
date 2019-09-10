package pt.wastemanagement.api.views.input;

import pt.wastemanagement.api.mappers.ContainerMapper;
import pt.wastemanagement.api.views.output.Options;

import java.time.LocalDateTime;
import java.util.List;

public class CollectRouteInput {
    public float latitude = -1000, longitude = -1000;
    public LocalDateTime startDate;
    public String truckPlate;
    public String containerType;
    public List<Options> containerTypeOptions = ContainerMapper.CONTAINER_TYPES;

}
