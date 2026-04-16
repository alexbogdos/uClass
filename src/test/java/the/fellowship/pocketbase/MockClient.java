package the.fellowship.pocketbase;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class MockClient implements Interceptor {
    private int code;
    private Function<Request, Response> response;
    private final Consumer<Request> consumer;

    public MockClient(Consumer<Request> consumer) {
        this(200, consumer);
    }

    public MockClient(int code, Consumer<Request> consumer) {
        this.code = code;
        this.consumer = consumer;
    }

    public MockClient(Function<Request, Response> response, Consumer<Request> consumer) {
        this.response = response;
        this.consumer = consumer;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        consumer.accept(request);

        if (response != null) {
            return response.apply(request);
        }

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .body(ResponseBody.create("", MediaType.parse("text/plain")))
                .build();
    }
}
