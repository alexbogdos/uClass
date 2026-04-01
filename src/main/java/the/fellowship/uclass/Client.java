package the.fellowship.uclass;

import okhttp3.*;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
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

public class Client {
    private final static String cookiePath = "/home/shollow/.cache/";

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
                .cookieJar(new JavaNetCookieJar(this.cookieManager))
                .build();
    }

    /**
     * Sends an api http GET/ request.
     *
     * @param path
     */
    public Map<String, ?> get(String path) {
        return this.send(HttpUrl.parse(this.service + path), null);
    }

    /**
     * Sends an api http request.
     *
     * @param url
     * @param body
     */
    private Map<String, ?> send(
            HttpUrl url,
            RequestBody body
    ) {

        Request.Builder request = new Request.Builder()
                .url(url)
                .header("User-Agent", agent);

        if (body != null) {
            request.post(body);
        }

        try (Response response = client.newCall(request.build()).execute()) {
            return Map.of(
                    "code", response.code(),
                    "successful", response.isSuccessful(),
                    "url", response.request().url(),
                    "body", response.body().string()
            );
        } catch (IOException e) {
            System.err.printf("[ERROR] Unable to make request %s/ %s. %s\n", body != null ? "POST" : "GET", url, e.getMessage());
            return null;
        }
    }

    /**
     * @param username
     * @param password
     * @return <b>True</b> if the credentials authenticated the user successfully
     */
    public boolean login(String username, String password) {
        String cookieFile = cookiePath + String.format("%s_cookies.pkl", username);
        Map<String, ?> response;

        // Load CookieStore from file
        loadCookies(cookieFile);

        // Check if the current session is already logged in
        response = get("/modules/auth/cas.php");
        if (response != null && !((HttpUrl) response.get("url")).toString().contains("/login")) {
            System.out.printf("Already authenticated as \"%s\"\n\n", username);
            return true;
        }

        // Obtain SSO's execution ticket
        response = retrieveExecutionTicket();
        if (response == null) {
            return false;
        }

        // Authenticate to SSO using the credentials and the execution ticket
        boolean authenticated = authenticate(username, password, (HttpUrl) response.get("url"), (String) response.get("token"));
        if (!authenticated) {
            return false;
        }

        // Store current CookieStore to file
        storeCookies(cookieFile);

        System.out.printf("Successfully authenticated as \"%s\"\n\n", username);
        return true;
    }

    public List<Map<String, String>> courses() {
        Map<String, ?> response = get("/main/portfolio.php?countPages=-1");
        if (response == null) {
            return null;
        }

        String html = (String) response.get("body");
        Document document = Jsoup.parse(html);
        List<Map<String, String>> courses = document.select(".row-course").stream().map(course -> {
            Element link = course.selectFirst("a");
            return Map.of(
                    "url", link.attr("href"),
                    "title", link.text()
            );
        }).toList();
        return courses;
    }

    /**
     * @return a map containing the SSO's URL <b>["url"]</b> to authenticate to and the execution token <b>["token"]</b> contained in the HTML page
     */
    private Map<String, ?> retrieveExecutionTicket() {
        Map<String, ?> response = get("/modules/auth/cas.php");
        if (response == null) {
            return null;
        }

        String html = (String) response.get("body");
        if (html.isEmpty()) {
            return null;
        }

        Document document = Jsoup.parse(html);
        String token = document.select("input[name=execution]").val();
        if (token.isEmpty()) {
            return null;
        }

        return Map.of(
                "url", response.get("url"),
                "token", token
        );
    }

    /**
     * @param username
     * @param password
     * @param url      Service URL
     * @param token    Execution token
     * @return <b>True</b> if the authentication was successful
     */
    private boolean authenticate(String username, String password, HttpUrl url, String token) {
        RequestBody form = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("execution", token)
                .add("_eventId", "submit")
                .build();

        Map<String, ?> response = send(url, form);
        if (response == null) {
            return false;
        }

        return !((HttpUrl) response.get("url")).toString().contains("/login");
    }

    private void loadCookies(String path) {
        if (!new File(path).exists()) {
            System.err.printf("[WARNING] Cookie file not found on \"%s\"\n", path);
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
