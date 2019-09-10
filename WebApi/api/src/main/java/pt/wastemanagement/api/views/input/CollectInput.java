package pt.wastemanagement.api.views.input;

import java.time.LocalDateTime;

public class CollectInput {
    /**
     * The format MUST be YYYY-MM-DDThh:mm:ss
     */
    public LocalDateTime collectDate;
    public String containerType;

}
