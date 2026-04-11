package the.fellowship.pocketbase.sse;

import java.util.Map;

public class SseMessage {
    /**
     * String identifier representing the last event ID value.
     */
    private String id;

    /**
     * The name/type of the event message.
     */
    private String event;

    /**
     * The raw data of the event message.
     */
    private String data;

    /**
     * The reconnection time (in milliseconds).
     */
    private int retry;

    private final Map<String, ?> json;

    public SseMessage(Map<String, ?> json) {
        this.id = (String) json.get("id");
        this.event = (String) json.get("event");
        this.data = (String) json.get("data");
        this.retry = (int) json.get("retry");
        this.json = json;
    }

    public String getId() {
        return id;
    }

    public String getEvent() {
        return event;
    }

    public String getData() {
        return data;
    }

    public int getRetry() {
        return retry;
    }

    public Map<String, ?> getJson() {
        return json;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", event, data);
    }
}
