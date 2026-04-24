package the.fellowship.pocketbase.services;

import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.MockClient;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.ResultList;
import the.fellowship.Json;
import the.fellowship.pocketbase.tools.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BaseCrudService")
abstract class CrudServiceTest<T> {
    final Function<PocketBase, BaseCrudService<T>> serviceFactory;
    final Function<T, String> identifier;
    final String expectedPath;

    CrudServiceTest(Function<PocketBase, BaseCrudService<T>> serviceFactory, Function<T, String> identifier, String expectedPath) {
        this.serviceFactory = serviceFactory;
        this.identifier = identifier;
        this.expectedPath = expectedPath;
    }

    @Test
    @DisplayName("getFullList() with last items.length < perPage")
    void getFullListWithLastItemsLengthLessPerPage() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    "1".equals(request.url().queryParameter("page"))
                                            // page1
                                            ? Json.encode(Map.of(
                                            "page", 1,
                                            "perPage", 2,
                                            "totalItems", -1,
                                            "totalPages", -1,
                                            "items", new Map[]{
                                                    Map.of("id", "1"),
                                                    Map.of("id", "2")
                                            }))
                                            // page2
                                            : Json.encode(Map.of(
                                            "page", 2,
                                            "perPage", 2,
                                            "totalItems", -1,
                                            "totalPages", -1,
                                            "items", new Map[]{
                                                    Map.of("id", "3")
                                            })),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals("789", request.header("test"));

