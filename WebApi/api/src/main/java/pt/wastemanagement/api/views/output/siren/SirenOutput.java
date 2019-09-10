package pt.wastemanagement.api.views.output.siren;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({"class","properties","entities","actions","links"})
public class SirenOutput {
    public static final String SIREN_OUTPUT_MEDIA_TYPE = "application/vnd.siren+json";

    @JsonProperty("class")
    public final String[] _class;
    public final Object properties;
    public final List<SubEntity> entities;
    public final List<SirenAction> actions;
    public final List<SirenLink> links;

    public SirenOutput(Optional<Object> object, String ... _class) {
        this(object, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), _class);
    }

    public SirenOutput(Optional<Object> object, List<SubEntity> entities, List<SirenAction> actions, List<SirenLink> links, String ... classes) {
        this._class = classes;
        this.properties = object.isPresent() ? object.get() : null;
        this.entities = entities == null ? new ArrayList<>() : entities;
        this.actions = actions == null ? new ArrayList<>() : actions;
        this.links = links == null ? new ArrayList<>() : links;
    }

    public SirenOutput addAction(SirenAction action) {
        actions.add(action);
        return this;
    }

    public SirenOutput addLink(SirenLink sirenLink) {
        links.add(sirenLink);
        return this;
    }

    public SirenOutput addSubEntity(SubEntity subEntity){
        entities.add(subEntity);
        return this;
    }
}
