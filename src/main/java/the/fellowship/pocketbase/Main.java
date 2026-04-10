package the.fellowship.pocketbase;

import the.fellowship.Environment;
import the.fellowship.pocketbase.dtos.RecordModel;
import the.fellowship.pocketbase.dtos.ResultList;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> env = Environment.load(".env");

        final PocketBase pb = new PocketBase("http://127.0.0.1:8090");

        Map<String, ?> body;
        Map<String, ?> response;

        //body = Map.of(
        //        "email", env.get("email"),
        //        "emailVisibility", false,
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
            response = pb.collection("users").authWithPassword(env.get("email"), env.get("password"));
            System.out.printf("Welcome, %s\n", ((Map<String, ?>) response.get("record")).containsKey("name") ? ((Map<String, ?>) response.get("record")).get("name") : ((Map<String, ?>) response.get("record")).get("email"));

            // after the above you can also access the auth data from the authStore
            //System.out.println(pb.getAuthStore().isValid());
            //System.out.println(pb.getAuthStore().getToken());
            //System.out.println(pb.getAuthStore().getRecord().getId());

            //body = Map.of(
            //        "title", "Hello, World!",
            //        "active", true,
            //        "author", ((Map<String, ?>) response.get("record")).get("id")
            //);
            //response = pb.collection("posts").create(null, null, body, null);


            ResultList<RecordModel> list = pb.collection("posts").getList(
                    null,
                    null,
                    "-created",
                    null,
                    null,
                    null
            );
            list.getItems().forEach(System.out::println);

            RecordModel item = pb.collection("posts").getOne(
                    "hcll40100ine8pt",
                    null,
                    null,
                    null,
                    null
            );
            System.out.println(item);

        } catch (ClientException e) {
            System.err.println(e);
        }

        // "logout"
        pb.getAuthStore().clear();
    }
}
