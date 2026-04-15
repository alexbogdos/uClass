package the.fellowship.pocketbase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import the.fellowship.pocketbase.services.RealtimeService;
import the.fellowship.pocketbase.services.RecordService;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PocketBase {
    /**
     * The shared HTTP client instance that is used when the
     * `reuseHTTPClient` constructor argument is set.
     */
    private final OkHttpClient client;

    /**
     * The base PocketBase backend url address (eg. '<a href="http://127.0.0.1.8090">http://127.0.0.1.8090</a>').
     */
    private final String baseURL;

    /**
     * An instance of the local [AuthStore] service.
     */
    private final AuthStore authStore;

    /**
     * An instance of the service that handles the **Realtime APIs**.
     * <p>
     * This service is usually used with custom realtime actions.
     * For records realtime subscriptions you can use the subscribe/unsubscribe
     * methods available in the `collection()` RecordService.
     */
    private final RealtimeService realtime;

    /**
     * The shared HTTP client instance that is used when the
     * `reuseHTTPClient` constructor argument is set.
     */
    private final Map<String, RecordService> recordServices = new HashMap<>();

    /**
     * Optional language code (default to `en-US`) that will be sent
     * with the requests to the server as `Accept-Language` header.
     */
    private String lang = "en-US";

    public PocketBase(String baseURL, String lang) {
        this(baseURL);
        this.lang = lang;
    }

    public PocketBase(String baseURL) {
        this.client = new OkHttpClient();
        this.baseURL = baseURL;
        this.authStore = new AuthStore();
        this.realtime = new RealtimeService(this);
    }

    public RealtimeService getRealtime() {
        return realtime;
    }

    /**
     * Returns the RecordService associated to the specified collection.
     */
    public RecordService getCollection(String idOrName) {
        if (!this.recordServices.containsKey(idOrName)) {
            this.recordServices.put(idOrName, new RecordService(this, idOrName));
        }

        return this.recordServices.get(idOrName);
    }

    public AuthStore getAuthStore() {
        return authStore;
    }

    /**
     * Builds a full client url by safely concatenating the provided path.
     */
    public HttpUrl buildURL(String path) {
        return buildURL(path, new HashMap<>());
    }

    /**
     * Builds a full client url by safely concatenating the provided path.
     */
    public HttpUrl buildURL(String path, Map<String, ?> query) {
        String url = baseURL + (baseURL.endsWith("/") ? "" : "/");

        if (!path.isEmpty()) {
            url += path.startsWith("/") ? path.substring(1) : path;
        }

        // TODO Create `normalizeQueryParameters`
        //query = _normalizeQueryParameters(queryParameters);
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        for (String name : query.keySet()) {
            if (query.get(name) == null) continue;
            builder.addQueryParameter(name, String.valueOf(query.get(name)));
        }

        // TODO: Replace `queryParameters`
        //return HttpUrl.parse(url).replace(queryParameters: query.isNotEmpty ? query : null);
        return builder.build();
    }

    /**
     * Sends an api http request.
     *
     * @throws ClientException
     */
    public Map<String, ?> send(
            String path,
            String method,
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body,
            List<MultipartFile> files
    ) throws ClientException {
        if (method == null) {
            method = "GET";
        }
        if (headers == null) {
            headers = new HashMap<>();
        }
        if (query == null) {
            query = new HashMap<>();
        }
        if (body == null) {
            body = new HashMap<>();
        }

        HttpUrl url = this.buildURL(path, query);

        Request.Builder request = new Request.Builder();

        if (files == null) {
            request = jsonRequest(method, url, headers, body);
        } else {
            request = multipartRequest(method, url, headers, body, files);
        }

        if (!headers.containsKey("Authorization") && authStore.isValid()) {
            request.header("Authorization", authStore.getToken());
        }

        if (!headers.containsKey("Accept-Language")) {
            request.header("Accept-Language", lang);
        }

        //System.out.printf("[REQUEST] %s, %s, %s\n", request.build(), body, files);
        try (Response response = this.client.newCall(request.build()).execute()) {
            Map<String, ?> responseBody = new Gson().fromJson(response.body().string(), new TypeToken<Map<String, ?>>() {
            }.getType());
            if (response.code() >= 400) {
                throw new ClientException(url, response.code(), responseBody);
            }
            return responseBody;
        } catch (IOException e) {
            throw new ClientException(url, false, -1, null, e.toString());
        }
    }

    private Request.Builder jsonRequest(
            String method,
            HttpUrl url,
            Map<String, String> headers,
            Map<String, ?> body
    ) {
        Request.Builder request = new Request.Builder()
                .url(url);

        if (!body.isEmpty()) {
            request.method(method, RequestBody.create(
                    jsonEncode(body),
                    MediaType.parse("application/json")
            ));
        } else {
            request.method(method, null);
        }

        if (!headers.isEmpty()) {
            request.headers(Headers.of(headers));
        }

        if (!headers.containsKey("Content-Type")) {
            request.header("Content-Type", "application/json");
        }

        return request;
    }

    private Request.Builder multipartRequest(
            String method,
            HttpUrl url,
            Map<String, String> headers,
            Map<String, ?> body,
            List<MultipartFile> files
    ) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("@jsonPayload", jsonEncode(body));

        for (MultipartFile file : files) {
            RequestBody fileBody = RequestBody.create(
                    file.getFile(),
                    file.getType() != null ? file.getType() : MediaType.parse("application/octet-stream")
            );
            requestBody.addFormDataPart(
                    file.getField(),
                    file.getName(),
                    fileBody
            );
        }

        Request.Builder request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .method(method, requestBody.build());

        return request;
    }

    public String jsonEncode(Map<String, ?> body) {
        Gson gson = new Gson();
        Type typeObject = new TypeToken<Map<String, ?>>() {
        }.getType();
        return gson.toJson(body, typeObject);
    }
}
