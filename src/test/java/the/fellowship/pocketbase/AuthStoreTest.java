package the.fellowship.pocketbase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import the.fellowship.pocketbase.dtos.RecordModel;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthStoreTest {
    @Nested
    @DisplayName("AuthStore.getToken() and AuthStore.getRecord()")
    class GetterTests {
        @Test
        @DisplayName("read getters")
        void readGetters() {
            final AuthStore store = new AuthStore();

            assertTrue(store.getToken().isEmpty());
            assertNull(store.getRecord());

            store.save("test_token", new RecordModel(Map.of("id", "test")));

            assertEquals("test_token", store.getToken());
            assertEquals("test", store.getRecord().getId());
        }
    }

    @Nested
    @DisplayName("AuthStore.isValid()")
    class ValidityTests {
        @Test
        @DisplayName("with empty token")
        void withEmptyToken() {
            final AuthStore store = new AuthStore();

            assertFalse(store.isValid());
        }

        @Test
        @DisplayName("with invalid JWT token")
        void withInvalidJWTToken() {
            final AuthStore store = new AuthStore();

            store.save("invalid", null);

            assertFalse(store.isValid());
        }

        @Test
        @DisplayName("with expired JWT token")
        void withExpiredJWTToken() {
            final String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NDA5OTE2NjF9.TxZjXz_Ks665Hju0FkZSGqHFCYBbgBmMGOLnIzkg9Dg";
            final AuthStore store = new AuthStore();

            store.save(token, null);

            assertFalse(store.isValid());
        }

        @Test
        @DisplayName("with valid JWT token")
        void withValidJWTToken() {
            final String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4OTM0NTI0NjF9.yVr-4JxMz6qUf1MIlGx8iW2ktUrQaFecjY_TMm7Bo4o";
            final AuthStore store = new AuthStore();

            store.save(token, null);

            assertTrue(store.isValid());
        }
    }

    @Nested
    @DisplayName("AuthStore.save()")
    class SaveTests {
        @Test
        @DisplayName("saves new token and model")
        void savesNewTokenAndModel() {
            final AuthStore store = new AuthStore();
            final String testToken = "test_token";
            final RecordModel testModel = new RecordModel(Map.of("id", "test"));

            store.setOnChange(event -> {
                assertEquals(testToken, event.token());
                assertEquals(testModel, event.record());
            });

            store.save(testToken, testModel);

            assertEquals(testToken, store.getToken());
            assertEquals(testModel, store.getRecord());
        }
    }

    @Nested
    @DisplayName("AuthStore.clear()")
    class ClearTests {
        @Test
        @DisplayName("clears the stored token and model")
        void clearsTheStoredTokenAndModel() {
            final AuthStore store = new AuthStore();
            final String testToken = "test_token";
            final RecordModel testModel = new RecordModel(Map.of("id", "test"));

            store.save(testToken, testModel);

            assertEquals(testToken, store.getToken());
            assertEquals(testModel, store.getRecord());

            store.setOnChange(event -> {
                assertTrue(event.token().isEmpty());
                assertNull(event.record());
            });

            store.clear();

            assertTrue(store.getToken().isEmpty());
            assertNull(store.getRecord());
        }
    }
}