package the.fellowship.pocketbase;

import org.jetbrains.annotations.NotNull;
import the.fellowship.pocketbase.dtos.RecordModel;

/**
 * Event object that holds an AuthStore state.
 */
public record AuthStoreEvent(String token, RecordModel record) {
    @NotNull
    @Override
    public String toString() {
        return String.format("Token: %s, Record: %s", token, record);
    }
}
