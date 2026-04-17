package the.fellowship.pocketbase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import the.fellowship.pocketbase.services.RealtimeService;
import the.fellowship.pocketbase.services.RecordService;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;
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
    private final Map<String, RecordService> recordServices = new TreeMap<>();

    /**
     * Optional language code (default to `en-US`) that will be sent
     * with the requests to the server as `Accept-Language` header.
     */
    private String lang;

    public PocketBase(String baseURL) {
        this(baseURL, "en-US", new AuthStore());
    }

    public PocketBase(String baseURL, String lang) {
        this(baseURL, lang, new AuthStore());
    }

    protected PocketBase(String baseURL, String lang, AuthStore authStore) {
        this.client = new OkHttpClient();
        this.baseURL = baseURL;
        this.authStore = authStore;
        this.realtime = new RealtimeService(this);
        this.lang = lang;
    }

    /**
     * Testing only.
     */
    public PocketBase(String baseURL, String lang, Interceptor interceptor) {
        this.client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        this.baseURL = baseURL;
        this.authStore = new AuthStore();
        this.realtime = new RealtimeService(this);
        this.lang = lang;
    }

    protected String getBaseURL() {
        return baseURL;
    }

    protected String getLang() {
        return lang;
    }

    public AuthStore getAuthStore() {
        return authStore;
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

    public String filter(String expr) {
        return filter(expr, null);
    }

    /**
     * Constructs a filter expression with placeholders populated from a map.
     * <p>
     * Placeholder parameters are defined with the `{:paramName}` notation.
     * <p>
     * The following parameter values are supported:
     * - `String` (_single quotes are autoescaped_)
     * - `num`
     * - `bool`
     * - `DateTime`
     * - `null`
     * - everything else is converted to a string using `jsonEncode()`
     * <p>
     * Example:
     * <p>
     * ```dart
     * pb.collection("example").getList(filter: pb.filter(
     * "title ~ {:title} && created >= {:created}",
     * { "title": "example", "created": DateTime.now() },
     * ));
     * ```
     */
    public String filter(
            String expr,
            Map<String, ?> query
    ) {
        if (query == null || query.isEmpty()) {
            return expr;
        }

        for (String key : query.keySet()) {
            Object value = query.get(key);
            String valueString;

            if (value == null) {
                valueString = "null";
            } else if (value instanceof Number || value instanceof Boolean) {
                valueString = value.toString();
            } else if (value instanceof Instant) {
                valueString = String.format("'%s'", ((Instant) value)
                        .atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT)
                        .replace("T", " "));
            } else if (value instanceof String) {
                valueString = String.format("'%s'", ((String) value).replace("'", "\\'"));
            } else {
                valueString = String.format("'%s'", new Gson().toJson(value).replace("\\u0027", "\\'"));
            }
            expr = expr.replace(String.format("{:%s}", key), valueString);
        }

        return expr;
    }

    /**
     * Builds a full client url by safely concatenating the provided path.
     */
    public HttpUrl buildURL(String path) {
        return buildURL(path, null);
    }

    /**
     * Builds a full client url by safely concatenating the provided path.
     */
    public HttpUrl buildURL(String path, Map<String, ?> query) {
        String url = baseURL + (baseURL.endsWith("/") ? "" : "/");

        if (!path.isEmpty()) {
            url += path.startsWith("/") ? path.substring(1) : path;
        }

        if (query == null || query.isEmpty()) {
            return HttpUrl.get(url);
        }

        Map<String, List<String>> normalizedQuery = normalizeQueryParameters(query);
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        for (String name : normalizedQuery.keySet()) {
            if (normalizedQuery.get(name) == null) continue;

            for (String parameter : normalizedQuery.get(name)) {
                builder.addQueryParameter(name, parameter);
            }
        }

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
            headers = new TreeMap<>();
        }
        if (query == null) {
            query = new TreeMap<>();
        }
        if (body == null) {
            body = new TreeMap<>();
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
        // TODO: Use [enqueue()] instead of [execute()]
        try (Response response = this.client.newCall(request.build()).execute()) {
            Map<String, ?> responseBody = new GsonBuilder()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create()
                    .fromJson(response.body().string(), new TypeToken<Map<String, ?>>() {
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

            // TODO: Do not use File. Replace [MultipartFile] with [MultipartBody.Part]
            RequestBody fileBody;
            if (file.hasFile()) {
                fileBody = RequestBody.create(
                        file.getFile(),
                        file.getType() != null ? file.getType() : MediaType.parse("application/octet-stream")
                );
            } else {
                fileBody = RequestBody.create(
                        file.getContent(),
                        MediaType.parse("application/octet-stream")
                );
            }

            requestBody.addFormDataPart(
                    file.getField(),
                    file.getName(),
                    fileBody
            );
        }

        Request.Builder request = new Request.Builder()
                .url(url)
                .method(method, requestBody.build());

        if (!headers.isEmpty()) {
            request.headers(Headers.of(headers));
        }

        if (!headers.containsKey("Content-Type")) {
            request.header("Content-Type", "multipart/form-data");
        }

        return request;
    }

    public static String jsonEncode(Map<String, ?> body) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        Type typeObject = new TypeToken<Map<String, ?>>() {}.getType();
        return gson.toJson(new TreeMap<>(body), typeObject);
    }

    private Map<String, List<String>> normalizeQueryParameters(Map<String, ?> parameters) {
        Map<String, List<String>> result = new TreeMap<>();

        for (String key : parameters.keySet()) {
            Object value = parameters.get(key);

            List<String> normalizedValue = new ArrayList<>();

            // TODO: Rewrite

            // convert to List to normalize access
            if (value instanceof Iterable) {
                for (Object v : (Iterable<?>) value) {
                    if (v == null) continue; // skip null query params
                    normalizedValue.add(String.valueOf(v));
                }
            } else if (value != null && value.getClass().isArray()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object v = Array.get(value, i);
                    if (v == null) continue;
                    normalizedValue.add(String.valueOf(v));
                }
            } else {
                if (value != null) {
                    normalizedValue.add(String.valueOf(value));
                }
            }

            if (!normalizedValue.isEmpty()) {
                result.put(key, normalizedValue);
            }
        }

        return result;
    }
}
