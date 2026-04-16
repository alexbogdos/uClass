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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BaseCrudService")
abstract class CrudServiceTest<T> {
    final Function<PocketBase, BaseCrudService<T>> serviceFactory;
    final String expectedPath;

    CrudServiceTest(Function<PocketBase, BaseCrudService<T>> serviceFactory, String expectedPath) {
        this.serviceFactory = serviceFactory;
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
                    Map.of(
                            "test", "789"
                    ),
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
                    Map.of(
                            "test", "789"
                    ),
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
    @DisplayName("getOne() with empty id")
    void getOneWithEmptyId() {

    }

    @Test
    @DisplayName("getFirstListItem()")
    void getFirstListItem() {

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