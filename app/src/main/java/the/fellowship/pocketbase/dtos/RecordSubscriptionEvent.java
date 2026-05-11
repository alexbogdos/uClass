package the.fellowship.pocketbase.dtos;

import java.util.Map;

/// Response DTO of a single realtime subscription event.
public class RecordSubscriptionEvent {
    private final String action;
    private final RecordModel record;

    public RecordSubscriptionEvent(Map<String, ?> json) {
        this.action = json.get("record") == null ? "" : (String) json.get("action");
        this.record = json.get("record") == null ? null : new RecordModel((Map<String, ?>) json.get("record"));
    }

    public String getAction() {
        return action;
    }

    public RecordModel getRecord() {
        return record;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", action, record);
    }
}
