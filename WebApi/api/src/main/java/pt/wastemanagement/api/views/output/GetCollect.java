package pt.wastemanagement.api.views.output;

import pt.wastemanagement.api.model.Collect;

import java.time.LocalDateTime;

public class GetCollect {
    public final int containerId;
    public final LocalDateTime collectDate;
    public final String confirmed;

    public GetCollect(Collect collect) {
        this.containerId = collect.containerId;
        this.collectDate = collect.collectDate;
        this.confirmed = collect.confirmed.equalsIgnoreCase("T")? Boolean.toString(true) : Boolean.toString(false);
    }
}
