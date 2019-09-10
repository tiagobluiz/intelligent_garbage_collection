package pt.wastemanagement.api.views.output.collection_json;


public class CollectionPlusJson {
    public static final String COLLECTION_PLUS_JSON_MEDIA_TYPE = "application/vnd.collection+json";

    public final CollectionJson collection;

    public CollectionPlusJson(CollectionJson collection) {
        this.collection = collection;
    }
}
