package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.ResultList;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Encode item to JSON.
     */
    Map<String, ?> itemEncoder(T item) {
        return null;
    }

    /**
     * Creates a new item.
     *
     * @throws ClientException
     */
    public T create(
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body,
            List<MultipartFile> files
    ) throws ClientException {
        Map<String, ?> json = this.client.send(
                this.getBaseCrudPath(),
                "POST",
                headers,
                query,
                body,
                files
        );

        return itemFactory(json);
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
            int page,
            int perPage,
            boolean skipTotal,
            String expand,
            String filter,
            String sort,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
            ) throws ClientException {
        Map<String, Object> enrichedQuery = query != null ? new HashMap<>(query) : new HashMap<>();
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

    /// Returns single item by its id.
    ///
    /// Throws 404 `ClientException` in case an empty `id` is provided.
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
                            "data", new HashMap<>()
                    )
            );
        }

        Map<String, Object> enrichedQuery = query != null ? new HashMap<>(query) : new HashMap<>();
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("fields", fields);

        Map<String, ?> json = client.send(
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
        Map<String, Object> enrichedQuery = query != null ? new HashMap<>(query) : new HashMap<>();
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("fields", fields);

        Map<String, ?> json = client.send(
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
