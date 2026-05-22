package the.fellowship.pocketbase.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.ResultList;
import the.fellowship.pocketbase.dtos.MultipartFile;

public class BaseCrudService<T> extends BaseService {
    public BaseCrudService(PocketBase client) {
        super(client);
    }

    String getBaseCrudPath() {
        return "";
    }

    /**
     * The factory function that will be used to
     * decode the returned items from the crud endpoints.
     */
    T itemFactory(Map<String, ?> json) {
        return null;
    }

    /**
     * The factory function that will be used to
     * encode the given item to JSON.
     */
    Map<String, ?> itemEncoder(T item) {
        return null;
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public List<T> getFullList() throws ClientException {
        return getFullList(1000, null, null, null, null, null, null);
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public List<T> getFullList(String filter) throws ClientException {
        return getFullList(1000, null, filter, null, null, null, null);
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public List<T> getFullList(int batch) throws ClientException {
        return getFullList(batch, null, null, null, null, null, null);
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public List<T> getFullList(
            int batch, //= 1000
            String expand,
            String filter,
            String sort,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) throws ClientException {
        final List<T> result = new ArrayList<>();
        ResultList<T> list;
        int page = 1;

        do {
            list = getList(
                    page,
                    batch,
                    true,
                    expand,
                    filter,
                    sort,
                    fields,
                    headers,
                    query
            );

            result.addAll(list.getItems());
            page++;
        } while (list.getItems().size() == list.getPerPage());

        return result;
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public ResultList<T> getList(
            int page //= 1
    ) throws ClientException {
        return getList(
                page,
                30,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public ResultList<T> getList(
            String expand,
            String filter,
            String sort,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) throws ClientException {
        return getList(
                1,
                30,
                false,
                expand,
                filter,
                sort,
                fields,
                headers,
                query
        );
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public ResultList<T> getList(
            int page, //= 1
            int perPage, //= 30
            boolean skipTotal, //= false
            String expand,
            String filter,
            String sort,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) throws ClientException {
        final Map<String, Object> enrichedQuery = query != null ? new TreeMap<>(query) : new TreeMap<>();
        enrichedQuery.put("page", page);
        enrichedQuery.put("perPage", perPage);
        enrichedQuery.putIfAbsent("skipTotal", skipTotal);
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("filter", filter);
        enrichedQuery.putIfAbsent("sort", sort);
        enrichedQuery.putIfAbsent("fields", fields);

        return new ResultList<T>(
                client.send(
                        getBaseCrudPath(),
                        null,
                        headers,
                        enrichedQuery,
                        null,
                        null
                ),
                this::itemFactory,
                this::itemEncoder
        );
    }

    /**
     * Returns single item by its id.
     * <p>
     * Throws 404 `ClientException` in case an empty `id` is provided.
     */
    public T getOne(
            String id
    ) throws ClientException {
        return getOne(id, null, null, null, null);
    }

    /**
     * Returns single item by its id.
     * <p>
     * Throws 404 `ClientException` in case an empty `id` is provided.
     */
    public T getOne(
            String id,
            String expand,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) throws ClientException {
        if (id.isEmpty()) {
            throw new ClientException(
                    client.buildURL(String.format("%s/", getBaseCrudPath())),
                    404,
                    Map.of(
                            "code", 404,
                            "message", "Missing required record id.",
                            "data", new TreeMap<>()
                    )
            );
        }

        final Map<String, Object> enrichedQuery = query != null ? new TreeMap<>(query) : new TreeMap<>();
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("fields", fields);

        final Map<String, ?> json = client.send(
                String.format("%s/%s", getBaseCrudPath(), URLEncoder.encode(id, StandardCharsets.UTF_8)),
                null,
                headers,
                enrichedQuery,
                null,
                null
        );

        return itemFactory(json);
    }

    /**
     * Returns the first found list item by the specified filter.
     * <p>
     * Internally it calls `getList()` and returns its first item.
     * <p>
     * For consistency with `getOne`, this method will throw a 404
     * `ClientException` if no item was found.
     */
    public T getFirstListItem(
            String filter
    ) throws ClientException {
        return getFirstListItem(filter, null, null, null, null);
    }

    /**
     * Returns the first found list item by the specified filter.
     * <p>
     * Internally it calls `getList()` and returns its first item.
     * <p>
     * For consistency with `getOne`, this method will throw a 404
     * `ClientException` if no item was found.
     */
    public T getFirstListItem(
            String filter,
            String expand,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) throws ClientException {
        final ResultList<T> result = getList(
                1,
                1,
                true,
                expand,
                filter,
                null,
                fields,
                headers,
                query
        );

        if (result.getItems().isEmpty()) {
            throw new ClientException(
                    client.buildURL(String.format("%s/", getBaseCrudPath())),
                    404,
                    Map.of(
                            "code", 404,
                            "message", "The requested resource wasn't found.",
                            "data", new TreeMap<>()
                    )
            );
        }

        return result.getItems().get(0);
    }

    /**
     * Creates a new item.
     *
     * @throws ClientException
     */
    public T create(Map<String, ?> body, List<MultipartFile> files) throws ClientException {
        return create(null, null, null, null, body, files);
    }

    /**
     * Creates a new item.
     *
     * @throws ClientException
     */
    public T create(
            String expand,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body,
            List<MultipartFile> files
    ) throws ClientException {
        final Map<String, Object> enrichedQuery = query != null ? new TreeMap<>(query) : new TreeMap<>();
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("fields", fields);

        final Map<String, ?> json = this.client.send(
                this.getBaseCrudPath(),
                "POST",
                headers,
                enrichedQuery,
                body,
                files
        );

        return itemFactory(json);
    }

    /**
     * Updates a single item by its id.
     */
    public T update(
            String id,
            Map<String, ?> body,
            List<MultipartFile> files
    ) throws ClientException {
        return update(id, null, null, null, null, body, files);
    }

    /**
     * Updates a single item by its id.
     */
    public T update(
            String id,
            String expand,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body,
            List<MultipartFile> files
    ) throws ClientException {
        final Map<String, Object> enrichedQuery = query != null ? new TreeMap<>(query) : new TreeMap<>();
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("fields", fields);

        final Map<String, ?> json = client.send(
                String.format("%s/%s", getBaseCrudPath(), URLEncoder.encode(id, StandardCharsets.UTF_8)),
                "PATCH",
                headers,
                enrichedQuery,
                body,
                files
        );

        return itemFactory(json);
    }

    /**
     * Deletes a single item by its id.
     */
    public void delete(String id) throws ClientException {
        delete(id, null, null, null);
    }

    /**
     * Deletes a single item by its id.
     */
    public void delete(
            String id,
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body
    ) throws ClientException {
        client.send(
                String.format("%s/%s", getBaseCrudPath(), URLEncoder.encode(id, StandardCharsets.UTF_8)),
                "DELETE",
                headers,
                query,
                body,
                null
        );
    }
}
