package the.fellowship.uclass;

import the.fellowship.Environment;

import java.util.List;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Map<String, String> environment = Environment.load(".env");

        Client client = new Client("https://eclass.aueb.gr");
        client.login(environment.get("username"), environment.get("password"));

        List<Map<String, String>> courses = client.courses();
        if (courses != null) {
            courses.forEach(course -> System.out.printf("%s  [%s]\n", course.get("title"), course.get("url")));
        }
    }
}