                    // page1
                    if ("1".equals(request.url().queryParameter("page"))) {
                        assertEquals(
                                "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=f%3D123&page=1&perPage=2&skipTotal=true&sort=s%3D456",
                                request.url().toString()
                        );
                    }
                    // page2
                    else {
                        assertEquals(
                                "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=f%3D123&page=2&perPage=2&skipTotal=true&sort=s%3D456",
                                request.url().toString()
                        );
                    }
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final List<T> result = serviceFactory.apply(client).getFullList(
                    2,
                    "rel",
                    "f=123",
                    "s=456",
                    "a",
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    )
            );

            assertNotNull(result);
            assertEquals(3, result.size());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getFullList() with last items.length = perPage")
    void getFullListWithLastItemsLengthEqualPerPage() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    "1".equals(request.url().queryParameter("page"))
                                            // page1
                                            ? Json.encode(Map.of(
                                            "page", 1,
                                            "perPage", 2,
                                            "totalItems", -1,
                                            "totalPages", -1,
                                            "items", new Map[]{
                                                    Map.of("id", "1"),
                                                    Map.of("id", "2")
                                            }))
                                            // page2
                                            : "2".equals(request.url().queryParameter("page"))
                                              ? Json.encode(Map.of(
                                            "page", 2,
                                            "perPage", 2,
                                            "totalItems", -1,
                                            "totalPages", -1,
                                            "items", new Map[]{
                                                    Map.of("id", "3"),
                                                    Map.of("id", "2")
                                            }))
                                              // page3
                                              : Json.encode(Map.of(
                                            "page", 3,
                                            "perPage", 2,
                                            "totalItems", -1,
                                            "totalPages", -1)),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals("789", request.header("test"));

                    // page1
                    if ("1".equals(request.url().queryParameter("page"))) {
                        assertEquals(
                                "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=f%3D123&page=1&perPage=2&skipTotal=true&sort=s%3D456",
                                request.url().toString()
                        );
                    }
                    // page2
                    else if ("2".equals(request.url().queryParameter("page"))) {
                        assertEquals(
                                "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=f%3D123&page=2&perPage=2&skipTotal=true&sort=s%3D456",
                                request.url().toString()
                        );
                    }
                    // page3
                    else {
                        assertEquals(
                                "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=f%3D123&page=3&perPage=2&skipTotal=true&sort=s%3D456",
                                request.url().toString()
                        );
                    }
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final List<T> result = serviceFactory.apply(client).getFullList(
                    2,
                    "rel",
                    "f=123",
                    "s=456",
                    "a",
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    )
            );

            assertNotNull(result);
            assertEquals(4, result.size());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getList()")
    void getList() {
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
                                            "page", 2,
                                            "perPage", 15,
                                            "totalItems", 17,
                                            "totalPages", 2,
                                            "items", new Map[]{
                                                    Map.of("id", "1"),
                                                    Map.of("id", "2"),
                                            })),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=f123&page=2&perPage=15&skipTotal=false&sort=s456",
                            request.url().toString()
                    );
                    assertEquals("789", request.header("test"));
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final ResultList<T> result = serviceFactory.apply(client).getList(
                    2,
                    15,
                    false,
                    "rel",
                    "f123",
                    "s456",
                    "a",
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    )
            );

            assertEquals(2, result.getPage());
            assertEquals(15, result.getPerPage());
            assertEquals(17, result.getTotalItems());
            assertEquals(2, result.getTotalPages());
            assertTrue(result.getItems() instanceof List<T>);
            assertEquals(2, result.getItems().size());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getOne()")
    void getOne() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    Json.encode(Map.of("id", "@id123")),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "/%40id123?a=1&a=2&b=%40demo&expand=rel",
                            request.url().toString()
                    );
                    assertEquals("789", request.header("test"));
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final T result = serviceFactory.apply(client).getOne(
                    "@id123",
                    "rel",
                    null,
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    )
            );

            assertTrue(result instanceof T);
            assertEquals("@id123", identifier.apply(result));
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getOne() with empty id")
    void getOneWithEmptyId() {
        final PocketBase client = new PocketBase("https://example.com/base");

        assertThrows(
                ClientException.class,
                () -> serviceFactory.apply(client).getOne("", null, null, null, null)
        );
    }

    @Test
    @DisplayName("getFirstListItem()")
    void getFirstListItem() {
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
                                            "page", 1,
                                            "perPage", 1,
                                            "totalItems", 1,
                                            "totalPages", 1,
                                            "items", new Map[]{
                                                    Map.of("id", "1"),
                                                    Map.of("id", "2"),
                                            })),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo&expand=rel&fields=a&filter=test%3D123&page=1&perPage=1&skipTotal=true",
                            request.url().toString()
                    );
                    assertEquals("789", request.header("test"));
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final T result = serviceFactory.apply(client).getFirstListItem(
                    "test=123",
                    "rel",
                    "a",
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    )
            );

            assertTrue(result instanceof T);
            assertEquals("1", identifier.apply(result));
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("create()")
    void create() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    Json.encode(Map.of("id", "@id123")),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("POST", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "?a=1&a=2&b=%40demo",
                            request.url().toString()
                    );
                    assertEquals("789", request.header("test"));

                    // Assert body
                    RequestBody body = request.body();
                    if (body != null) {
                        Buffer buffer = new Buffer();
                        try {
                            request.body().writeTo(buffer);
                            String contents = buffer.readUtf8();

                            assertTrue(contents.contains("Content-Disposition: form-data; name=\"@jsonPayload\"\r\n"));
                            assertTrue(contents.contains("{\"test_body\":123}\r\n"));
                            assertTrue(contents.contains("Content-Disposition: form-data; name=\"test_file\""));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final T result = serviceFactory.apply(client).create(
                    null,
                    null,
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    ),
                    Map.of("test_body", 123),
                    List.of(new MultipartFile(
                            "test_file",
                            "456"
                    ))
            );

            assertTrue(result instanceof T);
            assertEquals("@id123", identifier.apply(result));
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("update()")
    void update() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "application/json")
                            .body(ResponseBody.create(
                                    Json.encode(Map.of("id", "@id123")),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("PATCH", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "/%40id123?a=1&a=2&b=%40demo",
                            request.url().toString()
                    );
                    assertEquals("789", request.header("test"));

                    // Assert body
                    RequestBody body = request.body();
                    if (body != null) {
                        Buffer buffer = new Buffer();
                        try {
                            request.body().writeTo(buffer);
                            String contents = buffer.readUtf8();

                            assertTrue(contents.contains("Content-Disposition: form-data; name=\"@jsonPayload\"\r\n"));
                            assertTrue(contents.contains("{\"test_body\":123}\r\n"));
                            assertTrue(contents.contains("Content-Disposition: form-data; name=\"test_file\""));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            final T result = serviceFactory.apply(client).update(
                    "@id123",
                    null,
                    null,
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    ),
                    Map.of("test_body", 123),
                    List.of(new MultipartFile(
                            "test_file",
                            "456"
                    ))
            );

            assertTrue(result instanceof T);
            assertEquals("@id123", identifier.apply(result));
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("delete()")
    void delete() {
        Interceptor interceptor = new MockClient(
                204,
                (request) -> {
                    assertEquals("DELETE", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "/%40id123?a=1&a=2&b=%40demo",
                            request.url().toString()
                    );
                    assertEquals("application/json", request.header("Content-Type"));
                    assertEquals("789", request.header("test"));

                    // Assert body
                    RequestBody body = request.body();
                    if (body != null) {
                        Buffer buffer = new Buffer();
                        try {
                            request.body().writeTo(buffer);
                            assertEquals("{\"test_body\":123}", buffer.readUtf8());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

        final PocketBase client = new PocketBase("https://example.com/base", "en-US", interceptor);

        try {
            serviceFactory.apply(client).delete(
                    "@id123",
                    Map.of("test", "789"),
                    Map.of(
                            "a", new Object[]{"1", null, 2},
                            "b", "@demo"
                    ),
                    Map.of("test_body", 123)
            );
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }
}