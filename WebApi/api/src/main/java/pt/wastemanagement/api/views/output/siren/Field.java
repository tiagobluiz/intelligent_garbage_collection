package pt.wastemanagement.api.views.output.siren;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.wastemanagement.api.views.output.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This Field is equivalent to the one that is described by application/vdn.siren+json,
 * but since that one didn't prevent the case where the value has limited options,
 * it was added the @options field.
 * It was assumed that, if the value is other than null, then options should be an empty list,
 * if we want to have options, the we should have a null value.
 */
public class Field {
    public final String
            name,                                       // REQUIRED
            type,                                       // OPTIONAL
            value,                                      // OPTIONAL
            title;                                      // OPTIONAL
    @JsonProperty("field_class")
    public final String  fieldClass;                    // OPTIONAL
    public final List<Options> options;                // OPTIONAL

    public Field(String name, Optional<String> fieldClass, Optional<String> type, Optional<String> value, Optional<String> title, Optional<List<Options>> options) {
        this.name = name;
        this.fieldClass = fieldClass.isPresent() ? fieldClass.get() : null;
        this.type = type.isPresent() ? type.get() : null;
        this.title = title.isPresent() ? title.get() : null;
        if(value.isPresent()){
            this.value = value.get();
            this.options = new ArrayList<>(0);
        } else if(options.isPresent()){
            this.value = null;
            this.options = options.get();
        } else {
            this.value = null;
            this.options = new ArrayList<>(0);
        }
    }
}
