package the.fellowship.pocketbase.dtos;

import java.util.Map;

public class RecordAuth {
    private final String token;
    private final RecordModel record;

    public RecordAuth(Map<String, ?> json) {
        this.token = (String) json.get("token");
        this.record = new RecordModel((Map<String, ?>) json.get("record"));
    }

    public String getToken() {
        return token;
    }

    public RecordModel getRecord() {
        return record;
    }

    public String getIdentifier() {
        if (record.getValue("name") != null) {
            return record.getValue("name");
        }
        return record.getValue("email");
    }

    @Override
    public String toString() {
        return record.toString();
    }
}
