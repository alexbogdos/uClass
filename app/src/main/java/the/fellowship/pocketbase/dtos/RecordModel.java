package the.fellowship.pocketbase.dtos;

import the.fellowship.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Response DTO of a single record model.
 */
public class RecordModel {
    private final Map<String, Object> data;

    private final Map<String, List<RecordModel>> expand = new TreeMap<>();

    private final List<String> singleExpandKeys = new ArrayList<>();
    private final List<String> multiExpandKeys = new ArrayList<>();

    public RecordModel() {
        this(null);
    }

    public RecordModel(Map<String, ?> data) {
        this.data = data != null ? new TreeMap<>(data) : new TreeMap<>();

        if (this.data.get("expand") == null) {
            return;
        }

        for (Entry<String, ?> entry : ((Map<String, ?>) this.data.get("expand")).entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            final List<RecordModel> result = new ArrayList<>();

            if (value instanceof Iterable<?>) {
                multiExpandKeys.add(key);
                for (final Object item : (Iterable<?>) value) {
                    result.add(new RecordModel(item != null ? (Map<String, ?>) item : new TreeMap<>()));
                }
            } else if (value != null && value.getClass().isArray()) {
                multiExpandKeys.add(key);
                for (final Object item : (Object[]) value) {
                    result.add(new RecordModel(item != null ? (Map<String, ?>) item : new TreeMap<>()));
                }
            }

            if (value instanceof Map) {
                singleExpandKeys.add(key);
                result.add(new RecordModel((Map<String, ?>) value));
            }

            expand.put(key, result);
        }
    }

    public String getId() {
        return getValue("id");
    }

    public void setId(String id) {
        setValue("id", id);
    }

    public String getCollectionId() {
        return getValue("collectionId");
    }

    public String getCollectionName() {
        return getValue("collectionName");
    }

    public String getCreated() {
        return getValue("created");
    }

    public String getUpdated() {
        return getValue("updated");
    }

    public List<RecordModel> getExpand(String key) {
        return expand.get(key);
    }

    public <T> T getValue(String fieldName) {
        return getValue(fieldName, null);
    }

    public <T> T getValue(String fieldName, T defaultValue) {
        if (fieldName.contains(".")) {
            final String[] keys = fieldName.split("\\.");
            final Map<String, ?> internal = getValue(keys[0]);
            return internal.get(keys[1]) != null ? (T) internal.get(keys[1]) : defaultValue;
        }

        if (data.containsKey(fieldName)) {
            return (T) data.get(fieldName);
        }
        return defaultValue;
    }

    public <T> void setValue(String fieldName, T value) {
        data.put(fieldName, value);
    }

    /**
     * @return JSON as Map<String, ?>
     */
    public Map<String, ?> getJson() {
        return data;
    }

    /**
     * @return JSON encoded to String
     */
    public String toJson() {
        return Json.encode(getJson());
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
