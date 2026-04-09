package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.tools.MultipartFile;

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
    List<Map<String, ?>> getList(
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
        //final enrichedQuery =Map < String, dynamic >.of(query);
        //enrichedQuery["page"] = page;
        //enrichedQuery["perPage"] = perPage;
        //enrichedQuery["filter"] ? ? = filter;
        //enrichedQuery["sort"] ? ? = sort;
        //enrichedQuery["expand"] ? ? = expand;
        //enrichedQuery["fields"] ? ? = fields;
        //enrichedQuery["skipTotal"] ? ? = skipTotal;
        //
        //return client
        //        .send < Map < String,dynamic >> (
        //        baseCrudPath,
        //        query:enrichedQuery,
        //        headers:headers,
        //)
        //.then((data) = > ResultList < M >.fromJson(data, itemFactoryFunc));
        return null;
    }
}
