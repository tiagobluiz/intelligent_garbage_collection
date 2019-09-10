package pt.wastemanagement.api.views.output.collection_json;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class Query {
    public final URI href;                            // REQUIRED
    public final String
            rel,                                      // REQUIRED
            prompt,                                   // OPTIONAL
            name;                                     // OPTIONAL
    public final List<Property> data;                 // OPTIONAL

    public Query(URI href, String rel, Optional<String> prompt, Optional<String> name, List<Property> data) {
        this.href = href;
        this.rel = rel;
        this.prompt = prompt.isPresent() ? prompt.get() : null;
        this.name = name.isPresent() ? name.get() : null;
        this.data = data;
    }


}
