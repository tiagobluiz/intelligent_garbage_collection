package pt.wastemanagement.api.model.utils;

import java.util.List;

public class PaginatedList<T> {
    public final int totalEntries;
    public final List<T> elements;

    public PaginatedList(int totalEntries, List<T> elements) {
        this.totalEntries = totalEntries;
        this.elements = elements;
    }
}
