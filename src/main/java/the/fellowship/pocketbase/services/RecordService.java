package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordService extends BaseCrudService{
    private final String collectionIdOrName;

    public RecordService(PocketBase client, String collectionIdOrName) {
        super(client);
        this.collectionIdOrName = collectionIdOrName;
    }

    /**
     * Returns the current collection service base path.
     */
    private String getBaseCollectionPath() {
        return "/api/collections/" + this.collectionIdOrName;
    }

    @Override
    String getBaseCrudPath() {
        return getBaseCollectionPath() + "/records";
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

        Map<String, ?> response = this.client.send(
                this.getBaseCollectionPath() + "/auth-with-password",
                "POST",
                null,
                null,
                body,
                null
        );

        this.client.getAuthStore().save((String) response.get("token"));

        return response;
    }

    // TODO: Subscribe to realtime changes
}
