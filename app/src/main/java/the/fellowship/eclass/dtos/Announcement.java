package the.fellowship.eclass.dtos;

import androidx.annotation.NonNull;

public class Announcement {
    private final String title;
    private final String course;
    private final String courseId;
    private final String date;
    private final String url;
    private final String body;

    public Announcement(String title, String course, String courseId, String date, String url, String body) {
        this.title = title;
        this.course = course;
        this.courseId = courseId;
        this.date = date;
        this.url = url;
        this.body = body;
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

    @NonNull
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", courseId, course, title);
    }
}
