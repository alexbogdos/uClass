package the.fellowship.pocketbase.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

public class Json {
    public static String encode(Map<String, ?> body) {
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        final Type typeObject = new TypeToken<Map<String, ?>>() {
        }.getType();
        return gson.toJson(new TreeMap<>(body), typeObject);
    }

    public static Map<String, ?> decode(String data) {
        final Map<String, ?> decoded = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create()
                .fromJson(data, new TypeToken<Map<String, ?>>() {
                }.getType());
        return decoded;
    }
}
