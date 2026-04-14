package the.fellowship.pocketbase.sse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import the.fellowship.pocketbase.ClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very rudimentary streamed response http client wrapper compatible
 * with the SSE message format.
 * <p>
 * The client supports auto reconnect based on the `retry` event message value
 * (default to max 5 attempts with 5s cool down in between).
 * <p>
 * Example usage:
 * <p>
 * ```dart
 * final sse = SseClient("https://example.com")
 *
 * // subscribe to any message
 * sse.onMessage.listen((msg) {
 * print(msg);
 * });
 *
 * // subscribe to specific event(s) only
 * sse.onMessage.where((msg) => msg.event == "PB_CONNECT").listen((msg) {
 * print(msg);
 * });
 *
 * // close the connection and clean up any resources associated with it
 * sse.close();
 * ```
 */
public class SseClient {
    /**
     * List with default stepped retry timeouts (in ms).
     */
    private static final List<Integer> defaultRetryTimeouts = Arrays.asList(
            200,
            300,
            500,
            1000,
            1200,
            1500,
            2000
    );
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * Callback function that is triggered on client close.
     */
    private final Runnable onClose;
    /**
     * Callback function that is triggered on each error connect attempt.
     */
    private final Consumer<Throwable> onError;
    /**
     * The stream where you'll receive the parsed SSE event messages.
     */
    private final SubmissionPublisher<SseMessage> messageStreamController = new SubmissionPublisher<>();
    /**
     * The regex used to parse a single line of the streamed response message.
     */
    private final Pattern lineRegex = Pattern.compile("^(\\w+)[\\s:]+(.*)?$");
    private final String url;
    private final OkHttpClient httpClient;
    private ScheduledFuture<?> retryTimer;
    private int retryAttempts = 0;
    private int maxRetry = Integer.MAX_VALUE;

    /**
     * Indicates whether the client was closed.
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * The local streamed http response subscription.
     */
    private InputStream responseStreamSubscription;
    private Call httpCall;

    public SseClient(String url) {
        this(url, Integer.MAX_VALUE, () -> System.out.println("SSE Closed"), System.err::println);
    }

    /**
     * Initializes the client and connects to the provided url.
     */
    public SseClient(
            String url,
            int maxRetry,
            Runnable onClose,
            Consumer<Throwable> onError
    ) {
        this.url = url;
        this.maxRetry = maxRetry;
        this.onClose = onClose;
        this.onError = onError;
        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
        init();
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    public void setOnMessage(Flow.Subscriber<SseMessage> subscriber) {
        messageStreamController.subscribe(subscriber);
    }

    /**
     * Closes the client and cleans up any resources associated with it.
     * <p>
     * The method is also called internally on disconnect after
     * all allowed retry attempts have failed.
     * <p>
     * NB! After calling this method the client cannot be used anymore.
     */
    public void close() {
        if (isClosed.get()) {
            return; // already closed
        }

        isClosed.set(true);

        if (retryTimer != null) retryTimer.cancel(false);
        scheduler.shutdown();

        if (responseStreamSubscription != null) {
            try {
                responseStreamSubscription.close();
            } catch (IOException ignored) {

            }
        }
        httpCall.cancel();

        if (!messageStreamController.isClosed()) {
            messageStreamController.close();
        }

        onClose.run();
    }

    private void init() {
        if (isClosed.get()) {
            return; // already closed
        }

        HttpUrl url = HttpUrl.parse(this.url);
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "text/event-stream")
                .get()
                .build();

        httpCall = httpClient.newCall(request);
        httpCall.enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.err.println(new ClientException(url, false, -1, null, e.toString()));
                        close();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        SseMessage sseMessage = new SseMessage();
                        try {
                            if (response.code() >= 400) {
                                Map<String, ?> responseBody = new Gson().fromJson(response.body().string(), new TypeToken<Map<String, ?>>() {
                                }.getType());
                                throw new ClientException(url, response.code(), responseBody);
                            }

                            // resets
                            retryAttempts = 0;
                            sseMessage = new SseMessage();
                            if (responseStreamSubscription != null) responseStreamSubscription.close();

                            responseStreamSubscription = response.body().byteStream();
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStreamSubscription))) {
                                String line;
                                while ((line = reader.readLine()) != null && !isClosed.get()) {
                                    // message end detected
                                    if (line.isEmpty()) {
                                        messageStreamController.submit(sseMessage);
                                        sseMessage = new SseMessage(); // reset for the next chunk
                                        continue;
                                    }

                                    Matcher match = lineRegex.matcher(line);
                                    if (!match.matches()) {
                                        // ignore invalid lines
                                        // (some servers may send a different formatted line as a ping)
                                        continue;
                                    }

                                    String field = match.group(1);
                                    String value = match.group(2);

                                    switch (field) {
                                        case "id":
                                            sseMessage.setId(value);
                                            break;
                                        case "event":
                                            sseMessage.setEvent(value);
                                            break;
                                        case "retry":
                                            try {
                                                sseMessage.setRetry(Integer.parseInt(value));
                                            } catch (NumberFormatException ignored) {
                                                sseMessage.setRetry(0);
                                            }
                                            break;
                                        case "data":
                                            sseMessage.setData(value);
                                            break;
                                    }
                                }
                            }
                        } catch (IOException | ClientException e) {
                            // most likely the client failed to establish a connection with the server
                            onError.accept(e);
                            reconnect(sseMessage.getRetry());
                        } finally {
                            close();
                        }
                    }
                }
        );
    }

    private void reconnect(int retryTimeout) {
        if (scheduler.isShutdown()) return;

        if (retryAttempts >= maxRetry) {
            // no more retries
            close();
            return;
        }

        if (retryTimeout <= 0) {
            if (retryAttempts > defaultRetryTimeouts.size() - 1) {
                retryTimeout = defaultRetryTimeouts.getLast();
            } else {
                retryTimeout = defaultRetryTimeouts.get(retryAttempts);
            }
        }

        // cancel previous timer (if any)

        if (retryTimer != null) retryTimer.cancel(true);

        retryTimer = scheduler.schedule(() -> {
            retryAttempts++;
            init();
        }, retryTimeout, TimeUnit.MILLISECONDS);
    }
}
