package pt.wastemanagement.api.controllers;

import pt.wastemanagement.api.views.output.collection_json.CollectionLink;
import pt.wastemanagement.api.views.output.collection_json.Query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;

public class Controller {
    // Data Types
    public static final String
            TEXT_DATA_TYPE = "text",
            NUMBER_DATA_TYPE = "number",
            DATE_TIME_LOCAL_TYPE = "datetime-local";

    //Path partitions
    public static final String
            ACTIVATE_PREFIX = "/activate",
            DEACTIVATE_PREFIX = "/deactivate",
            RELS_PREFIX = "/rels";

    //Path parameters
    public static final String
            SHOW_INACTIVE_QUERY_PARAM ="showInactive",
            PAGE_QUERY_PARAM = "page",
            ROWS_QUERY_PARAM = "rows";

    //Classes
    public static final String
            EMPLOYEE_CLASS = "employee",
            ROUTE_COLLECTIONS_CLASS = "route-collections",
            ROUTE_COLLECT_ZONES_CLASS = "route-collect-zones",
            ROUTE_CONTAINERS_CLASS = "route-containers",
            ROUTE_COLLECTION_PLAN_CLASS = "route-collection-plan",
            ROUTE_CLASS = "route",
            STATION_CLASS = "station",
            CONTAINER_CLASS = "container",
            CONTAINERS_IN_RANGE_CLASS = "containers-in-range",
            COMMUNICATION_CLASS = "communication",
            COLLECT_CLASS = "collect",
            COLLECT_COLLECT_ZONE_CONTAINERS_CLASS = "collect-collect-zone-containers",
            COLLECT_ZONES_IN_RANGE_CLASS = "collect-zones-in-range",
            WASH_CLASS = "wash",
            WASH_COLLECT_ZONE_CONTAINERS_CLASS = "wash-collect-zone-containers",
            CONFIGURATION_CLASS = "configuration",
            CONFIGURATION_COMMUNICATION_CLASS = "configuration-communication",
            CONFIGURATION_COMMUNICATION_LIST_CLASS = "configuration-communication-list",
            TRUCK_CLASS = "truck",
            COLLECT_ZONE_CLASS = "collect-zone",
            COLLECT_ROUTE_CLASS = "collect-route",
            COLLECT_ZONE_CONTAINERS_CLASS = "collect-zone-containers",
            ROUTE_DROP_ZONE_CLASS = "route-drop-zone",
            TRUCK_COLLECTS_CLASS = "truck-collects",
            COLLECTABLE_ROUTES_CLASS = "collectable-routes",
            COLLECTION_CLASS = "collection",
            LIST_CLASS = "list";

    //Relations
    public static final String
            EMPLOYEE_REL = RELS_PREFIX + "/" + EMPLOYEE_CLASS,
            ROUTE_COLLECTIONS_REL = RELS_PREFIX + "/" + ROUTE_COLLECTIONS_CLASS,
            ROUTE_COLLECT_ZONES_REL = RELS_PREFIX +  "/" + ROUTE_COLLECT_ZONES_CLASS,
            CONTAINERS_IN_RANGE_REL = RELS_PREFIX + "/" + CONTAINERS_IN_RANGE_CLASS,
            COLLECT_ZONE_REL = RELS_PREFIX +  "/" + COLLECT_ZONE_CLASS,
            COLLECT_ZONE_LIST_REL = COLLECT_ZONE_REL + "-" + LIST_CLASS,
            COLLECT_ZONES_IN_RANGE_REL = RELS_PREFIX + "/" + COLLECT_ZONES_IN_RANGE_CLASS,
            COLLECTABLE_ROUTES_REL = RELS_PREFIX +  "/" + COLLECTABLE_ROUTES_CLASS,
            COLLECT_ZONE_CONTAINERS_REL = RELS_PREFIX +  "/" + COLLECT_ZONE_CONTAINERS_CLASS,
            COLLECT_REL = RELS_PREFIX + "/" + COLLECT_CLASS,
            COLLECT_ROUTE_REL = RELS_PREFIX + "/" +  COLLECT_ROUTE_CLASS,
            COLLECT_COLLECT_ZONE_CONTAINERS_REL = RELS_PREFIX + "/" + COLLECT_COLLECT_ZONE_CONTAINERS_CLASS,
            WASH_REL = RELS_PREFIX + "/" + WASH_CLASS,
            WASH_COLLECT_ZONE_CONTAINERS_REL = RELS_PREFIX + "/" +  WASH_COLLECT_ZONE_CONTAINERS_CLASS,
            CONFIGURATION_COMMUNICATIONS_LIST_REL = RELS_PREFIX +  "/" + CONFIGURATION_COMMUNICATION_LIST_CLASS,
            TRUCK_COLLECTS_REL = RELS_PREFIX +  "/" + TRUCK_COLLECTS_CLASS,
            ROUTE_CONTAINERS_REL = RELS_PREFIX +  "/" + ROUTE_CONTAINERS_CLASS,
            ROUTE_COLLECTION_PLAN_REL = RELS_PREFIX +  "/" + ROUTE_COLLECTION_PLAN_CLASS,
            ROUTE_DROP_ZONES_REL = RELS_PREFIX +  "/" + ROUTE_DROP_ZONE_CLASS,
            ROUTE_REL = RELS_PREFIX + "/" + ROUTE_CLASS,
            ROUTE_LIST_REL = ROUTE_REL + "-" + LIST_CLASS,
            CONFIGURATION_REL = RELS_PREFIX +  "/" + CONFIGURATION_CLASS,
            CONFIGURATION_LIST_REL = CONFIGURATION_REL + "-" + LIST_CLASS,
            COMMUNICATION_REL = RELS_PREFIX +  "/" + COMMUNICATION_CLASS,
            COMMUNICATION_LIST_REL = COMMUNICATION_REL + "-" + LIST_CLASS,
            STATION_REL = RELS_PREFIX + "/" + STATION_CLASS,
            STATION_LIST_REL = STATION_REL + "-" + LIST_CLASS,
            TRUCK_REL = RELS_PREFIX + "/" + TRUCK_CLASS,
            TRUCK_LIST_REL = TRUCK_REL + "-" + LIST_CLASS,
            FILTER_REL = "filter";

