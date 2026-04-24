package the.fellowship;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.Type;
import java.util.Map;

public class Json {
    private static final ToNumberPolicy numberPolicy = ToNumberPolicy.LONG_OR_DOUBLE;

    public static <T> String encode(T data) {
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        return gson.toJson(data, data.getClass());
    }

    public static Map<String, ?> decode(String data) {
        return decode(data, new TypeToken<Map<String, ?>>() {}.getType());
    }

    public static <T> T decode(String data, Type typeOfT) {
        return new GsonBuilder()
                .setObjectToNumberStrategy(numberPolicy)
                .create()
                .fromJson(data, typeOfT);
    }

    public static <T> T decode(String data, Class<T> classOfT) {
        return new GsonBuilder()
                .setObjectToNumberStrategy(numberPolicy)
                .create()
                .fromJson(data, classOfT);
    }
}
