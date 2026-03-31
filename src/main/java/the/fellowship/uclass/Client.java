package the.fellowship.uclass;

import okhttp3.*;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.CookieManager;
import java.util.Map;

public class Client {
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

    public Map<String, ?> get(String path) {
        return this.send(path, null);
    }

    private Map<String, ?> send(
            String path,
            RequestBody body
    ) {
        return send(HttpUrl.parse(this.service + path), body);
    }

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
            return Map.of("successful", false);
        }
    }

    /**
     * @param username
     * @param password
     * @return <b>True</b> if the credentials authenticated the user successfully
     */
    public boolean login(String username, String password) {
        // TODO: Load CookieStore from file `username_cookies.pkl`

        // TODO: Check if the current session is already logged in

        // Obtain SSO's execution ticket
        Map<String, ?> response = retrieveExecutionTicket();
        if (response == null) {
            return false;
        }

        // Authenticate to SSO using the credentials and the execution ticket
        boolean authenticated = authenticate(username, password, (HttpUrl) response.get("url"), (String) response.get("token"));
        if (!authenticated) {
            return false;
        }

        // TODO: Store current CookieStore to file `username_cookies.pkl`

        System.out.printf("Successfully authenticated as \"%s\"\n", username);
        return true;
    }

    /**
     * @return a map containing the SSO's URL <b>["Location"]</b> to authenticate to and the execution token <b>["Token"]</b> contained in the HTML page
     */
    private Map<String, ?> retrieveExecutionTicket() {
        Map<String, ?> response = get("/modules/auth/cas.php");
        if (!(boolean) response.get("successful")) {
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
        if (!(boolean) response.get("successful")) {
            return false;
        }

        return !((HttpUrl) response.get("url")).toString().contains("/login");
    }
}
