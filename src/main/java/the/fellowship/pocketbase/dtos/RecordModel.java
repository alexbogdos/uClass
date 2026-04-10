package the.fellowship.pocketbase.dtos;

import java.util.Map;

/**
 * Response DTO of a single record model.
 */
public class RecordModel {
    private final String id;
    private final String collectionId;
    private final String collectionName;
    private final String created;
    private final String updated;

    private final Map<String, ?> record;

    public RecordModel(Map<String, ?> json) {
        this.id = (String) json.get("id");
        this.collectionId = (String) json.get("collectionId");
        this.collectionName = (String) json.get("collectionName");
        this.created = (String) json.get("created");
        this.updated = (String) json.get("updated");

        this.record = json;
    }

    public String getId() {
        return id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public Object getValueOrDefault(String fieldName, Object defaultValue) {
        if (record.containsKey(fieldName) && record.get(fieldName) != null) {
            return record.get(fieldName);
        }
        return defaultValue;
    }

    public Object getValue(String fieldName) {
        return record.get(fieldName);
    }

    @Override
    public String toString() {
        return record.toString();
    }
}
