package the.fellowship.pocketbase;

import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.services.RecordService;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class MockAuthStore extends AuthStore {
}

class PocketBaseTest {
    @Nested
    @DisplayName("PocketBase()")
    class ConstructorTests {
        @Test
        @DisplayName("with defaults")
        void withDefaults() {
            final PocketBase client = new PocketBase("https://example.com");

            assertEquals("https://example.com", client.getBaseURL());
            assertEquals("en-US", client.getLang());
            assertNotNull(client.getAuthStore());

            // services
            assertNotNull(client.getRealtime());
        }

        @Test
        @DisplayName("with opt fields")
        void withOptFields() {
            final PocketBase client = new PocketBase(
                    "https://example.com",
                    "test_lang",
                    new MockAuthStore()
            );

            assertEquals("https://example.com", client.getBaseURL());
            assertEquals("test_lang", client.getLang());
            assertInstanceOf(MockAuthStore.class, client.getAuthStore());
        }
    }

    @Nested
    @DisplayName("PocketBase.getCollection()")
    class CollectionTests {
        @Test
        @DisplayName("initializing different RecordServices")
        void initializingDifferentRecordServices() {
            final PocketBase client = new PocketBase("https://example.com");

            RecordService service1 = client.getCollection("test1");
            RecordService service2 = client.getCollection("@test2");
            RecordService service3 = client.getCollection("test1"); // same as service1

            assertEquals("/api/collections/test1/records", service1.getBaseCrudPath());
            assertEquals("/api/collections/%40test2/records", service2.getBaseCrudPath());
            assertEquals("/api/collections/test1/records", service3.getBaseCrudPath());
        }
    }

    @Nested
    @DisplayName("PocketBase.filter()")
    class FilterTests {
        @Test
        @DisplayName("filter expression without params")
        void filterExpressionWithoutParams() {
            final PocketBase client = new PocketBase("https://example.com");

            String expr = "a > {:test1} && b = {:test2} || c = {:test2}";

            assertEquals(expr, client.filter(expr));
        }

        @Test
        @DisplayName("filter expression with partial missing placeholders")
        void filterExpressionWithPartialMissingPlaceholders() {
            final PocketBase client = new PocketBase("https://example.com");

            String expr = "a > {:test1} && b = {:test2} || c = {:test2}";

            assertEquals(
                    "a > {:test1} && b = 'hello' || c = 'hello'",
                    client.filter(expr, Map.of("test2", "hello"))
            );
        }

        @Test
        @DisplayName("filter expression with all placeholder types")
        void filterExpressionWithAllPlaceholderTypes() {
            final PocketBase client = new PocketBase("https://example.com");

            Map<String, Object> params = new TreeMap<>();
            params.put("test1", "a'b'c'");
            params.put("test2", null);
            params.put("test3", true);
            params.put("test4", false);
            params.put("test5", 123);
            params.put("test6", -123.45);
            params.put("test7", 123.45);
            params.put("test8", ZonedDateTime.of(2023, 10, 18, 10, 11, 12, 0, ZoneOffset.UTC).toInstant());
            params.put("test9", new Object[]{1, 2, 3, "test'123"});
            params.put("test10", Map.of("a", "test'123"));

            StringBuilder expr = new StringBuilder();
            for (String key : params.keySet()) {
                if (!expr.isEmpty()) {
                    expr.append(" || ");
                }
                expr.append(String.format("%s={:%s}", key, key));
            }

            assertEquals(
                    "test1='a\\'b\\'c\\'' || test10='{\"a\":\"test\\'123\"}' || test2=null || test3=true || test4=false || test5=123 || test6=-123.45 || test7=123.45 || test8='2023-10-18 10:11:12Z' || test9='[1,2,3,\"test\\'123\"]'",
                    client.filter(expr.toString(), params)
            );
        }
    }

    @Nested
    @DisplayName("PocketBase.buildURL()")
    class BuildURlTests {
        @Test
        @DisplayName("baseURL with trailing slash")
        void baseURLWithTrailingSlash() {
            final PocketBase client = new PocketBase("https://example.com");

            assertEquals("https://example.com/test", client.buildURL("test").toString());
            assertEquals("https://example.com/test", client.buildURL("/test").toString());
        }

