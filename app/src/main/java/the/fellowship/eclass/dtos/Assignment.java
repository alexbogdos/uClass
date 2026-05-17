package the.fellowship.eclass.dtos;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import okhttp3.HttpUrl;

public class Assignment {
    private static int LATEST_ID = 0;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final ZoneId zoneId = ZoneId.systemDefault();
    private final int id;
    private final String title;
    private final Instant start;
    private final Instant end;
    private final String courseName;
    private final String courseId;
    private final HttpUrl url;

    public Assignment(Map<String, ?> json) {
        // Split "CourseName: Assignment Title" to ["CourseName", "Assignment Title"]
        final String[] titleParts = ((String) json.get("title")).split(": ");

        this.id = Integer.parseInt((String) json.get("id"));
        this.title = titleParts[1];
        this.start = Instant.ofEpochMilli((long) json.get("start"));
        this.end = Instant.ofEpochMilli((long) json.get("start"));
        this.courseName = titleParts[0];
        this.courseId = (String) json.get("course");
        this.url = HttpUrl.parse((String) json.get("url"));
    }

    public static int getLatestId() {
        return LATEST_ID;
    }

    public static void setLatestId(int latestId) {
        LATEST_ID = latestId;
    }

    public int getId() {
        return id;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getEnd() {
        return dateFormatter.format(end.atZone(zoneId));
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", title, dateFormatter.format(end.atZone(zoneId)));
    }
}
