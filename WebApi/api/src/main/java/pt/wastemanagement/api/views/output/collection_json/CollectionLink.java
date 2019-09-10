package pt.wastemanagement.api.views.output.collection_json;

import java.net.URI;
import java.util.Optional;

public class CollectionLink {
    public static final String
            PREV_REL = "prev",
            NEXT_REL = "next",
            LINK_REL = "link";

    public final String rel;                  // REQUIRED
    public final URI href;                    // REQUIRED
    public final String prompt, render, name; // OPTIONAL

    public CollectionLink(String rel, URI href, Optional<String> name, Optional<String> prompt, Optional<Render> render) {
        this.href = href;
        this.rel = rel;
        this.name = name.isPresent() ? name.get() : null;
        this.prompt = prompt.isPresent() ?  prompt.get() : null;
        this.render = render.isPresent() ? render.get().getRender() : null;
    }
}
