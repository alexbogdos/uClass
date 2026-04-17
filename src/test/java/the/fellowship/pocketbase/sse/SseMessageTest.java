package the.fellowship.pocketbase.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SseMessage")
class SseMessageTest {
    @Test
    @DisplayName("fromJson() and toJson()")
    void fromJsonAndToJson() {
        final Map<String, ?> json = Map.of(
                "id", "test_id",
                "event", "test_event",
                "data", "test_data",
                "retry", 123
        );

        final SseMessage message = new SseMessage(json);

        assertEquals(json, message.getJson());
    }

    @Test
    @DisplayName("jsonData() with serialized data object")
    void jsonDataWithSerializedDataObject() {
        final SseMessage message = new SseMessage("{\"a\": 123}");
        assertEquals(Map.of("a", 123).toString(), message.getJsonData().toString());
    }

    @Test
    @DisplayName("jsonData() with serialized data array")
    void jsonDataWithSerializedDataArray() {
        final SseMessage message = new SseMessage("[1, 2, 3]");
        assertEquals(Map.of("data", List.of(1, 2, 3)).toString(), message.getJsonData().toString());
    }

    @Test
    @DisplayName("jsonData() with non-json data string")
    void jsonDataWithNonJsonDataString() {
        final SseMessage message = new SseMessage("test");
        assertEquals(Map.of("data", "test").toString(), message.getJsonData().toString());
    }

    @Test
    @DisplayName("jsonData() with empty data")
    void jsonDataWithEmptyData() {
        final SseMessage message = new SseMessage();
        assertEquals(Map.of().toString(), message.getJsonData().toString());
    }
}