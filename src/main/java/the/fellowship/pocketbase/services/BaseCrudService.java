package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.dtos.ResultList;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseCrudService extends BaseService {
    public BaseCrudService(PocketBase client) {
        super(client);
    }

    String getBaseCrudPath() {
        return "";
    }

    /**
     * Creates a new item.
     *
     * @throws ClientException
     */
    public Map<String, ?> create(
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body,
            List<MultipartFile> files
    ) throws ClientException {
        return this.client.send(
                this.getBaseCrudPath(),
                "POST",
                headers,
                query,
                body,
                files
        );
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public ResultList<RecordModel> getList(
            String expand,
            String filter,
            String sort,
            String fields,
            Map<String, ?> query,
            Map<String, String> headers
    ) throws ClientException {
        return getList(
                1,
                30,
                false,
                expand,
                filter,
                sort,
                fields,
                query,
                headers
        );
    }

    /**
     * Returns paginated items list.
     *
     * @throws ClientException
     */
    public ResultList<RecordModel> getList(
            int page,
            int perPage,
            boolean skipTotal,
            String expand,
            String filter,
            String sort,
            String fields,
            Map<String, ?> query,
            Map<String, String> headers
    ) throws ClientException {
        Map<String, Object> enrichedQuery = query != null ? new HashMap<>(query) : new HashMap<>();
        enrichedQuery.put("page", page);
        enrichedQuery.put("perPage", perPage);
        enrichedQuery.putIfAbsent("skipTotal", skipTotal);
        enrichedQuery.putIfAbsent("expand", expand);
        enrichedQuery.putIfAbsent("filter", filter);
        enrichedQuery.putIfAbsent("sort", sort);
        enrichedQuery.putIfAbsent("fields", fields);

        return new ResultList<RecordModel>(
                client.send(
                        getBaseCrudPath(),
                        null,
                        headers,
                        enrichedQuery,
                        null,
                        null
                ),
                RecordModel::new
        );
    }
}
