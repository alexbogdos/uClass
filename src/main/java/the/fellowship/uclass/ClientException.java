package the.fellowship.uclass;

import okhttp3.HttpUrl;

import java.util.Map;

public class ClientException extends Exception {
    /**
     * The [HttpUrl] of the failed request.
     */
    private final HttpUrl url;

    /**
     * The status code of the failed request.
     */
    private final int statusCode;

    /**
     * Contains the error response.
     */
    private final String response;

    /**
     * The original response error (could be anything - String, Exception, etc.).
     */
    private final Throwable originalError;

    public ClientException(HttpUrl url, int statusCode, String response) {
        this(url, statusCode, response, null);
    }

    public ClientException(HttpUrl url, Throwable originalError) {
        this(url, -1, null, originalError);
    }

    public ClientException(HttpUrl url, int statusCode, String response, Throwable originalError) {
        this.url = url;
        this.statusCode = statusCode;
        this.response = response;
        this.originalError = originalError;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }

    public Throwable getOriginalError() {
        return originalError;
    }

    @Override
    public String toString() {
        return "[ClientException]" +
                "\nurl=" + url +
                "\nstatusCode=" + (statusCode >= 0 ? statusCode : "ECONNREFUSED") +
                //"\nresponse=" + response +
                "\noriginalError=" + originalError;
    }
}
