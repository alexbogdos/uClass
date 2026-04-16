package the.fellowship.pocketbase.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.PocketBase;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RecordAuth")
class RecordAuthTest {
    @Test
    @DisplayName("fromJson() and toJson()")
    void fromJsonAndToJson() {
        final Map<String, Object> json = Map.of(
                "token", "test_token",
                "record", Map.of(
                        "id", "test_id",
                        "created", "test_created",
                        "updated", "test_updated",
                        "collectionId", "test_collectionId",
                        "collectionName", "test_collectionName",
                        "expand", Map.of(
                                "test", new RecordModel(Map.of("id", "123")).getJson()
                        ),
                        "a", 1
                ),
                "meta", Map.of("test", 123)
        );

        final RecordAuth auth = new RecordAuth(json);

        assertEquals(json, auth.getJson());
    }
}