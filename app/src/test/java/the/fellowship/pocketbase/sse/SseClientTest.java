package the.fellowship.pocketbase.sse;

import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.MockClient;

import java.util.Map;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SseClient")
class SseClientTest {
    @Test
    @DisplayName("initialize and stream SseMessage objects")
    void initializeAndStreamSseMessageObjects() {
        Interceptor interceptor = new MockClient(
                (request) -> {
                    return new Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .header("Content-Type", "text/event-stream")
                            .body(ResponseBody.create(
                                    "id:test_id1\nevent:test_event1\ndata:{\"a\":123}\nrandom line that should be ignored\nretry:100\n\nid:test_id2\nevent:test_event2\ndata:none_object\nanother random line that should be ignored\n\n",
                                    MediaType.parse("text/event-stream")
                            ))
                            .build();
                },
                (request) -> {}
        );

        final SseClient client = new SseClient("https://example.com/base", interceptor);

        client.setOnMessage(new Flow.Subscriber<SseMessage>() {
            private Flow.Subscription subscription;
            private int count = 0;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(SseMessage message) {
                subscription.request(1);
                count++;

                if (count == 1) {
                    assertEquals("test_id1", message.getId());
                    assertEquals("test_event1", message.getEvent());
                    assertEquals("{\"a\":123}", message.getData());
                    assertEquals(100, message.getRetry());
                    assertEquals(Map.of("a", 123).toString(), message.getJsonData().toString());
                } else if (count == 2) {
                    assertEquals("test_id2", message.getId());
                    assertEquals("test_event2", message.getEvent());
                    assertEquals("none_object", message.getData());
                    assertEquals(0, message.getRetry());
                    assertEquals(Map.of("data", "none_object"), message.getJsonData());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {

        }

        client.close();
        assertTrue(client.isClosed());
    }
}