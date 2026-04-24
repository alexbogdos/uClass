package the.fellowship.eclass;

import okhttp3.*;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import the.fellowship.Json;
import the.fellowship.eclass.cookies.FileCookieJar;
import the.fellowship.eclass.dtos.Assignment;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EClass {
    private final String service;
    private final String agent;
    private final FileCookieJar cookieJar;
    private final EClassSSO sso;
    OkHttpClient httpClient;

    /**
     * @param serviceURL
     */
    public EClass(String serviceURL) {
        this.service = serviceURL;
        this.agent = UseAgentGenerator.generate();
        this.sso = new EClassSSO(this);
        this.cookieJar = new FileCookieJar();
        this.httpClient = new OkHttpClient.Builder()
                // TODO: Custom CookieStore. Extract cookie loading/parsing/storing
                .cookieJar(new JavaNetCookieJar(this.cookieJar.getCookieManager()))
                .build();
    }

    /**
     * Testing only.
     */
    protected EClass(String serviceURL, Interceptor interceptor) {
        this.service = serviceURL;
        this.agent = UseAgentGenerator.generate();
        this.sso = new EClassSSO(this);
        this.cookieJar = new FileCookieJar();
        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(this.cookieJar.getCookieManager()))
                .addInterceptor(interceptor)
                .build();
    }

    public FileCookieJar getCookieJar() {
        return cookieJar;
    }

    protected EClassSSO getSSO() {
        return sso;
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  HTML Parsers
     * - - - - - - - - - - - - - - - - - - - - */

    public CompletableFuture<List<Map<String, String>>> getCourses() {
        return get("/main/portfolio.php?countPages=-1")
                .thenApply(response -> {
                    String html = (String) response.get("body");
                    if (html.isEmpty()) return new ArrayList<>();

                    Document document = Jsoup.parse(html);
                    List<Map<String, String>> courses = document.select(".row-course").stream().map(course -> {
                        Element link = course.selectFirst("a");
                        return Map.of(
                                "url", link.attr("href"),
                                "title", link.text()
                        );
                    }).toList();
                    return courses;
                });
    }

    public CompletableFuture<List<Assignment>> getAssignments(Instant start, Instant end) {
        return getAssignments(null, start, end);
    }

    public CompletableFuture<List<Assignment>> getAssignments(String courseId, Instant start, Instant end) {
        final String url = String.format("/main/calendar_data.php?from=%s&to=%s", start.getEpochSecond() * 1000, end.getEpochSecond() * 1000);

        return get(url)
                .thenApply(response -> {
                    String json = (String) response.get("body");
                    if (json.isEmpty()) return new ArrayList<>();

                    Map<String, ?> events = Json.decode(json);

                    return ((List<Map<String, ?>>) events.get("result")).stream()
                            .filter(event -> {
                                // Calendar event is not an assignment
                                if (!"assignment".equals(event.get("event_type"))) return false;

                                if (courseId == null || courseId.isEmpty()) return true;
                                return courseId.equals(event.get("course"));
                            })
                            .map(Assignment::new)
                            .toList();
                });
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  SSO handler
     * - - - - - - - - - - - - - - - - - - - - */

    /**
     * @param username
     * @param password
     * @return <b>True</b> if the credentials authenticated the user successfully
     */
    public CompletableFuture<String> login(String username, String password) {
        return sso.login(username, password);
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  GET/POST Request handler
     * - - - - - - - - - - - - - - - - - - - - */

    /**
     * Sends an api http GET/ request.
     *
     * @param path
     */
    protected CompletableFuture<Map<String, ?>> get(String path) {
        return send(HttpUrl.parse(this.service + path), null);
    }

    /**
     * Sends an api http request.
     *
     * @param url
     * @param body
     */
    protected CompletableFuture<Map<String, ?>> send(
            HttpUrl url,
            RequestBody body
    ) {
        Request.Builder request = new Request.Builder()
                .url(url)
                .header("User-Agent", agent);

        if (body != null) {
            request.post(body);
        }

        final CompletableFuture<Map<String, ?>> future = new CompletableFuture<>();

        httpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //System.err.printf("[ERROR] Unable to make request %s/ %s. %s\n", body != null ? "POST" : "GET", url, e.getMessage());
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new ClientException(url, response.code(), response.body().string()));
                }

                future.complete(Map.of(
                        "url", response.request().url(),
                        "body", response.body().string()
                ));
            }
        });

        return future;
    }
}
