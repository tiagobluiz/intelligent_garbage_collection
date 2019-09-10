package pt.wastemanagement.api.model;

import java.time.LocalDateTime;

public class Collect {
    public final int containerId;
    public final LocalDateTime collectDate;
    public final String confirmed;

    public Collect(int containerId, LocalDateTime collectDate, String confirmed) {
        this.containerId = containerId;
        this.collectDate = collectDate;
        this.confirmed = confirmed;
    }
}
