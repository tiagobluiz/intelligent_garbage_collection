package pt.wastemanagement.api.views.output.collection_json;

import pt.wastemanagement.api.views.output.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This Property is equivalent to the one that is described by application/collection+json,
 * but since that one didn't prevent the case where the value has limited options,
 * it was added the @options field.
 * It was assumed that, if the value is other than null, then options should be an empty list,
 * if we want to have options, the we should have a null value.
 */
public class Property {
    public final String
            name,                                   // REQUIRED
            value,                                  // OPTIONAL
            prompt;                                 // OPTIONAL
    public final List<Options> options;     // OPTIONAL

    public Property(String name, Optional<String> value, Optional<String> prompt, Optional<List<Options>> options) {
        this.name = name;
        this.prompt = prompt.isPresent() ? prompt.get() : null;
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
