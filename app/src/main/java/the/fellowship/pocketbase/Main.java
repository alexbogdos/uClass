package the.fellowship.pocketbase;

import the.fellowship.Environment;
import the.fellowship.pocketbase.dtos.RecordAuth;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> env = Environment.load(".env");

        final PocketBase pb = new PocketBase("http://192.168.1.2:8090");

        try {
            RecordAuth auth = pb.getCollection("users").authWithPassword(env.get("email"), env.get("password"));
            System.out.printf("Welcome, %s\n", auth.getIdentifier());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // "logout"
        pb.getAuthStore().clear();
    }
}
