package the.fellowship.eclass;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EClassSSO {
    private final EClass client;

    public EClassSSO(EClass client) {
        this.client = client;
    }

    /**
     * @param username
     * @param password
     * @return <b>True</b> if the credentials authenticated the user successfully
     */
    public CompletableFuture<Boolean> login(String username, String password) {
        client.getCookieJar().setKey(username);

        // Load CookieStore from file
        client.getCookieJar().load();

        // Check if the current session is already logged in
        return client.get("/modules/auth/cas.php")
                .thenCompose((response) -> {
                    // Check if the current session is already logged in
                    if (!((HttpUrl) response.get("url")).toString().contains("/login")) {
                        return CompletableFuture.completedFuture(true);
                    }

                    // Obtain SSO's execution ticket
                    return retrieveExecutionTicket()
                            // Authenticate to SSO using the credentials and the execution ticket
                            .thenCompose(res -> authenticate(username, password, (HttpUrl) res.get("url"), (String) res.get("token")))
                            .thenApply(res -> {
                                if (res) {
                                    // Store current CookieStore to file
                                    client.getCookieJar().store();
                                }
                                return res;
                            });
                });
    }

    /**
     * @return a map containing the SSO's URL <b>["url"]</b> to authenticate to and the execution token <b>["token"]</b> contained in the HTML page
     */
    protected CompletableFuture<Map<String, ?>> retrieveExecutionTicket() {
        return client.get("/modules/auth/cas.php")
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

        return client.send(url, form)
                .thenApply(res -> !((HttpUrl) res.get("url")).toString().contains("/login"));
    }
}
