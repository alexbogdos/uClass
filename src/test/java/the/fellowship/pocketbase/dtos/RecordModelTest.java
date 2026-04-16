package the.fellowship.pocketbase.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.PocketBase;

import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecordModel")
class RecordModelTest {
    @Test
    @DisplayName("fromJson() and toJson()")
    void fromJsonAndToJson() {
        final Map<String, Object> json = Map.of(
                "id", "test_id",
                "created", "test_created",
                "updated", "test_updated",
                "collectionId", "test_collectionId",
                "collectionName", "test_collectionName",
                "expand", Map.of(
                        "one", new RecordModel(Map.of("id", "1")).getJson(),
                        "many", new Object[]{
                                new RecordModel(Map.of("id", "2")).getJson(),
                                new RecordModel(Map.of(
                                        "id", "3",
                                        "expand", Map.of(
                                                "recursive", new RecordModel(Map.of("id", "4")).getJson()
                                        )
                                )).getJson()
                        }
                ),
                "a", 1,
                "b", "test",
                "c", true
        );

        final RecordModel model = new RecordModel(json);

        assertEquals(json, model.getJson());
        assertEquals("test_id", model.getId());
        assertEquals("test_created", model.getCreated());
        assertEquals("test_updated", model.getUpdated());
        assertEquals("test_collectionId", model.getCollectionId());
        assertEquals("test_collectionName", model.getCollectionName());
        assertEquals(1, model.getExpand("one").size());
        assertEquals("1", model.getExpand("one").get(0).getId());
        assertEquals(2, model.getExpand("many").size());
        assertEquals("2", model.getExpand("many").get(0).getId());
        assertEquals("3", model.getExpand("many").get(1).getId());
        assertEquals(1, model.getExpand("many").get(1).getExpand("recursive").size());
        assertEquals("4", model.getExpand("many").get(1).getExpand("recursive").get(0).getId());

        // to json
        assertEquals(model.getJson(), json);
        assertEquals(PocketBase.jsonEncode(json), model.toJson());
    }

    @Test
    @DisplayName("setValue()")
    void setValue() {
        final RecordModel model = new RecordModel(Map.of("a", 123));

        model.setValue("a", 456);
        model.setValue("b", 789);

        assertEquals(456, model.<Integer>getValue("a"));
        assertEquals(789, model.<Integer>getValue("b"));
    }

    @Test
    @DisplayName("getValue()")
    void getValue() {
        Map<String, Object> json = new TreeMap<>();
        json.put("a", null);
        json.put("b", 1.5);
        json.put("c", "test");
        json.put("d", false);
        json.put("e", List.of("1", "2", "3"));
        json.put("f", Map.of("test", 123));
        json.put("g", List.of(
                Map.of("test", 123)
        ));
        final RecordModel model = new RecordModel(json);

        assertEquals(null, model.<Object>getValue("unknown"));
        assertEquals("missing!", model.getValue("unknown", "missing!"));
        assertEquals(1.5, model.<Number>getValue("b"));
        assertEquals(1.5, model.<Double>getValue("b"));
        //assertEquals(1, model.<Integer>getValue("b"));  // Double to Integer not permitted
        assertEquals("test", model.<String>getValue("c"));
        assertEquals(List.of("1", "2", "3"), model.<List<String>>getValue("e"));
        assertEquals(Map.of("test", 123), model.<Map<String, ?>>getValue("f"));
        assertEquals(Map.of("test", 123), model.<List<Map<String, ?>>>getValue("g").getFirst());
        assertEquals(123, model.<Integer>getValue("f.test"));
        assertEquals(-1, model.<Integer>getValue("f.test_2", -1));
        //assertEquals(Map.of("test", 123), model.<RecordModel>getValue("f").getJson());

        // lists
        assertArrayEquals(new RecordModel[]{}, model.<RecordModel[]>getValue("missing", new RecordModel[]{}));
        //assertArrayEquals(new Integer[]{1}, model.<Integer[]>getValue("b"));  // Double to Integer not permitted
        //assertArrayEquals(new Integer[]{0}, model.<Integer[]>getValue("d"));  // Boolean to Integer not permitted
        assertArrayEquals(new Integer[]{}, model.<Integer[]>getValue("missing", new Integer[]{}));
        //assertArrayEquals(new Integer[]{1, 2, 3}, model.<Integer[]>getValue("e"));  // String[] to Integer[] not permitted

        assertEquals(
                Map.of("test", 123),
                model.<List<Map<String, ?>>>getValue("g").stream().map((r) -> new RecordModel(r).getJson()).toList().getFirst()
        );

        // existing field as nullable type
        assertEquals(1.5, model.<Number>getValue("b"));
        assertEquals("test", model.<String>getValue("c"));
        assertEquals(false, model.<Boolean>getValue("d"));
        assertEquals(List.of("1", "2", "3"), model.<List<String>>getValue("e"));
        assertEquals(Map.of("test", 123), model.<Map<String, ?>>getValue("f"));
        assertEquals(123, model.<Integer>getValue("f.test"));
        assertEquals(Map.of("test", 123), new RecordModel(model.getValue("f")).getJson());

        // non-existing field as nullable type
        assertNull(model.<Number>getValue("b_2"));
        assertNull(model.<String>getValue("c_2"));
        assertNull(model.<Boolean>getValue("d_2"));
        assertNull(model.<List<String>>getValue("e_2"));
        assertNull(model.<Map<String, ?>>getValue("f_2"));
        assertNull(model.<Integer>getValue("f.test_2"));
        assertNull(model.<RecordModel>getValue("f_2"));
    }
}