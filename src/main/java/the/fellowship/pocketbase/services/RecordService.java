package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordService {
    private final PocketBase client;
    private final String collectionIdOrName;

    public RecordService(PocketBase client, String collectionIdOrName) {
        this.client = client;
        this.collectionIdOrName = collectionIdOrName;
    }

    /**
     * Returns the current collection service base path.
     */
    private String baseCollectionPath() {
        return "/api/collections/" + encodeURIComponent(this.collectionIdOrName);
    }

    /**
     * Encodes a text string as a valid component of a Uniform Resource Identifier (URI).
     *
     * @param uriComponent A value representing an unencoded URI component.
     */
    private String encodeURIComponent(String uriComponent) {
        return uriComponent;
    }

    /**
     * Authenticate a single auth collection record via its username/email and password.
     * <p>
     * On success, this method also automatically updates
     * the client's AuthStore data and returns:
     * - the authentication token
     * - the authenticated record model
     *
     * @throws ClientException
     */
    public Map<String, ?> authWithPassword(String usernameOrEmail, String password) throws ClientException {
        Map<String, String> body = new HashMap<>();
        body.put("identity", usernameOrEmail);
        body.put("password", password);

        return this.client.send(
                this.baseCollectionPath() + "/auth-with-password",
                "POST",
                null,
                null,
                body,
                null
        );
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
                this.baseCollectionPath() + "/records",
                "POST",
                headers,
                query,
                body,
                files
        );
    }
}
