package pt.wastemanagement.api.views.output.siren;

import java.net.URI;

public class SirenLink {
    public static final String
            SELF_REL = "self",
            UP_REL = "up";


    public final String[] rel;
    public final URI href;

    public SirenLink(URI href, String ... rel) {
        this.rel = rel;
        this.href = href;
    }
}
