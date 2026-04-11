package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.sse.SseMessage;

import java.util.Map;
import java.util.function.Consumer;

public class RealtimeService extends BaseService {
    public RealtimeService(PocketBase client) {
        super(client);
    }

    /**
     * Register the subscription listener.
     * <p>
     * You can subscribe multiple times to the same topic.
     * <p>
     * If the SSE connection is not started yet,
     * this method will also initialize it.
     * <p>
     * Here is an example listening to the connect/reconnect events:
     * <p>
     * ```java
     * pb.getRealtime().subscribe("PB_CONNECT", (e) -> {
     * print("Connected: $e");
     * });
     * ```
     */
    Runnable subscribe(
            String topic,
            Consumer<SseMessage> listener,
            String expand,
            String filter,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) {
        return () -> {};
    }

    /**
     * Unsubscribe from all subscription listeners with the specified topic.
     * <p>
     * If [topic] is not set, then this method will unsubscribe
     * from all active subscriptions.
     * <p>
     * This method is no-op if there are no active subscriptions.
     * <p>
     * The related sse connection will be autoclosed if after the
     * unsubscribe operation there are no active subscriptions left.
     */
    Map<String, ?> unsubscribe(String topic) {
        return null;
    }

    /**
     * Unsubscribe from all subscription listeners starting with
     * the specified topic prefix.
     * <p>
     * This method is no-op if there are no active subscriptions
     * with the specified topic prefix.
     * <p>
     * The related sse connection will be autoclosed if after the
     * unsubscribe operation there are no active subscriptions left.
     */
    Map<String, ?> unsubscribeByPrefix(String topicPrefix) {
        return null;
    }
}
