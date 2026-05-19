package the.fellowship.eclass.dtos;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Occurrence {
    private String title;
    private final int dayOfWeek;
    private final int start;
    private final int end;
    private final String location;

    public Occurrence(String dayOfWeek, String hours, String location) {
        this.dayOfWeek = parseDayOfWeek(dayOfWeek);
        this.start = Integer.parseInt(hours.split("-")[0].strip());
        this.end = Integer.parseInt(hours.split("-")[1].strip());
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getStart() {
        return start;
    }

    public LocalDateTime getStart(LocalDateTime date) {
        return LocalDateTime.of(date.toLocalDate().plusDays(dayOfWeek - date.getDayOfWeek().getValue()), LocalTime.of(start, 0));
    }

    public int getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    private static int parseDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek) {
            case "ΔΕ":
                return 1;
            case "ΤΡ":
                return 2;
            case "ΤΕ":
                return 3;
            case "ΠΕ":
                return 4;
            case "ΠΑ":
                return 5;
        }
        return 0;
    }
}