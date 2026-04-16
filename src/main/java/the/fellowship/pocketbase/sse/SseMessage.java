package the.fellowship.pocketbase.sse;

import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.util.TreeMap;
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
        this.json = new TreeMap<>();
    }

    public SseMessage(Map<String, ?> json) {
        this.id = (String) json.get("id");
        this.event = (String) json.get("event");
        this.data = (String) json.get("data");
        this.retry = (int) json.get("retry");
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
            Map<String, ?> decoded = new GsonBuilder()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create()
                    .fromJson(data, new TypeToken<Map<String, ?>>() {}.getType());
            return decoded;
        }

        return new TreeMap<>();
    }

    public Map<String, ?> getJson() {
        return json;
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
