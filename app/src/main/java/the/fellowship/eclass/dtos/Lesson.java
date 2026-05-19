package the.fellowship.eclass.dtos;

import java.util.Arrays;
import java.util.List;

public class Lesson {
    private final String title;
    private final List<Occurrence> occurrences;

    public Lesson(String title, Occurrence... occurrences) {
        this.title = title;
        this.occurrences = Arrays.asList(occurrences);
        this.occurrences.forEach(occurrence -> occurrence.setTitle(title));
    }

    public String getTitle() {
        return title;
    }

    public List<Occurrence> getOccurrences() {
        return occurrences;
    }
}
