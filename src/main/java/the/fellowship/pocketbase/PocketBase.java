package the.fellowship.pocketbase;

import okhttp3.*;
import the.fellowship.pocketbase.services.RecordService;
import the.fellowship.pocketbase.tools.SendOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PocketBase {
    private final OkHttpClient client;

    /**
     * The base PocketBase backend url address (eg. '<a href="http://127.0.0.1.8090">http://127.0.0.1.8090</a>').
     */
    private final String baseURL;
    /**
     * Optional language code (default to `en-US`) that will be sent
     * with the requests to the server as `Accept-Language` header.
     */
    private String lang = "en-US";

    private final Map<String, RecordService> recordServices = new HashMap<>();

    public PocketBase(String baseURL, String lang) {
        this(baseURL);
        this.lang = lang;
    }

    public PocketBase(String baseURL) {
        this.client = new OkHttpClient();
        this.baseURL = baseURL;
    }

    /**
     * Returns the RecordService associated to the specified collection.
     */
    public RecordService collection(String idOrName) {
        if (!this.recordServices.containsKey(idOrName)) {
            this.recordServices.put(idOrName, new RecordService(this, idOrName));
        }

        return this.recordServices.get(idOrName);
    }

    /**
     * Builds a full client url by safely concatenating the provided path.
     */
    private HttpUrl buildURL(String path) {
        return HttpUrl.get(this.baseURL + path);
    }

    /**
     * Sends an api http request.
     *
     * @throws {ClientResponseError}
     */
    public String send(String path, SendOptions options) {
        options = initSendOptions(path, options);

        HttpUrl url = this.buildURL(path);

        Request request = new Request(
                url,
                !options.getHeaders().isEmpty() ? Headers.of(options.getHeaders()) : Headers.EMPTY,
                !options.getMethod().isEmpty() ? options.getMethod() : "GET",
                options.getBody() != null ? RequestBody.create(options.getBody().toString(), MediaType.parse("application/json")) : null
        );

        try (Response response = this.client.newCall(request).execute()) {
            if (response.code() >= 400) {
                throw new RuntimeException(String.format(
                        "[ClientResponseError]\nURL: %s, STATUS: %s, DATA:\n%s",
                        url,
                        response.code(),
                        response.body().string()
                ));
            }

            return response.body().string();
        } catch (IOException e) {
            System.err.println("Connection Error!");
        }
        return null;
    }

    /**
     * Shallow copy the provided object and takes care to initialize
     * any options required to preserve the backward compatability.
     *
     * @param {SendOptions} options
     * @return {SendOptions}
     */
    private SendOptions initSendOptions(String path, SendOptions options) {
        return options;
    }
}