        @Test
        @DisplayName("baseURL without trailing slash")
        void baseURLWithoutTrailingSlash() {
            final PocketBase client = new PocketBase("https://example.com");

            assertEquals("https://example.com/test", client.buildURL("test").toString());
            assertEquals("https://example.com/test", client.buildURL("/test").toString());
        }

        @Test
        @DisplayName("relative baseURL")
        @Disabled("Use `URI.create(url)`")
        void relativeBaseURL() {
            final PocketBase client = new PocketBase("/api");

            assertEquals("/api/test", client.buildURL("test").toString());
            assertEquals("/api/test", client.buildURL("/test").toString());
        }

        @Test
        @DisplayName("with query parameters")
        void withQueryParameters() {
            final PocketBase client = new PocketBase("https://example.com/");

            Map<String, Object> query = new HashMap<>();
            query.put("a", null);
            query.put("b", 123);
            query.put("c", "123");
            query.put("d", new Object[]{"1", 2, null});
            query.put("@encodeA", "@encodeB");

            HttpUrl url = client.buildURL("/test", query);

            assertEquals(
                    "https://example.com/test?b=123&c=123&d=1&d=2&%40encodeA=%40encodeB",
                    url.toString()
            );
        }
    }

    @Nested
    @DisplayName("PocketBase.send()")
    class SendTests {
        @Test
        @DisplayName("check request data (json)")
        void checkRequestDataJson() {
            Interceptor interceptor = new MockClient(
                    (request) -> {
                        // Assert method
                        assertEquals("POST", request.method());

                        // Assert URL
                        assertEquals("https://example.com/base/test?a=1&a=2&c=3", request.url().toString());

                        // Assert headers
                        assertEquals("test_lang", request.header("Accept-Language"));
                        assertEquals("application/json", request.header("Content-Type"));
                        assertEquals("123", request.header("test"));

                        // Assert body
                        RequestBody body = request.body();
                        if (body != null) {
                            Buffer buffer = new Buffer();
                            try {
                                request.body().writeTo(buffer);
                                assertEquals(
                                        PocketBase.jsonEncode(Map.of("test", 123)),
                                        buffer.readUtf8()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "test_lang", interceptor);

            try {
                Map<String, Object> query = new HashMap<>();
                query.put("a", new Object[]{"1", 2, null});
                query.put("b", null);
                query.put("c", 3);

                client.send(
                        "/test",
                        "POST",
                        Map.of("test", "123"),
                        query,
                        Map.of("test", 123),
                        null
                );
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("check request data (multipart/form-data)")
        void checkRequestDataMultipartFormData() {
            Interceptor interceptor = new MockClient(
                    (request) -> {
                        // Assert method
                        assertEquals("POST", request.method());

                        // Assert URL
                        assertEquals("https://example.com/base/test?a=1&a=2&c=3", request.url().toString());

                        // Assert headers
                        assertEquals("test_lang", request.header("Accept-Language"));
                        assertEquals("multipart/form-data", request.header("Content-Type"));
                        assertEquals("123", request.header("test_header"));

                        // Assert body
                        RequestBody body = request.body();
                        if (body != null) {
                            Buffer buffer = new Buffer();
                            try {
                                request.body().writeTo(buffer);
                                String contents = buffer.readUtf8();

                                assertTrue(contents.contains(
                                        "Content-Disposition: form-data; name=\"@jsonPayload\"\r\n"
                                ));
                                assertTrue(contents.contains(
                                        "{\"a\":123,\"b1\":[\"1\",\"2\"],\"b2\":[],\"c1\":[1,2],\"c2\":[],\"d\":null,\"e\":{\"test\":123}}\r\n"
                                ));
                                assertTrue(contents.contains(
                                        "Content-Disposition: form-data; name=\"test_file\""
                                ));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "test_lang", interceptor);

            try {
                Map<String, Object> query = new HashMap<>();
                query.put("a", new Object[]{"1", 2, null});
                query.put("b", null);
                query.put("c", 3);

                Map<String, Object> body = new TreeMap<>();
                body.put("a", 123);
                body.put("b1", new String[]{"1", "2"});
                body.put("b2", new String[]{});
                body.put("c1", new int[]{1, 2});
                body.put("c2", new Object[]{});
                body.put("d", null);
                body.put("e", Map.of("test", 123));

                client.send(
                        "/test",
                        "POST",
                        Map.of("test_header", "123"),
                        query,
                        body,
                        List.of(new MultipartFile(
                                "test_file",
                                new File("./assets/mock.txt")
                        ))
                );
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("response with status code > 400")
        void responseWithStatusCodeAbove400() {
            Interceptor interceptor = new MockClient(
                    400,
                    (request) -> {
                        // Assert method
                        assertEquals("GET", request.method());

                        // Assert URL
                        assertEquals("https://example.com/base/", request.url().toString());

                        // Assert headers
                        assertEquals(
                                Headers.of(
                                        "Content-Type", "application/json",
                                        "Accept-Language", "en-US"),
                                request.headers()
                        );

                        // Assert body
                        assertNull(request.body());
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            assertThrows(ClientException.class, () -> client.send("", null, null, null, null, null));
        }

        @Test
        @DisplayName("empty body response")
        void emptyBodyResponse() {
            Interceptor interceptor = new MockClient(
                    204,
                    (request) -> {
                        // Assert method
                        assertEquals("GET", request.method());

                        // Assert URL
                        assertEquals("https://example.com/base/test", request.url().toString());

                        // Assert headers
                        assertEquals(
                                Headers.of(
                                        "Content-Type", "application/json",
                                        "Accept-Language", "en-US"),
                                request.headers()
                        );

                        // Assert body
                        assertNull(request.body());
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            try {
                assertNull(client.send("/test", null, null, null, null, null));
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("json response")
        void jsonResponse() {
            Interceptor interceptor = new MockClient(
                    (request) -> new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    PocketBase.jsonEncode(Map.of("test", 123)),
                                    MediaType.parse("application/json")))
                            .build(),
                    (request) -> {
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            try {
                assertEquals(
                        Map.of("test", 123).toString(),
                        client.send("/test", null, null, null, null, null).toString()
                );
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("non-json response")
        @Disabled("HTML response not supported. Send accepts only JSON")
        void nonJsonResponse() {
            Interceptor interceptor = new MockClient(
                    (request) -> new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "text/html")
                            .body(ResponseBody.create(
                                    "test123",
                                    MediaType.parse("text/html")))
                            .build(),
                    (request) -> {
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            try {
                assertEquals(
                        "test123",
                        client.send("/test", null, null, null, null, null).toString()
                );
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("with valid record authStore model")
        void withValidRecordAuthStoreModel() {
            Interceptor interceptor = new MockClient(
                    (request) -> {
                        assertTrue(
                                request.header("Authorization").contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.")
                        );
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            client.getAuthStore().save(
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4OTM0NTI0NjF9.yVr-4JxMz6qUf1MIlGx8iW2ktUrQaFecjY_TMm7Bo4o",
                    new RecordModel()
            );

            try {
                client.send("", null, null, null, null, null);
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("with invalid record authStore")
        void withInvalidRecordAuthStore() {
            Interceptor interceptor = new MockClient(
                    (request) -> {
                        assertNull(request.header("Authorization"));
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            client.getAuthStore().save(
                    // expired
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NDA5OTE2NjF9.TxZjXz_Ks665Hju0FkZSGqHFCYBbgBmMGOLnIzkg9Dg",
                    new RecordModel()
            );

            try {
                client.send("", null, null, null, null, null);
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("with custom Authorization header")
        void withCustomAuthorizationHeader() {
            Interceptor interceptor = new MockClient(
                    (request) -> {
                        assertEquals(
                                "test_custom",
                                request.header("Authorization")
                        );
                    }
            );

            final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

            client.getAuthStore().save(
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4OTM0NTI0NjF9.yVr-4JxMz6qUf1MIlGx8iW2ktUrQaFecjY_TMm7Bo4o",
                    new RecordModel()
            );

            try {
                client.send("", null, Map.of("Authorization", "test_custom"), null, null, null);
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }
    }
}