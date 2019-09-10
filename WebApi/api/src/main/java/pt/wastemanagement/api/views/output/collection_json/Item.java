package pt.wastemanagement.api.views.output.collection_json;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Item {
    public final URI href;
    public final List<Property> data;
    public final List<CollectionLink> links;

    public Item(URI href) {
        this(href, new ArrayList<>(), new ArrayList<>());
    }

    public Item(URI href, List<Property> data, List<CollectionLink> links) {
        this.href = href;
        this.data = data;
        this.links = links;
    }

    public Item addProperty(Property property){
        data.add(property);
        return this;
    }

    public Item addLink(CollectionLink link){
        links.add(link);
        return this;
    }
}
