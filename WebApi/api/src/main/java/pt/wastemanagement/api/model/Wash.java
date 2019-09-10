package pt.wastemanagement.api.model;

import java.time.LocalDateTime;

public class Wash {
    public final int containerId;
    public final LocalDateTime washDate;

    public Wash(int containerId, LocalDateTime washDate) {
        this.containerId = containerId;
        this.washDate = washDate;
    }
}
