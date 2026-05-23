package the.fellowship.eclass;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.CookieManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import the.fellowship.Json;
import the.fellowship.eclass.dtos.Announcement;
import the.fellowship.eclass.dtos.Assignment;
import the.fellowship.eclass.dtos.Course;
import the.fellowship.eclass.dtos.Lecturer;

public class EClass {
    private final String service;
    private final String agent;
    private final EClassSSO sso;
    private final SharedPreferences prefs;

    /**
     * Cached data
     */
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>();
    private final MutableLiveData<List<Announcement>> announcements = new MutableLiveData<>();
    private final MutableLiveData<List<Assignment>> assignments = new MutableLiveData<>();
    private final MutableLiveData<String> notification = new MutableLiveData<>();
    OkHttpClient httpClient;

    /**
     * @param serviceURL
     */
    public EClass(String serviceURL, SharedPreferences prefs) {
        this.service = serviceURL;
        this.prefs = prefs;
        this.agent = UseAgentGenerator.generate();
        this.sso = new EClassSSO(this);
        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .build();
    }

    /**
     * Testing only.
     */
    protected EClass(String serviceURL, Interceptor interceptor) {
        this.prefs = null;
        this.service = serviceURL;
        this.agent = UseAgentGenerator.generate();
        this.sso = new EClassSSO(this);
        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .addInterceptor(interceptor)
                .build();
    }

    public String getService() {
        return service;
    }

    protected EClassSSO getSSO() {
        return sso;
    }

    public Course getCourse(String id) {
        for (Course course : courses.getValue()) {
            if (id.equals(course.getId())) {
                return course;
            }
        }
        return null;
    }

    public Announcement getAnnouncement(int id) {
        for (Announcement announcement : announcements.getValue()) {
            if (id == announcement.getId()) {
                return announcement;
            }
        }
        return null;
    }

    public List<Announcement> getAnnouncements(String courseId) {
        return announcements.getValue().stream().filter(an -> courseId.equals(an.getCourseId())).collect(Collectors.toList());
    }

    public LiveData<String> getNotification() {
        return notification;
    }

    public void postNotification(String notification) {
        this.notification.postValue(notification);
    }

    /* - - - - - - - - - - - - - - - - - - - -
     *  HTML Parsers
     * - - - - - - - - - - - - - - - - - - - - */

    public void fetchNetwork() {
        Log.d("EClass", "Fetch all..");

        fetchCourses().thenAccept(list -> {
            courses.postValue(list);
            Log.d("EClass", String.format("Received courses: %s", list));

            // Receive all lecturers to cache them
            CompletableFuture.allOf(list.stream().map(course -> fetchLecturerDetails(course.getId())).toArray(CompletableFuture[]::new)).thenAccept(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("cache:courses", Json.encode(list));
                editor.apply();
            });
        });

        fetchAnnouncements().thenAccept(list -> {
            announcements.postValue(list);
            Log.d("EClass", String.format("Received announcements: %s", list));

            final int latestId = list.stream().max(Comparator.comparingInt(Announcement::getId)).get().getId();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("cache:announcements", Json.encode(list));
            editor.putInt("cache:announcements_latest_id", latestId);
            editor.apply();

            if (latestId > Announcement.getLatestId()) {
                notification.postValue("Έχετε νέες ανακοινώσεις");
            }
        });

