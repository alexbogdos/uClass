package the.fellowship.pocketbase.services;

import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.ClientException;
import the.fellowship.pocketbase.MockClient;
import the.fellowship.pocketbase.PocketBase;
import the.fellowship.pocketbase.dtos.ResultList;

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

    }

    @Test
    @DisplayName("getFullList() with last items.length = perPage")
    void getFullListWithLastItemsLengthEqualPerPage() {

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
                            .body(ResponseBody.create(
                                    PocketBase.jsonEncode(Map.of(
                                            "page", 2,
                                            "perPage", 15,
                                            "totalItems", 17,
                                            "totalPages", 2,
                                            "items", new Map[]{
                                                    Map.of("id", "1"),
                                                    Map.of("id", "2"),
                                            }
                                    )),
                                    MediaType.parse("text/plain")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "?filter=f123&a=1&a=2&b=%40demo&expand=rel&perPage=15&skipTotal=false&page=2&sort=s456&fields=a",
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
                                    PocketBase.jsonEncode(Map.of("id", "@id123")),
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
                                    PocketBase.jsonEncode(Map.of(
                                            "page", 1,
                                            "perPage", 1,
                                            "totalItems", 1,
                                            "totalPages", 1,
                                            "items", new Map[]{
                                                    Map.of("id", "1"),
                                                    Map.of("id", "2"),
                                            }
                                    )),
                                    MediaType.parse("application/json")
                            ))
                            .build();
                },
                (request) -> {
                    assertEquals("GET", request.method());
                    assertEquals(
                            "https://example.com/base/api/" + expectedPath + "?filter=test%3D123&a=1&a=2&b=%40demo&expand=rel&perPage=1&skipTotal=true&page=1&fields=a",
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

    }

    @Test
    @DisplayName("update()")
    void update() {

    }

    @Test
    @DisplayName("delete()")
    void delete() {

    }
}