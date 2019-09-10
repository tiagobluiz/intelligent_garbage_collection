package pt.wastemanagement.api.views.output.json_home;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Formats {
    @JsonProperty("application/json")
    public final Object format;

    public Formats(Object format) {
        this.format = format;
    }
}