        fetchAssignments(LocalDateTime.now(), LocalDateTime.now().plusMonths(6)).thenAccept(list -> {
            assignments.postValue(list);
            Log.d("EClass", String.format("Received assignments: %s", list));

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("cache:assignments", Json.encode(list));
            editor.putInt("cache:assignments_latest_id", list.stream().max(Comparator.comparingInt(Assignment::getId)).get().getId());
            editor.apply();
        });
    }

    public void fetchCached() {
        Log.d("EClass", "Fetch Cached..");

        if (prefs.contains("cache:courses")) {
            List<Course> cachedCourses = Json.decode(prefs.getString("cache:courses", "{}"), new TypeToken<List<Course>>() {});
            Log.d("EClass", String.format("Cached courses: %s", cachedCourses));
            courses.postValue(cachedCourses);
        }

        if (prefs.contains("cache:announcements")) {
            List<Announcement> cachedAnnouncements = Json.decode(prefs.getString("cache:announcements", "{}"), new TypeToken<List<Announcement>>() {});
            Log.d("EClass", String.format("Cached announcements: %s", cachedAnnouncements));
            announcements.postValue(cachedAnnouncements);

            if (prefs.contains("cache:announcements_latest_id")) {
                //Announcement.setLatestId(prefs.getInt("cache:announcements_latest_id", 0));
            }
        }

        if (prefs.contains("cache:assignments")) {
            List<Assignment> cachedAssignments = Json.decode(prefs.getString("cache:assignments", "{}"), new TypeToken<List<Assignment>>() {});
            Log.d("EClass", String.format("Cached assignments: %s", cachedAssignments));
            assignments.postValue(cachedAssignments);

            if (prefs.contains("cache:assignments_latest_id")) {
                Assignment.setLatestId(prefs.getInt("cache:assignments_latest_id", 0));
            }
        }
    }

    public LiveData<List<Course>> getCourses() {
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
                        String id = link.attr("href").split("/")[4];
                        String lecturer = course.select("div").get(1).selectFirst("small").text();
                        return new Course(id, link.text(), new Lecturer(lecturer), link.attr("href"));
                    }).collect(Collectors.toList());
                });
    }

    public LiveData<List<Announcement>> getAnnouncements() {
        return announcements;
    }

    private CompletableFuture<List<Announcement>> fetchAnnouncements() {
        return get("/modules/announcements/myannouncements.php")
                .thenApply(response -> {
                    String json = (String) response.get("body");
                    if (json.isEmpty()) return new ArrayList<>();

                    List<List<String>> _announcements = (List<List<String>>) Json.decode(json).get("aaData");
                    if (_announcements == null) return new ArrayList<>();

                    return _announcements.stream().map(an -> {
                        String content = an.get(0);
                        String date = an.get(1);

                        Document document = Jsoup.parse(content);
                        Element link = document.selectFirst(".table_td_header").selectFirst("a");
                        String id = link.attr("href").split("\\?")[1].split("&")[1].split("=")[1];
                        String course = document.selectFirst("small").text();
                        String courseId = link.attr("href").split("\\?")[1].split("&")[0].split("=")[1];
                        String body = document.selectFirst(".table_td_body").text();

                        return new Announcement(Integer.parseInt(id), link.text(), course, courseId, date, link.attr("href"), body);
                    }).collect(Collectors.toList());
                });
    }

    public CompletableFuture<Lecturer> fetchLecturerDetails(String courseId) {
        Lecturer lecturer = getCourse(courseId).getLecturer();
        if (lecturer.getEmail() != null) {
            return CompletableFuture.completedFuture(lecturer);
        }

        return send(HttpUrl.parse("https://www.dept.aueb.gr/el/content/CS_OfficeHours"), null)
                .thenApply(response -> {
                    String name = lecturer.getName();

                    String html = (String) response.get("body");
                    if (html.isEmpty()) return lecturer;

                    Document document = Jsoup.parse(html);
                    Element table = document.selectFirst("tbody");

                    for (Element row : table.getElementsByTag("tr")) {
                        if (row.selectFirst("td").text().contains(name)) {
                            List<Element> columns = row.select("td");

                            if (columns.get(1).selectFirst("a") != null) {
                                lecturer.setEmail(columns.get(1).selectFirst("a").text());
                            }
                            if (!columns.get(2).html().isEmpty()) {
                                lecturer.setHours(String.join(", ", Arrays.stream(columns.get(2).html().split("<br>")).map((el -> Jsoup.parse(el).text())).collect(Collectors.toList())));
                            }
                            if (columns.get(1).html().contains("<br>")) {
                                lecturer.setOffice(Jsoup.parse(columns.get(1).html().split("<br>")[0]).text());
                            }
                            break;
                        }
                    }
                    return lecturer;
                });
    }

    public LiveData<List<Assignment>> getAssignments() {
        return assignments;
    }

    public CompletableFuture<List<Assignment>> fetchAssignments(LocalDateTime start, LocalDateTime end) {
        return fetchAssignments(null, start, end);
    }

    public CompletableFuture<List<Assignment>> fetchAssignments(String courseId, LocalDateTime start, LocalDateTime end) {
        final ZoneId zone = ZoneId.of("Europe/Athens");

        final ZonedDateTime startDate = start.atZone(zone);
        final ZonedDateTime endDate = end.atZone(zone);

        final long start_ms = startDate.toInstant().getEpochSecond() * 1000;
        final long end_ms = endDate.toInstant().getEpochSecond() * 1000;

        final String url = String.format("/main/calendar_data.php?from=%s&to=%s&utc_offset_from=-180&utc_offset_to=-180", start_ms, end_ms);
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
                Log.e("EClass/Send", String.valueOf(new ClientException(url, e)));
                future.completeExceptionally(new ClientException(url, e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    //Log.e("EClass", String.valueOf(new ClientException(url, response.code(), response.body().string())));
                    future.completeExceptionally(new ClientException(url, response.code(), ""));
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
