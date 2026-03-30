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

    // TODO: Use a singe point for requests (similar to PocketBase.send())
    public String get(String path) {
        Request request = new Request.Builder()
                .url(this.service + path)
                .header("User-Agent", agent)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;

            return response.body().string();
        } catch (IOException e) {
            System.err.println("Connection Error!");
            return null;
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
            System.err.println("Failed to retrieve execution ticket");
            return false;
        }

        // Authenticate to SSO using the credentials and the execution ticket
        boolean authenticated = authenticate(username, password, (HttpUrl) response.get("Location"), (String) response.get("Token"));
        if (!authenticated) {
            System.err.println("Failed to authenticate");
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
        Request request = new Request.Builder()
                .url(this.service + "/modules/auth/cas.php")
                .header("User-Agent", agent)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;

            String html = response.body().string();
            if (html.isEmpty()) return null;

            Document document = Jsoup.parse(html);
            String token = document.select("input[name=execution]").val();
            if (token.isEmpty()) return null;

            return Map.of("Location", response.request().url(), "Token", token);
        } catch (IOException e) {
            System.err.println("Connection Error!");
            return null;
        }
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

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", agent)
                .post(form)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return false;
            return !response.request().url().toString().contains("/login");
        } catch (IOException e) {
            System.err.println("Connection Error!");
            return false;
        }
    }
}
