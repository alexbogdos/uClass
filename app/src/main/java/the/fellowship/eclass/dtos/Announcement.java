package the.fellowship.eclass.dtos;

import androidx.annotation.NonNull;

public class Announcement {
    private final String title;
    private final String course;
    private final String date;
    private final String url;
    private final String body;

    public Announcement(String title, String course, String date, String url, String body) {
        this.title = title;
        this.course = course;
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

    public String getDate() {
        return date;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s: %s", course, title);
    }
}
