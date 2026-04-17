package the.fellowship.pocketbase.sse;

import the.fellowship.pocketbase.tools.Json;

import java.util.Map;
import java.util.TreeMap;

public class SseMessage {
    private final Map<String, Object> json;
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

    public SseMessage() {
        this("", "message", "", 0);
    }

    public SseMessage(String data) {
        this("", "message", data, 0);
    }

    public SseMessage(String id, String event, String data, int retry) {
        this(Map.of(
                "id", id,
                "event", event,
                "data", data,
                "retry", retry
        ));
    }

    public SseMessage(Map<String, ?> json) {
        this.id = json.get("id") != null ? (String) json.get("id") : "";
        this.event = json.get("event") != null ? (String) json.get("event") : "message";
        this.data = json.get("data") != null ? (String) json.get("data") : "";
        this.retry = json.get("retry") != null ? (int) json.get("retry") : 0;
        this.json = new TreeMap<>(json);
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

    /**
     * Decodes the event message data as json map.
     */
    public Map<String, ?> getJsonData() {
        if (!data.isEmpty()) {
            try {
                return Json.decode(data);
            } catch (Exception ignored) {
                return Map.of("data", data);
            }
        }

        return new TreeMap<>();
    }

    /**
     * @return JSON as Map<String, ?>
     */
    public Map<String, ?> getJson() {
        return json;
    }

    /**
     * @return JSON encoded to String
     */
    public String toJson() {
        return Json.encode(getJson());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
