package the.fellowship.pocketbase;

import the.fellowship.Environment;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> env = Environment.load(".env");

        final PocketBase pb = new PocketBase("http://127.0.0.1:8090");

        //Map<String, String> body = Map.of(
        //        "email", env.get("email"),
        //        "emailVisibility", "false",
        //        "name", env.get("name"),
        //        "password", env.get("password"),
        //        "passwordConfirm", env.get("password")
        //);
        //try {
        //    pb.collection("users").create(null, null, body, null);
        //} catch (ClientException e) {
        //    System.err.println(e);
        //}

        try {
            Map<String, ?> response = pb.collection("users").authWithPassword(env.get("email"), env.get("password"));
            System.out.printf("Welcome, %s\n", ((Map<String, ?>) response.get("record")).get("name"));
        } catch (ClientException e) {
            System.err.println(e);
        }

        // after the above you can also access the auth data from the authStore
        System.out.println(pb.getAuthStore().isValid());
        System.out.println(pb.getAuthStore().getToken());
        //console.log(pb.authStore.record.id);

        // "logout"
        pb.getAuthStore().clear();
    }
}
