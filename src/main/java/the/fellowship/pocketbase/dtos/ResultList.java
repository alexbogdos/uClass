package the.fellowship.pocketbase.dtos;

import java.util.List;
import java.util.Map;

public class ResultList<T> {
    double page;
    double perPage;
    double totalItems;
    double totalPages;

    List<T> items;

    public ResultList(Map<String, ?> data, java.util.function.Function<Map<String, ?>, ? super T> itemFactory) {
        this.page = (double) data.get("page");
        this.perPage = (double) data.get("perPage");
        this.totalItems = (double) data.get("totalItems");
        this.totalPages = (double) data.get("totalPages");
        this.items = (List<T>) ((List<Map<String, ?>>) data.get("items")).stream().map(itemFactory::apply).toList();
    }

    public double getPage() {
        return page;
    }

    public double getPerPage() {
        return perPage;
    }

    public double getTotalItems() {
        return totalItems;
    }

    public double getTotalPages() {
        return totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
