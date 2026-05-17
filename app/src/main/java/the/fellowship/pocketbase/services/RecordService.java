package the.fellowship.pocketbase.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordAuth;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.dtos.RecordSubscriptionEvent;

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
        return String.format("/api/collections/%s", URLEncoder.encode(this.collectionIdOrName, StandardCharsets.UTF_8));
    }

    @Override
    public String getBaseCrudPath() {
        return getBaseCollectionPath() + "/records";
    }

    @Override
    RecordModel itemFactory(Map<String, ?> json) {
        return new RecordModel(json);
    }

    @Override
    Map<String, ?> itemEncoder(RecordModel item) {
        return item.getJson();
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
            Consumer<RecordSubscriptionEvent> callback
    ) throws ClientException {
        return subscribe(topic, callback, null, null, null, null, null);
    }

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
            String filter,
            Consumer<RecordSubscriptionEvent> callback
            ) throws ClientException {
        return subscribe(topic, callback, null, filter, null, null, null);
    }

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
    ) throws ClientException {
        return client.getRealtime().subscribe(
                String.format("%s/%s", collectionIdOrName, topic),
                (message) -> callback.accept(new RecordSubscriptionEvent(message.getJsonData())),
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
    public void unsubscribe() throws ClientException {
        unsubscribe("");
    }

    /**
     * Unsubscribe from all subscriptions of the specified topic
     * ("*" or record id).
     * <p>
     * If [topic] is not set, then this method will unsubscribe from
     * all subscriptions associated to the current collection.
     */
    public void unsubscribe(String topic) throws ClientException {
        if (topic != null && !topic.isEmpty()) {
            client.getRealtime().unsubscribe(String.format("%s/%s", collectionIdOrName, topic));
            return;
        }

        client.getRealtime().unsubscribeByPrefix(collectionIdOrName);
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  Auth collection handlers
     * - - - - - - - - - - - - - - - - - - - - */

    /**
     * Prepare successful record authentication response.
     */
    private RecordAuth authResponse(Map<String, ?> data) {
        final RecordAuth auth = new RecordAuth(data);
        client.getAuthStore().save(auth.getToken(), auth.getRecord());
        return auth;
    }

    public RecordAuth authWithPassword(String usernameOrEmail, String password) throws ClientException {
        return authWithPassword(usernameOrEmail, password, null, null, null, null, null);
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
    public RecordAuth authWithPassword(
            String usernameOrEmail,
            String password,
            String expand,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body
    ) throws ClientException {
        final Map<String, Object> enrichedBody = query != null ? new TreeMap<>(body) : new TreeMap<>();
        enrichedBody.put("identity", usernameOrEmail);
        enrichedBody.put("password", password);

        final Map<String, Object> enrichedQuery = query != null ? new TreeMap<>(query) : new TreeMap<>();
        if (expand != null && !expand.isEmpty()) enrichedQuery.put("expand", expand);
        if (fields != null && !fields.isEmpty()) enrichedQuery.put("fields", fields);

        final Map<String, ?> response = this.client.send(
                this.getBaseCollectionPath() + "/auth-with-password",
                "POST",
                headers,
                enrichedQuery,
                enrichedBody,
                null
        );

        return authResponse(response);
    }
}
