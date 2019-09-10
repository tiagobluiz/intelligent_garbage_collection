package pt.wastemanagement.api.views.output.json_home;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resource{
    public String href;
    public HrefVars hrefVars;
    public String hrefTemplate;
    public Hints hints;

    public Resource(String hrefTemplate, HrefVars hrefVars, Hints hints) {
        this.href = null;
        this.hrefTemplate = hrefTemplate;
        this.hrefVars = hrefVars;
        this.hints=hints;
    }
    public Resource(String href) {
        this.href = href;
        this.hrefTemplate = null;
        this.hrefVars = null;
        this.hints=null;
    }
}
