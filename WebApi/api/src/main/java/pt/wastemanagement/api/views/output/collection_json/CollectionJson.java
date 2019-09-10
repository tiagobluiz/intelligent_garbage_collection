package pt.wastemanagement.api.views.output.collection_json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({"version","href","links","items","queries","template"})
public class CollectionJson {
    public final String version;            // REQUIRED
    public final URI href;                  // REQUIRED
    public final List<CollectionLink> links;          // OPTIONAL
    public final List<Item> items;          // OPTIONAL
    public final List<Query> queries;       // OPTIONAL
    public final Template template;         // OPTIONAL

    public CollectionJson(URI href, List<CollectionLink> links, List<Item> items, List<Query> queries, Optional<Template> template) {
        this.version = "1.0";
        this.href = href;
        this.links = links == null ? new ArrayList<>() : links;
        this.items = items == null ? new ArrayList<>() : items;
        this.queries = queries == null ? new ArrayList<>() : queries;
        this.template = template.isPresent() ? template.get() : null;
    }
}
