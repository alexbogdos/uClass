package the.fellowship.pocketbase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import the.fellowship.pocketbase.services.RecordService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
     * Optional language code (default to `en-US`) that will be sent
     * with the requests to the server as `Accept-Language` header.
     */
    private String lang = "en-US";

    /**
     * The shared HTTP client instance that is used when the
     * `reuseHTTPClient` constructor argument is set.
     */
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
    private HttpUrl buildURL(String path, Map<String, ?> query) {
        String url = baseURL + (baseURL.endsWith("/") ? "" : "/");

        if (!path.isEmpty()) {
            url += path.startsWith("/") ? path.substring(1) : path;
        }

        // TODO Create `normalizeQueryParameters`
        //query = _normalizeQueryParameters(queryParameters);

        // TODO: Replace `queryParameters`
        //return HttpUrl.parse(url).replace(queryParameters: query.isNotEmpty ? query : null);
        return HttpUrl.parse(url);
    }

    /**
     * Sends an api http request.
     *
     * @throws {ClientResponseError}
     */
    public String send(
            String path,
            String method,
            Map<String, String> headers,
            Map<String, ?> query,
            Map<String, ?> body,
            List<MultipartBody> files
    ) {
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
        if (files == null) {
            files = new ArrayList<>();
        }

        HttpUrl url = this.buildURL(path, query);

        Request.Builder request = new Request.Builder();

        if (files.isEmpty()) {
            request = jsonRequest(method, url, headers, body);
        } else {
            //request = multipartRequest(method, url, headers, body, files);
        }

        // TODO: Create `AuthStore`
        //if (!headers.containsKey("Authorization") && authStore.isValid) {
        //    request.header("Authorization") = authStore.token;
        //}

        if (!headers.containsKey("Accept-Language")) {
            request.header("Accept-Language", lang);
        }

        try (Response response = this.client.newCall(request.build()).execute()) {
            if (response.code() >= 400) {
                throw new RuntimeException(String.format(
                        "\n[ClientResponseError]\nURL: %s, STATUS: %s, DATA:\n%s",
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

    Request.Builder jsonRequest(
            String method,
            HttpUrl url,
            Map<String, String> headers,
            Map<String, ?> body
    ) {
        Request.Builder request = new Request.Builder()
                .url(url);

        if (!body.isEmpty()) {
            Gson gson = new Gson();
            Type typeObject = new TypeToken<HashMap<String, ?>>() {
            }.getType();
            String json = gson.toJson(body, typeObject);
            request.method(method, RequestBody.create(json, MediaType.parse("application/json")));
        } else {
            request.get();
        }

        if (!headers.isEmpty()) {
            request.headers(Headers.of(headers));
        }

        if (!headers.containsKey("Content-Type")) {
            request.header("Content-Type", "application/json");
        }

        return request;
    }

    // TODO: `multipartRequest`
    //MultipartRequest _multipartRequest(
    //        String method,
    //        Uri url, {
    //    Map<String, String> headers = const{
    //    },
    //    Map<String, dynamic> body = const{
    //    },
    //    List<http.MultipartFile> files = const [],
    //})
    //
    //{
    //    final request =MultipartRequest(method, url)
    //        ..files.addAll(files)
    //        ..headers.addAll(headers);
    //
    //    request.fields["@jsonPayload"] = [jsonEncode(body)];
    //
    //    return request;
    //}
}
