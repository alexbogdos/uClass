package the.fellowship.eclass;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.Environment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("eClass")
class SSOTests {
    final String service = "https://eclass.aueb.gr";
    EClass client;

    // TODO: Intercept requests, write tests for failing paths

    @BeforeEach
    void setUp() {
        client = new EClass(service);
    }

    @Test
    @DisplayName("send()")
    void send() {
        client.send(HttpUrl.parse(service), null)
                .thenAccept(result -> {
                            assertNotNull(result);
                            assertNotNull(result.get("url"));
                            assertNotNull(result.get("body"));

                            assertInstanceOf(HttpUrl.class, result.get("url"));
                            assertInstanceOf(String.class, result.get("body"));

                            assertEquals("https://eclass.aueb.gr/", ((HttpUrl) result.get("url")).toString());
                            assertFalse(((String) result.get("body")).isEmpty());
                        }
                )
                .join();
    }

    @Test
    @DisplayName("retrieveExecutionTicket()")
    void retrieveExecutionTicket() {
        client.getSSO().retrieveExecutionTicket()
                .thenAccept(result -> {
                    assertNotNull(result);
                    assertNotNull(result.get("url"));
                    assertNotNull(result.get("token"));

                    assertInstanceOf(HttpUrl.class, result.get("url"));
                    assertInstanceOf(String.class, result.get("token"));

                    assertEquals(
                            "https://sso.aueb.gr/login?service=https%3A%2F%2Feclass.aueb.gr%2Fmodules%2Fauth%2Fcas.php",
                            ((HttpUrl) result.get("url")).toString()
                    );
                    assertFalse(((String) result.get("token")).isEmpty());
                    assertEquals(5797, ((String) result.get("token")).length());
                })
                .join();
    }

    @Test
    @DisplayName("authenticate()")
    void authenticate() {
        final Map<String, String> env = Environment.load(".env");

        client.getSSO().retrieveExecutionTicket()
                .thenCompose(execution -> client.getSSO().authenticate(
                        env.get("username"),
                        env.get("password"),
                        (HttpUrl) execution.get("url"),
                        (String) execution.get("token")
                ))
                .thenAccept(result -> assertTrue(result))
                .join();
    }

    @Test
    @DisplayName("login()")
    void login() {
        final Map<String, String> env = Environment.load(".env");

        client.login(env.get("username"), env.get("password"))
                .thenAccept(result -> {
                    assertTrue("SUCCESS".equals(result) || "RESTORE".equals(result));
                })
                //.handle((res, err) -> {
                //    if (err != null) System.err.printf("ERROR: %s [%s/%s]\n", err.getMessage() ,err.getClass(), err.getCause().getClass());
                //    return null;
                //})
                .join();
    }
}