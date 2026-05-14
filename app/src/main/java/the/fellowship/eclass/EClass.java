package the.fellowship.eclass;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import the.fellowship.Json;
import the.fellowship.eclass.cookies.CookieJar;
import the.fellowship.eclass.cookies.FileCookieJar;
import the.fellowship.eclass.cookies.PrefsCookieJar;
import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.eclass.dtos.Assignment;
import the.fellowship.eclass.dtos.Course;

public class EClass {
    private final String service;
    private final String agent;
    private final CookieJar cookieJar;
    private final EClassSSO sso;
    /**
     * Cached data
     */
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>();
    private final MutableLiveData<List<Announcement>> announcements = new MutableLiveData<>();
    OkHttpClient httpClient;

    /**
     * @param serviceURL
     */
    public EClass(String serviceURL, PrefsCookieJar cookieJar) {
        this.service = serviceURL;
        this.cookieJar = cookieJar;
        this.agent = UseAgentGenerator.generate();
        this.sso = new EClassSSO(this);
        this.httpClient = new OkHttpClient.Builder()
                // TODO: Custom CookieStore. Extract cookie loading/parsing/storing
                .cookieJar(new JavaNetCookieJar(this.cookieJar.getCookieManager()))
                .build();
    }

    public EClass(String serviceURL) {
        this.service = serviceURL;
        this.cookieJar = new FileCookieJar();
        this.agent = UseAgentGenerator.generate();
        this.sso = new EClassSSO(this);
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

    public String getService() {
        return service;
    }

    public CookieJar getCookieJar() {
        return cookieJar;
    }

    protected EClassSSO getSSO() {
        return sso;
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  HTML Parsers
     * - - - - - - - - - - - - - - - - - - - - */

    public void fetchAll() {
        Log.d("EClass", "Fetch all");
        fetchCourses().thenAccept(list -> {
            courses.postValue(list);
            Log.d("EClass", String.format("Received courses: %s", list));
        });
        fetchAnnouncements().thenAccept(list -> {
            announcements.postValue(list);
            Log.d("EClass", String.format("Received announcements: %s", list));
        });
    }

    public LiveData<List<Course>> getCourses() {
        if (courses.getValue() == null) {
            fetchCourses().thenAccept(courses::postValue);
        }
        return courses;
    }

    private CompletableFuture<List<Course>> fetchCourses() {
        return get("/main/portfolio.php?countPages=-1")
                .thenApply(response -> {
                    String html = (String) response.get("body");
                    if (html.isEmpty()) return new ArrayList<>();

                    Document document = Jsoup.parse(html);
                    return document.select(".row-course").stream().map(course -> {
                        Element link = course.selectFirst("a");
                        String id = course.selectFirst("div").selectFirst("small").text();
                        String lecturer = course.select("div").get(1).selectFirst("small").text();
                        return new Course(id, link.text(), lecturer, link.attr("href"));
                    }).collect(Collectors.toList());
                });
    }


    public LiveData<List<Announcement>> getAnnouncements() {
        if (announcements.getValue() == null) {
            fetchAnnouncements().thenAccept(announcements::postValue);
        }
        return announcements;
    }

    private CompletableFuture<List<Announcement>> fetchAnnouncements() {
        return get("/modules/announcements/myannouncements.php")
                .thenApply(response -> {
                    String json = (String) response.get("body");
                    if (json.isEmpty()) return new ArrayList<>();

                    List<List<String>> items = (List<List<String>>) Json.decode(json).get("aaData");
                    if (items == null) return new ArrayList<>();

                    List<Announcement> announcements = new ArrayList<>(items.size());
                    for (List<String> an : items) {
                        String content = an.get(0);
                        String date = an.get(1);

                        Document document = Jsoup.parse(content);
                        Element link = document.selectFirst(".table_td_header").selectFirst("a");
                        String course = document.selectFirst("small").text();
                        String body = document.selectFirst(".table_td_body").text();
                        announcements.add(new Announcement(link.text(), course, date, (link.attr("href")), body));
                    }

                    return announcements;
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
                            .collect(Collectors.toList());
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
    public CompletableFuture<Boolean> login(String username, String password) {
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
                .header("User-Agent", agent)
                .header("X-Requested-With", "XMLHttpRequest");

        if (body != null) {
            request.post(body);
        }

        final CompletableFuture<Map<String, ?>> future = new CompletableFuture<>();

        httpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //System.err.printf("[ERROR] Unable to make request %s/ %s. %s\n", body != null ? "POST" : "GET", url, e.getMessage());
                future.completeExceptionally(new ClientException(url, e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("EClass", String.valueOf(new ClientException(url, response.code(), response.body().string())));
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
