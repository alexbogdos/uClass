package the.fellowship.pocketbase.dtos;

import java.util.Map;

/// Response DTO of a single realtime subscription event.
public class RecordSubscriptionEvent {
    private String action;
    private RecordModel record;

    public RecordSubscriptionEvent(Map<String, ?> json) {
        this.action = (String) json.get("action");
        this.record = (RecordModel) json.get("record");
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
