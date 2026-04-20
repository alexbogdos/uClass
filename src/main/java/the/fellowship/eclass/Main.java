package the.fellowship.eclass;

import the.fellowship.Environment;

import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Map<String, String> env = Environment.load(".env");

        EClass client = new EClass("https://eclass.aueb.gr");

        client.login(env.get("username"), env.get("password"))
                .thenAccept(result -> {
                    switch (result) {
                        case "FAILURE": {
                            System.out.println("Incorrect username or password\n");
                            break;
                        }
                        case "SUCCESS": {
                            System.out.printf("Welcome, %s!\n\n", env.get("username"));
                            break;
                        }
                        case "RESTORE": {
                            System.out.printf("Welcome back, %s!\n\n", env.get("username"));
                            break;
                        }
                    }
                })
                .exceptionally(err -> {
                    if (err != null) {
                        System.err.printf("Failed to login: \"%s\"", err.getMessage().substring(err.getMessage().indexOf(":") + 2));
                    }
                    return null;
                }).join();


        client.getCourses()
                .thenAccept(list -> {
                    list.forEach(course -> System.out.printf("%s  [%s]\n", course.get("title"), course.get("url")));
                }).join();
    }
}
