package the.fellowship.pocketbase.services;

import org.junit.jupiter.api.DisplayName;
import the.fellowship.pocketbase.dtos.RecordModel;

@DisplayName("RecordService")
class RecordServiceTest extends CrudServiceTest<RecordModel> {
    RecordServiceTest() {
        super(
                (client) -> new RecordService(client, "@test_collection"),
                RecordModel::getId,
                "collections/%40test_collection/records"
        );
    }
}