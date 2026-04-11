package the.fellowship.pocketbase;

import the.fellowship.Environment;
import the.fellowship.pocketbase.dtos.RecordAuth;
import the.fellowship.pocketbase.dtos.RecordModel;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> env = Environment.load(".env");

        final PocketBase pb = new PocketBase("http://127.0.0.1:8090");

        Map<String, ?> body;
        RecordModel response;

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
            RecordAuth auth = pb.collection("users").authWithPassword(env.get("email"), env.get("password"));
            System.out.printf("Welcome, %s\n", auth.getIdentifier());

            // after the above you can also access the auth data from the authStore
            //System.out.println(pb.getAuthStore().isValid());
            //System.out.println(pb.getAuthStore().getToken());
            //System.out.println(pb.getAuthStore().getRecord().getId());

            body = Map.of(
                    "description", "Περίεργος καθηγητής!",
                    "course", "5b5605872rq51tw"
            );
            response = pb.collection("ratings").update("k0map51cxub748m", null, null, body, null, null, null);
            System.out.println(response);

            //pb.collection("ratings").delete(response.getId(), null, null, null);

            //ResultList<RecordModel> list = pb.collection("ratings").getList(
            //        null,
            //        null,
            //        "-created",
            //        null,
            //        null,
            //        null
            //);
            //list.getItems().forEach(System.out::println);

        } catch (ClientException e) {
            System.err.println(e);
        }

        // "logout"
        pb.getAuthStore().clear();
    }
}
