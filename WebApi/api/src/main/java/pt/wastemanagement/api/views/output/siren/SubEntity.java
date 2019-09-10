package pt.wastemanagement.api.views.output.siren;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({"class","rel","href","properties","links"})
public class SubEntity {
    @JsonProperty("class")
    public final String[] _class;
    public final String[] rel;
    public final URI href;
    public final String type, title;
    public final List<SirenLink> links;
    public final Object properties;

    public SubEntity(URI href, String[] rel, Object properties, Optional<String> type, Optional<String> title, String... classes) {
        this._class = classes;
        this.href = href;
        this.rel = rel;
        this.properties = properties;
        links = new ArrayList<>();
        this.type = type.isPresent() ? type.get() : null;
        this.title = title.isPresent() ? title.get() : null;
    }

    public SubEntity addLink(SirenLink sirenLink){
        links.add(sirenLink);
        return this;
    }
}
