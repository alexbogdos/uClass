package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.sse.SseClient;
import the.fellowship.pocketbase.sse.SseMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class RealtimeService extends BaseService {
    private final Map<String, List<Consumer<SseMessage>>> subscriptions = new HashMap<>();
    private SseClient sse;
    private String clientId = "";
    /**
     * An optional hook that is invoked when the realtime client disconnects
     * either when unsubscribing from all subscriptions or when the
     * connection was interrupted or closed by the server.
     * <p>
     * It receives the subscriptions map before the disconnect
     * (could be used to determine whether the disconnect was caused by
     * unsubscribing or network/server error).
     * <p>
     * If you want to listen for the opposite, aka. when the client
     * connection is established, subscribe to the `PB_CONNECT` event.
     */
    private Consumer<Map<String, List<Consumer<SseMessage>>>> onDisconnect;

    public RealtimeService(PocketBase client) {
        super(client);
    }

    /**
     * Returns the established SSE connection client id (if any).
     */
    public String getClientId() {
        return clientId;
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
    public Runnable subscribe(
            String topic,
            Consumer<SseMessage> listener,
            String expand,
            String filter,
            String fields,
            Map<String, String> headers,
            Map<String, ?> query
    ) throws ClientException {
        String key = topic;

        // merge query parameters
        Map<String, Object> enrichedQuery = query != null ? new HashMap<>(query) : new HashMap<>();
        if (expand != null && !expand.isEmpty()) {
            enrichedQuery.put("expand", expand);
        }
        if (filter != null && !filter.isEmpty()) {
            enrichedQuery.put("filter", filter);
        }
        if (fields != null && !fields.isEmpty()) {
            enrichedQuery.put("fields", fields);
        }

        // serialize and append the topic options (if any)
        Map<String, Object> options = new HashMap<>();
        if (!enrichedQuery.isEmpty()) {
            options.put("query", enrichedQuery);
        }
        if (headers != null && !headers.isEmpty()) {
            options.put("headers", headers);
        }
        if (!options.isEmpty()) {
            try {
                String encodedQuery = new URI(null, null, null, client.jsonEncode(options), null).getQuery();
                System.out.printf("Encoded Query: %s\n", new URI(null, null, null, client.jsonEncode(options), null).getQuery());
                System.out.printf("Encoded Query Raw: %s\n", new URI(null, null, null, client.jsonEncode(options), null).getRawQuery());
                String encoded = String.format("options=%s}", encodedQuery);
                key += (key.contains("?") ? "&" : "?") + encoded;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        if (!subscriptions.containsKey(key)) {
            subscriptions.put(key, new ArrayList<>());
        }
        subscriptions.get(key).add(listener);

        // start a new sse connection
        if (sse == null) {
            connect();
        } else if (!clientId.isEmpty() && subscriptions.get(key).size() == 1) {
            // otherwise - just persist the updated subscriptions
            // (if it is the first for the topic)
            submitSubscriptions();
        }

        return () -> {
            try {
                unsubscribeByTopicAndListener(topic, listener);
            } catch (ClientException e) {
                System.err.println(e);
            }
        };
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
    public Map<String, ?> unsubscribe(String topic) throws ClientException {
        var needToSubmit = false;

        if (topic.isEmpty()) {
            // remove all subscriptions
            subscriptions.clear();
        } else {
            Map<String, List<Consumer<SseMessage>>> subs = getSubscriptionsByTopic(topic);
            for (String key : subs.keySet()) {
                subscriptions.remove(key);
                needToSubmit = true;
            }
        }

        // no other subscriptions -> close the sse connection
        if (!hasNonEmptyTopic()) {
            disconnect();
            return null;
        }

        // otherwise - notify the server about the subscription changes
        if (!clientId.isEmpty() && needToSubmit) {
            submitSubscriptions();
            return null;
        }
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
    public void unsubscribeByPrefix(String topicPrefix) throws ClientException {
        int beforeLength = subscriptions.size();


        // remove matching subscriptions
        for (String key : subscriptions.keySet()) {
            if (String.format("%s?", key).startsWith(topicPrefix)) {
                subscriptions.remove(key);
            }
        }

        // no changes
        if (beforeLength == subscriptions.size()) {
            return;
        }

        // no other subscriptions -> close the sse connection
        if (!hasNonEmptyTopic()) {
            disconnect();
            return;
        }

        // otherwise - notify the server about the subscription changes
        if (!clientId.isEmpty()) {
            submitSubscriptions();
        }
    }

    /**
     * Unsubscribe from all subscriptions matching the specified topic
     * and listener function.
     * <p>
     * This method is no-op if there are no active subscription with
     * the specified topic and listener.
     * <p>
     * The related sse connection will be autoclosed if after the
     * unsubscribe operation there are no active subscriptions left.
     */
    private void unsubscribeByTopicAndListener(
            String topic,
            Consumer<SseMessage> listener
    ) throws ClientException {
        boolean needToSubmit = false;

        Map<String, List<Consumer<SseMessage>>> subs = getSubscriptionsByTopic(topic);

        for (String key : subs.keySet()) {
            if (!subscriptions.containsKey(key) || subscriptions.get(key).isEmpty()) {
                continue; // nothing to unsubscribe from
            }

            int beforeLength = subscriptions.containsKey(key) ? subscriptions.get(key).size() : 0;

            subscriptions.get(key).removeIf(fn -> fn == listener);

            int afterLength = subscriptions.containsKey(key) ? subscriptions.get(key).size() : 0;

            // no changes
            if (beforeLength == afterLength) {
                continue;
            }

            // mark for subscriptions change submit if there are no other listeners
            if (!needToSubmit && afterLength == 0) {
                needToSubmit = true;
            }
        }

        // no other subscriptions -> close the sse connection
        if (!hasNonEmptyTopic()) {
            disconnect();
        }

        // otherwise - notify the server about the subscription changes
        // (if there are no other subscriptions in the topic)
        if (!clientId.isEmpty() && needToSubmit) {
            submitSubscriptions();
        }
    }

    private Map<String, List<Consumer<SseMessage>>> getSubscriptionsByTopic(String topic) {
        Map<String, List<Consumer<SseMessage>>> result = new HashMap<>();

        // "?" so that it can be used as end delimiter for the topic
        String finalTopic = topic.contains("?") ? topic : String.format("%s?", topic);

        subscriptions.forEach((key, value) -> {
            if (String.format("%s?", key).startsWith(finalTopic)) {
                result.put(key, value);
            }
        });

        return result;
    }

    private boolean hasNonEmptyTopic() {
        for (String key : subscriptions.keySet()) {
            if (subscriptions.containsKey(key) && !subscriptions.get(key).isEmpty()) {
                return true; // has at least one listener
            }
        }

        return false;
    }

    private CompletableFuture<Void> connect() {
        disconnect();

        CompletableFuture<Void> completer = new CompletableFuture<>();

        String url = client.buildURL("/api/realtime").toString();

        SseClient sse = new SseClient(
                url,
                Integer.MAX_VALUE,
                () -> {
                    if (!clientId.isEmpty() && onDisconnect != null) {
                        onDisconnect.accept(subscriptions);
                    }

                    disconnect();

                    if (!completer.isDone()) {
                        completer.completeExceptionally(new IllegalStateException("failed to establish SSE connection"));
                    }
                },
                (err) -> {
                    if (!clientId.isEmpty() && onDisconnect != null) {
                        clientId = "";
                        onDisconnect.accept(subscriptions);
                    }
                }
        );

        this.sse = sse;

        // bind subscriptions listener
        sse.setOnMessage(
                new Flow.Subscriber<>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        this.subscription.request(1);
                    }

                    @Override
                    public void onNext(SseMessage message) {
                        this.subscription.request(1);
                        if (!subscriptions.containsKey(message.getEvent())) {
                            return;
                        }

                        subscriptions.get(message.getEvent()).forEach(fn -> fn.accept(message));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Subscription listener completed");
                    }
                }
        );


        // resubmit local subscriptions on first reconnect
        sse.setOnMessage(
                new Flow.Subscriber<>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        this.subscription.request(1);
                    }

                    @Override
                    public void onNext(SseMessage message) {
                        this.subscription.request(1);
                        if (!message.getEvent().equals("PB_CONNECT")) {
                            return;
                        }

                        try {
                            clientId = message.getId();
                            submitSubscriptions();

                            if (!completer.isDone()) {
                                completer.complete(null);
                            }
                        } catch (Exception e) {
                            disconnect();
                            if (!completer.isDone()) {
                                completer.completeExceptionally(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        disconnect();
                        if (!completer.isDone()) {
                            completer.completeExceptionally(throwable);
                        }
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Resubmission listener completed");
                    }
                }
        );

        return completer;
    }

    private void disconnect() {
        if (sse != null) sse.close();
        sse = null;
        clientId = "";
    }

    Map<String, ?> submitSubscriptions() throws ClientException {
        Map<String, ?> body = Map.of(
                "clientId", clientId,
                "subscriptions", subscriptions.keySet().toArray()
        );

        return client.send(
                "/api/realtime",
                "POST",
                null,
                null,
                body,
                null
        );
    }
}
