package the.fellowship.pocketbase;

import org.jetbrains.annotations.NotNull;
import the.fellowship.pocketbase.dtos.RecordModel;

/**
 * Event object that holds an AuthStore state.
 */
public class AuthStoreEvent {
    private final String token;
    private final RecordModel record;

    public AuthStoreEvent(String token, RecordModel record) {
        this.token = token;
        this.record = record;
    }

    public String getToken() {return token;}
    public RecordModel getRecord() {return record;}

    @NotNull
    @Override
    public String toString() {
        return String.format("Token: %s, Record: %s", token, record);
    }
}
