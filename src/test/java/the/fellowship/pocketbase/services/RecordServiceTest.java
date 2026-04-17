package the.fellowship.pocketbase.services;

import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.MockClient;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.RecordAuth;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.tools.Json;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RecordService")
class RecordServiceTest extends CrudServiceTest<RecordModel> {
    RecordServiceTest() {
        super(
                (client) -> new RecordService(client, "@test_collection"),
                RecordModel::getId,
                "collections/%40test_collection/records"
        );
    }

    @Test
    @DisplayName("authWithPassword()")
    void authWithPassword() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    Json.encode(Map.of(
                                            "token", "test_token",
                                            "record", Map.of("id", "test_id")
                                    )),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("POST", request.method());
                    assertEquals(
                            "https://example.com/base/api/collections/test/auth-with-password?a=1&a=2&b=%40demo&expand=rel&fields=a",
                            request.url().toString()
                    );
                    assertEquals("789", request.header("test"));
                    assertEquals("test", request.header("Authorization"));

                    // Assert body
                    RequestBody body = request.body();
                    if (body != null) {
                        Buffer buffer = new Buffer();
                        try {
                            request.body().writeTo(buffer);
                            assertEquals(
                                    Json.encode(Map.of(
                                            "test_body", 123,
                                            "identity", "test_identity",
                                            "password", "test_password"
                                    )),
                                    buffer.readUtf8()
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final RecordAuth result = client.getCollection("test").authWithPassword(
                    "test_identity",
                    "test_password",
                    "rel",
                    "a",
                    Map.of(
                            "test", "789",
                            "Authorization", "test"
                    ),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    ),
                    Map.of("test_body", 123)
            );

            assertEquals("test_token", result.getToken());
            assertEquals("test_id", result.getRecord().getId());
            assertEquals("test_token", client.getAuthStore().getToken());
            assertEquals("test_id", client.getAuthStore().getRecord().getId());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }
}