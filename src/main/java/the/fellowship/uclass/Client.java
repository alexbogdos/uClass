package the.fellowship.uclass;

import okhttp3.*;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Client {
    private final static String cookiePath = "";

    private final String service;
    private final String agent;
    private CookieManager cookieManager;
    OkHttpClient client;

    /**
     * @param serviceURL
     */
    public Client(String serviceURL) {
        this.service = serviceURL;
        this.agent = UseAgentGenerator.generate();
        this.cookieManager = new CookieManager();
        //this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.client = new OkHttpClient.Builder()
                // TODO: Custom CookieStore. Extract cookie loading/parsing/storing
                .cookieJar(new JavaNetCookieJar(this.cookieManager))
                .build();
    }

    /**
     * Testing only.
     */
    protected Client(String serviceURL, Interceptor interceptor) {
        this.service = serviceURL;
        this.agent = UseAgentGenerator.generate();
        this.cookieManager = new CookieManager();
        //this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(this.cookieManager))
                .addInterceptor(interceptor)
                .build();
    }


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

        client.newCall(request.build()).enqueue(new Callback() {
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

    /**
     * @param username
     * @param password
     * @return <b>True</b> if the credentials authenticated the user successfully
     */
    public CompletableFuture<String> login(String username, String password) {
        String cookieFile = cookiePath + String.format("%s_cookies.pkl", username);

        // Load CookieStore from file
        loadCookies(cookieFile);

        // Check if the current session is already logged in
        return get("/modules/auth/cas.php")
                .thenCompose((response) -> {
                    // Check if the current session is already logged in
                    if (!((HttpUrl) response.get("url")).toString().contains("/login")) {
                        return CompletableFuture.completedFuture("RESTORE");
                    }

                    // Obtain SSO's execution ticket
                    return retrieveExecutionTicket()
                            // Authenticate to SSO using the credentials and the execution ticket
                            .thenCompose(res -> authenticate(username, password, (HttpUrl) res.get("url"), (String) res.get("token")))
                            .thenApply(res -> {
                                if (res) {
                                    // Store current CookieStore to file
                                    storeCookies(cookieFile);
                                }
                                return res ? "SUCCESS" : "FAILURE";
                            });
                });
    }

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

    /**
     * @return a map containing the SSO's URL <b>["url"]</b> to authenticate to and the execution token <b>["token"]</b> contained in the HTML page
     */
    protected CompletableFuture<Map<String, ?>> retrieveExecutionTicket() {
        return get("/modules/auth/cas.php")
                .thenApply(response -> {
                    String html = (String) response.get("body");
                    if (html.isEmpty()) return null;

                    Document document = Jsoup.parse(html);
                    String token = document.select("input[name=execution]").val();
                    if (token.isEmpty()) return null;

                    return Map.of(
                            "url", response.get("url"),
                            "token", token
                    );
                });
    }

    /**
     * @param username
     * @param password
     * @param url      Service URL
     * @param token    Execution token
     * @return <b>True</b> if the authentication was successful
     */
    protected CompletableFuture<Boolean> authenticate(String username, String password, HttpUrl url, String token) {
        RequestBody form = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("execution", token)
                .add("_eventId", "submit")
                .build();

        return send(url, form)
                .thenApply(res -> !((HttpUrl) res.get("url")).toString().contains("/login"));
    }

    private void loadCookies(String path) {
        if (!new File(path).exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                HttpCookie cookie = parseCookie(line);
                URI uri = URI.create(cookie.getDomain() + cookie.getPath());
                this.cookieManager.getCookieStore().add(uri, cookie);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load cookies from file");
        }
    }

    private static HttpCookie parseCookie(String line) {
        String[] row = line.split(";");
        List<String> pairs = new ArrayList<>();
        for (String column : row) {
            String[] pair = column.split("=");
            pairs.add(pair[0]);
            pairs.add(pair[1]);
        }
        HttpCookie cookie = new HttpCookie(pairs.get(0), pairs.get(1));
        cookie.setDomain(pairs.get(3));
        cookie.setPath(pairs.get(5));
        cookie.setMaxAge(Integer.parseInt(pairs.get(7)));
        return cookie;
    }

    private void storeCookies(String path) {
        List<HttpCookie> cookies = this.cookieManager.getCookieStore().getCookies();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (HttpCookie cookie : cookies) {
                writer.write(String.format("%s=%s;$Path=%s;$Domain=%s;$Expires=%s\n", cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge()));
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save cookies to file");
        }
    }
}
