package the.fellowship.pocketbase.sse;

import java.util.HashMap;
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

    private final Map<String, Object> json;

    public SseMessage() {
        this.json = new HashMap<>();
    }

    public SseMessage(Map<String, ?> json) {
        this.id = (String) json.get("id");
        this.event = (String) json.get("event");
        this.data = (String) json.get("data");
        this.retry = (int) json.get("retry");
        this.json = new HashMap<>(json);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.json.put("id", id);
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
        this.json.put("event", event);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        this.json.put("data", data);
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
        this.json.put("retry", retry);
    }


    public Map<String, ?> getJson() {
        return json;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", event, data);
    }
}
