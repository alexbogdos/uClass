package the.fellowship.pocketbase.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResultList")
class ResultListTest {
    @Test
    @DisplayName("fromJson() and toJson()")
    void fromJsonAndToJson() {
        Map<String, ?> json = Map.of(
                "page", 2,
                "perPage", 20,
                "totalItems", 200,
                "totalPages", 10,
                "items", List.of(
                        Map.of(
                                "id", "test_id",
                                "created", "test_created",
                                "updated", "test_updated",
                                "collectionId", "test_collectionId",
                                "collectionName", "test_collectionName",
                                "expand", Map.of(
                                        "test", new RecordModel(Map.of("id", "1")).getJson()
                                ),
                                "a", 1,
                                "b", "test",
                                "c", true
                        )
                )
        );

        final ResultList<RecordModel> model = new ResultList<>(
                json,
                RecordModel::new,
                RecordModel::getJson
        );

        assertEquals(json, model.getJson());
    }
}