    //Email configurations
    public static final String
            SENDER_NAME = "Waste Management Administration",
            SENDER_EMAIL = "42215@alunos.isel.ipl.pt",
            API_KEY = "5c49077408c0941bed78096adc4774b8",
            API_SECRET  = "50d8bc89c5bfb216be77d7a0a08df0ea";

    /*
    Utility methods
     */

    /**
     * Create a list with links for next and previous page, having showInactive flag
     * @param totalEntries total entries of the collection
     * @param selfURI URI of the actual resource (excluding query parameters)
     * @param pageNumber actual page number
     * @param rowsPerPage number of rows per page to show
     * @param showInactive actual state of the showInactive flag
     * @return a list with the links for next and prev page
     * @throws URISyntaxException
     */
    protected static List<CollectionLink> getPageLinks(int totalEntries, String selfURI,
                                              int pageNumber, int rowsPerPage,
                                              boolean showInactive) throws URISyntaxException {
        List<CollectionLink> links = new ArrayList<>();
        if (totalEntries > 0) {
            if (totalEntries > (pageNumber * rowsPerPage))
                links.add(new CollectionLink(CollectionLink.NEXT_REL, new URI(selfURI + "?" + PAGE_QUERY_PARAM + "=" +
                        (pageNumber + 1) + "&" + ROWS_QUERY_PARAM + "=" + rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" +
                        showInactive), Optional.empty(), Optional.empty(), Optional.empty()));
            if (rowsPerPage <= (pageNumber - 1) * rowsPerPage)
                links.add(new CollectionLink(CollectionLink.PREV_REL, new URI(selfURI + "?" + PAGE_QUERY_PARAM + "=" +
                        (pageNumber - 1) + "&" + ROWS_QUERY_PARAM + "=" + rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" +
                        showInactive), Optional.empty(), Optional.empty(), Optional.empty()));
        }
        return links;
    }

    /**
     * Create a list with links for next and previous page, without showInactive flag
     * @param totalEntries total entries of the collection
     * @param selfURI URI of the actual resource (excluding query parameters)
     * @param pageNumber actual page number
     * @param rowsPerPage number of rows per page to show
     * @return a list with the links for next and prev page
     * @throws URISyntaxException
     */
    protected static List<CollectionLink> getPageLinks(int totalEntries, String selfURI,
                                              int pageNumber, int rowsPerPage)
            throws URISyntaxException {
        List<CollectionLink> links = new ArrayList<>();
        if (totalEntries > 0) {
            if (totalEntries > (pageNumber * rowsPerPage))
                links.add(new CollectionLink(CollectionLink.NEXT_REL, new URI(selfURI + "?" + PAGE_QUERY_PARAM + "=" +
                        (pageNumber + 1) + "&" + ROWS_QUERY_PARAM + "=" + rowsPerPage), Optional.empty(),
                        Optional.empty(), Optional.empty()));
            if (rowsPerPage <= (pageNumber - 1) * rowsPerPage)
                links.add(new CollectionLink(CollectionLink.PREV_REL, new URI(selfURI + "?" + PAGE_QUERY_PARAM + "=" +
                        (pageNumber - 1) + "&" + ROWS_QUERY_PARAM + "=" + rowsPerPage), Optional.empty(),
                        Optional.empty(), Optional.empty()));
        }
        return links;
    }

    /**
     * Return a list with a query relative to the show inactive flag
     * @param selfURI URI of the actual resource (excluding query parameters)
     * @param rowsPerPage actual number of rows per page
     * @param showInactive actual state of flag @showInactive on resource
     * @return a list with a query
     * @throws URISyntaxException
     */
    protected static List<Query> getShowInactiveQueries (String selfURI, int rowsPerPage, boolean showInactive)
            throws URISyntaxException {
        List<Query> queries = new ArrayList<>(1);
        queries.add(new Query(
                new URI(selfURI + "?" + PAGE_QUERY_PARAM + "=" + 1 + "&" + ROWS_QUERY_PARAM + "=" +
                        rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + !showInactive),
                FILTER_REL, empty(), empty(), new ArrayList<>()
        ));
        return queries;
    }
}
