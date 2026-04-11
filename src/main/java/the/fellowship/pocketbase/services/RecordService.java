package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordAuth;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.dtos.RecordSubscriptionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RecordService extends BaseCrudService<RecordModel> {
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

    @Override
    RecordModel itemFactory(Map<String, ?> json) {
        return new RecordModel(json);
    }

    /* - - - - - - - - - - - - - - - - - - - -
     * Realtime handlers
     * - - - - - - - - - - - - - - - - - - - - */

    /**
     * Subscribe to realtime changes to the specified topic ("*" or record id).
     * <p>
     * If [topic] is the wildcard "*", then this method will subscribe to
     * any record changes in the collection.
     * <p>
     * If [topic] is a record id, then this method will subscribe only
     * to changes of the specified record id.
     * <p>
     * It's OK to subscribe multiple times to the same topic.
     * <p>
     * You can use the returned [UnsubscribeFunc] to remove the subscription.
     * Or use [unsubscribe(topic)] if you want to remove all
     * subscriptions attached to the topic.
     */
    public Runnable subscribe(
            String topic,
            Consumer<RecordSubscriptionEvent> callback,
            String expand,
            String filter,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) {
        return client.getRealtime().subscribe(
                String.format("%s/%s", collectionIdOrName, topic),
                (e) -> callback.accept(new RecordSubscriptionEvent(e.getJson())),
                expand,
                filter,
                fields,
                headers,
                query
        );
    }

    /**
     * Unsubscribe from all subscriptions of the specified topic
     * ("*" or record id).
     * <p>
     * If [topic] is not set, then this method will unsubscribe from
     * all subscriptions associated to the current collection.
     */
    Map<String, ?> unsubscribe(String topic) {
        if (!topic.isEmpty()) {
            return client.getRealtime().unsubscribe(String.format("%s/%s", collectionIdOrName, topic));
        }

        return client.getRealtime().unsubscribeByPrefix(collectionIdOrName);
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  Auth collection handlers
     * - - - - - - - - - - - - - - - - - - - - */

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
    public RecordAuth authWithPassword(String usernameOrEmail, String password) throws ClientException {
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

        this.client.getAuthStore().save(
                (String) response.get("token"),
                new RecordModel((Map<String, ?>) response.get("record"))
        );

        return new RecordAuth(response);
    }
}
