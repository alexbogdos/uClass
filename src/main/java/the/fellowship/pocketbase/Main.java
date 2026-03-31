package the.fellowship.pocketbase;

public class Main {
    public static void main(String[] args) {
        final PocketBase pb = new PocketBase("http://trantor.lan:8090");

        pb.collection("users").authWithPassword("p3220134@aueb.gr", "wayrceal947");

//        // after the above you can also access the auth data from the authStore
//        console.log(pb.authStore.isValid);
//        console.log(pb.authStore.token);
//        console.log(pb.authStore.record.id);
//
//        // "logout"
//        pb.authStore.clear();
    }
}
