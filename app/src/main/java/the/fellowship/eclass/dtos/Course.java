package the.fellowship.eclass.dtos;

import androidx.annotation.NonNull;

public class Course {
    private final String courseId;
    private final String name;
    private final String lecturer;
    private final String url;

    public Course(String courseId, String name, String lecturer, String url) {
        this.courseId = courseId;
        this.name = name;
        this.lecturer = lecturer;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getLecturer() {
        return lecturer;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", courseId, name, lecturer);
    }
}
