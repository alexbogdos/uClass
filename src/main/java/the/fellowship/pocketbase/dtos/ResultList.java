package the.fellowship.pocketbase.dtos;

import the.fellowship.pocketbase.PocketBase;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ResultList<T> {
    private final int page;
    private final int perPage;
    private final int totalItems;
    private final int totalPages;
    private final Function<T, Map<String, ?>> itemConverter;

    private final List<T> items;

    public ResultList(Map<String, ?> data, Function<Map<String, ?>, T> itemFactory) {
        this(data, itemFactory, null);
    }

    public ResultList(Map<String, ?> data, Function<Map<String, ?>, T> itemFactory, Function<T, Map<String, ?>> itemConverter) {
        this.page = ((Number) data.get("page")).intValue();
        this.perPage = ((Number) data.get("perPage")).intValue();
        this.totalItems = ((Number) data.get("totalItems")).intValue();
        this.totalPages = ((Number) data.get("totalPages")).intValue();
        this.items = ((List<Map<String, ?>>) data.get("items")).stream().map(itemFactory).toList();
        this.itemConverter = itemConverter;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    /**
     * @return JSON as Map<String, ?>
     */
    public Map<String, ?> getJson() {
        return Map.of(
                "page", page,
                "perPage", perPage,
                "totalItems", totalItems,
                "totalPages", totalPages,
                "items", items.stream().map((item) -> {
                    if (itemConverter != null) {
                        return itemConverter.apply(item);
                    }
                    return String.valueOf(item);
                }).toList()
        );
    }

    /**
     * @return JSON encoded to String
     */
    public String toJson() {
        return PocketBase.jsonEncode(getJson());
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
