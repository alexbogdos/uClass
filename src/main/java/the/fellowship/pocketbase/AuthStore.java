package the.fellowship.pocketbase;

import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import the.fellowship.pocketbase.dtos.RecordModel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Base authentication store management service that keep tracks of
 * the authenticated User/Admin model and its token.
 */
public class AuthStore {
    private String token = "";
    private RecordModel record;

    /**
     * Returns the saved auth token (if any).
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the saved auth record (if any).
     */
    public RecordModel getRecord() {
        return record;
    }

    /**
     * Loosely checks if the current AuthStore has valid auth data
     * (eg. whether the token is expired or not).
     */
    public boolean isValid() {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        byte[] tokenPart = Base64.getDecoder().decode(parts[1]);
        String jsonString = new String(tokenPart, StandardCharsets.UTF_8);

        Map<String, ?> data = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create()
                .fromJson(jsonString, new TypeToken<Map<String, ?>>() {}.getType());
        long exp = data.get("exp") != null ? (Long) data.get("exp") : 0;
        return exp > System.currentTimeMillis() / 1000;
    }

    /**
     * Saves the provided [newToken] and [newRecord] auth data into the store.
     */
    public void save(String newToken, RecordModel newRecord) {
        this.token = newToken;
        this.record = newRecord;
    }

    /**
     * Clears the previously stored [token] and [record] auth data.
     */
    public void clear() {
        this.token = "";
        this.record = null;
    }
}
