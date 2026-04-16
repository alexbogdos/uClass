package the.fellowship.pocketbase.dtos;

import the.fellowship.pocketbase.PocketBase;

import java.util.HashMap;
import java.util.Map;

public class RecordAuth {
    private final String token;
    private final RecordModel record;
    private final Map<String, ?> meta;

    public RecordAuth(Map<String, ?> json) {
        this.token = json.get("token") != null ? (String) json.get("token") : "";
        this.record = json.get("record") != null ? new RecordModel((Map<String, ?>) json.get("record")) : null;
        this.meta = json.get("meta") != null ? (Map<String, ?>) json.get("meta") : new HashMap<>();
    }

    public String getToken() {
        return token;
    }

    public RecordModel getRecord() {
        return record;
    }

    public Map<String, ?> getMeta() {
        return meta;
    }

    public String getIdentifier() {
        if (record.getValue("name") != null) {
            return record.getValue("name");
        }
        return record.getValue("email");
    }

    /**
     * @return JSON as Map<String, ?>
     */
    public Map<String, ?> getJson() {
        return Map.of(
                "token", token,
                "record", record.getJson(),
                "meta", meta
        );
    }

    /**
     * @return JSON encoded to String
     */
    public String toJson() {
        return PocketBase.jsonEncode(getJson());
    }

    @Override
    public String toString() {
        return record.toString();
    }
}
