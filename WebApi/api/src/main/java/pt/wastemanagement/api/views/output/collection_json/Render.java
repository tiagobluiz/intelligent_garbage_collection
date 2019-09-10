package pt.wastemanagement.api.views.output.collection_json;

public class Render {
    public static final Render Link = new Render("link");
    public static final Render Image = new Render("image");

    public final String render;

    private Render(String render){
        this.render = render;
    }

    public String getRender() {
        return render;
    }

    public static Render valueOf(String render){
        if(Link.getRender().equalsIgnoreCase(render))
            return Link;
        else if (Image.getRender().equalsIgnoreCase(render))
            return Image;
        return new Render(render);
    }
}
