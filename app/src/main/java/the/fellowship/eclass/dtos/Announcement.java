package the.fellowship.eclass.dtos;

import androidx.annotation.NonNull;

public class Announcement {
    private static int LATEST_ID = 0;

    private final int id;
    private final String title;
    private final String course;
    private final String courseId;
    private final String date;
    private final String url;
    private final String body;

    public Announcement(int id, String title, String course, String courseId, String date, String url, String body) {
        this.id = id;
        this.title = title;
        this.course = course;
        this.courseId = courseId;
        this.date = date;
        this.url = url;
        this.body = body;
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

    public String getTitle() {
        return title;
    }

    public String getCourse() {
        return course;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[%s/%s] %s: %s", courseId, id, course, title);
    }
}
