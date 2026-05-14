package the.fellowship;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Environment {
    public static Map<String, String> load(String path) {
        Map<String, String> environment = new TreeMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entries = line.split("=");
                environment.put(entries[0], entries[1]);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load account from file");
        }
        return environment;
    }
}
