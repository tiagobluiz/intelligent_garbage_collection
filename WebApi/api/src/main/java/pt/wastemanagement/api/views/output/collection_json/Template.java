package pt.wastemanagement.api.views.output.collection_json;

import java.util.ArrayList;
import java.util.List;

public class Template {
    public final List<Property> data;

    public Template(){
        this(new ArrayList<>());
    }

    public Template(List<Property> data) {
        this.data = data;
    }

    public Template addProperty(Property property){
        data.add(property);
        return this;
    }
}
