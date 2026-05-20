package the.fellowship.pocketbase.services;

import java.util.Map;
import java.util.TreeMap;

import okhttp3.HttpUrl;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordModel;

/**
 * The service that handles the **File APIs**.
 * <p>
 * Usually shouldn't be initialized manually and instead
 * [PocketBase.files] should be used.
 */
public class FileService extends BaseService {
    public FileService(PocketBase client) {
        super(client);
    }

    /**
     * Builds and returns an absolute record file url.
     */
    HttpUrl getURL(
            RecordModel record,
            String filename,
            String thumb,
            String token,
            boolean download,
            Map<String, ?> query
    ) {
        if (filename.isEmpty() || record.getId().isEmpty()) {
            return null;
        }

        final Map<String, Object> params = query != null ? new TreeMap<>(query) : new TreeMap<>();
        if (thumb != null && !thumb.isEmpty()) params.put("thumb", thumb);
        if (token != null && !token.isEmpty()) params.put("token", token);
        if (token != null && download) params.put("download", "");

        final String collectionIdOrName = record.getCollectionId().isEmpty()
                ? record.getCollectionName()
                : record.getCollectionId();

        return client.buildURL(
                "/api/files/${Uri.encodeComponent(collectionIdOrName)}/${Uri.encodeComponent(record.id)}/${Uri.encodeComponent(filename)}",
                params
        );
    }
}
