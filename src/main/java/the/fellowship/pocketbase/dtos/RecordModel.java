package the.fellowship.pocketbase.dtos;

import java.util.HashMap;
import java.util.Map;

/**
 * Response DTO of a single record model.
 */
public class RecordModel {
    private final Map<String, Object> data;

    public RecordModel() {
        this(new HashMap<>());
    }

    public RecordModel(Map<String, ?> data) {
        this.data = (Map<String, Object>) data;
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

    public <T> T getValue(String fieldName) {
        return getValue(fieldName, null);
    }

    public <T> T getValue(String fieldName, T defaultValue) {
        if (data.containsKey(fieldName)) {
            return (T) data.get(fieldName);
        }
        return defaultValue;
    }

    public <T> void setValue(String fieldName, T value) {
        data.put(fieldName, value);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
