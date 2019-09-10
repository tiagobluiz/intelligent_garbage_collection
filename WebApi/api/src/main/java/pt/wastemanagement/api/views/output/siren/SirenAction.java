package pt.wastemanagement.api.views.output.siren;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SirenAction {
    public final String name;
    public final String title;
    public final HttpMethod method;
    public final URI href;
    public final String type;
    public final List<Field> fields;

    public SirenAction(String name, Optional<String> title, HttpMethod method, URI href, MediaType type) {
        this.name = name;
        this.title = title.isPresent()? title.get() : null;
        this.method = method;
        this.href = href;
        this.type = type.getType() + "/" + type.getSubtype();
        this.fields = new ArrayList<>();
    }

    public SirenAction addField(Field field){
        fields.add(field);
        return this;
    }
}
