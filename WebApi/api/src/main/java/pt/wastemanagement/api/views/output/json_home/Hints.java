package pt.wastemanagement.api.views.output.json_home;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Hints{

    public String [] allow;
    public Formats formats;
    public String [] acceptPost;

    public Hints(String[] allow, Formats formats, String[] acceptPost) {
        this.allow = allow;
        this.formats = formats;
        this.acceptPost = acceptPost;
    }
}
