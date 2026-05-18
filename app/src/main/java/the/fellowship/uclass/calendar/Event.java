package the.fellowship.uclass.calendar;

import java.time.LocalDateTime;

import the.fellowship.eclass.dtos.Assignment;

public class Event {
    private final String title;
    private final LocalDateTime date;
    private final String location;

    public Event(String title, LocalDateTime date) {
        this(title, date, null);
    }

    public Event(Assignment assignment) {
        this(assignment.getTitle(), assignment.getEnd(), null);
    }

    public Event(String title, LocalDateTime date, String location) {
        this.title = title;
        this.date = date;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public boolean hasLocation() {
        return location != null;
    }
}
