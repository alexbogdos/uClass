package the.fellowship.pocketbase;

import the.fellowship.Environment;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> env = Environment.load(".env");

        final PocketBase pb = new PocketBase("http://trantor.lan:8090");

        pb.collection("users").authWithPassword(env.get("username"), env.get("password"));

//        // after the above you can also access the auth data from the authStore
//        console.log(pb.authStore.isValid);
//        console.log(pb.authStore.token);
//        console.log(pb.authStore.record.id);
//
//        // "logout"
//        pb.authStore.clear();
    }
}
