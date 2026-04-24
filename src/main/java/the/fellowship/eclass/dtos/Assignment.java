package the.fellowship.eclass.dtos;

import okhttp3.HttpUrl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Assignment {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final ZoneId zoneId = ZoneId.systemDefault();
    private final String id;
    private final String title;
    private final Instant start;
    private final Instant end;
    private final String courseName;
    private final String courseId;
    private final HttpUrl url;

    public Assignment(Map<String, ?> json) {
        // Split "CourseName: Assignment Title" to ["CourseName", "Assignment Title"]
        final String[] titleParts = ((String) json.get("title")).split(": ");

        this.id = (String) json.get("id");
        this.title = titleParts[1];
        this.start = Instant.ofEpochMilli((long) json.get("start"));
        this.end = Instant.ofEpochMilli((long) json.get("start"));
        this.courseName = titleParts[0];
        this.courseId = (String) json.get("course");
        this.url = HttpUrl.parse((String) json.get("url"));
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", title, dateFormatter.format(end.atZone(zoneId)));
    }
}
