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

    private final Map<String, ?> data;

    public RecordModel(Map<String, ?> record) {
        this.id = (String) record.get("id");
        this.collectionId = (String) record.get("collectionId");
        this.collectionName = (String) record.get("collectionName");
        this.created = (String) record.get("created");
        this.updated = (String) record.get("updated");

        this.data = record;
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
        if (data.containsKey(fieldName) && data.get(fieldName) != null) {
            return data.get(fieldName);
        }
        return defaultValue;
    }

    public Object getValue(String fieldName) {
        return data.get(fieldName);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
