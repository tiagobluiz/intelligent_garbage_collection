package pt.wastemanagement.api.views.input;

import java.time.LocalDateTime;

public class WashInput {
    /**
     * The format MUST be YYYY-MM-DDThh:mm:ss
     */
    public LocalDateTime washDate;
    public String containerType;
}
