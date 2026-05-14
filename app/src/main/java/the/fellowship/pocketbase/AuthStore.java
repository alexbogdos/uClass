package the.fellowship.pocketbase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

import the.fellowship.Json;
import the.fellowship.pocketbase.dtos.RecordModel;

/**
 * Base authentication store management service that keep tracks of
 * the authenticated User/Admin model and its token.
 */
public class AuthStore {
    private final SubmissionPublisher<AuthStoreEvent> onChangeController = new SubmissionPublisher<>();

    private String token = "";
    private RecordModel record;

    /**
     * Returns the saved auth token (if any).
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the saved auth record (if any).
     */
    public RecordModel getRecord() {
        return record;
    }

    /**
     * Stream that gets triggered on each auth store change
     * (aka. on [save()] and [clear()] call).
     */
    public void setOnChange(Consumer<AuthStoreEvent> consumer) {
        onChangeController.subscribe(new Flow.Subscriber<AuthStoreEvent>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(AuthStoreEvent authStoreEvent) {
                this.subscription.request(1);
                consumer.accept(authStoreEvent);
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * Loosely checks if the current AuthStore has valid auth data
     * (eg. whether the token is expired or not).
     */
    public boolean isValid() {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        byte[] tokenPart = Base64.getDecoder().decode(parts[1]);
        String jsonString = new String(tokenPart, StandardCharsets.UTF_8);

        Map<String, ?> data = Json.decode(jsonString);
        long exp = data.get("exp") != null ? ((Number) data.get("exp")).longValue() : 0;
        return exp > System.currentTimeMillis() / 1000;
    }

    /**
     * Saves the provided [newToken] and [newRecord] auth data into the store.
     */
    public void save(String newToken, RecordModel newRecord) {
        this.token = newToken;
        this.record = newRecord;

        onChangeController.submit(new AuthStoreEvent(this.token, this.record));
    }

    /**
     * Clears the previously stored [token] and [record] auth data.
     */
    public void clear() {
        this.token = "";
        this.record = null;

        onChangeController.submit(new AuthStoreEvent(this.token, this.record));
    }
}
