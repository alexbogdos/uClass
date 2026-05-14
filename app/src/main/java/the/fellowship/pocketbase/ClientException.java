package the.fellowship.pocketbase;

import java.util.Map;

import okhttp3.HttpUrl;

public class ClientException extends Exception {
    /**
     * The [HttpUrl] of the failed request.
     */
    private final HttpUrl url;

    /**
     * Indicates whether the error is a result from request cancellation/abort.
     */
    private final boolean isAbort;

    /**
     * The status code of the failed request.
     */
    private final int statusCode;

    /**
     * Contains the JSON API error response.
     */
    private final Map<String, ?> response;

    /**
     * The original response error (could be anything - String, Exception, etc.).
     */
    private final Throwable originalError;

    public ClientException(HttpUrl url, int statusCode, Map<String, ?> response) {
        this(url, false, statusCode, response, null);
    }

    public ClientException(HttpUrl url, Throwable originalError) {
        this(url, false, -1, null, originalError);
    }

    public ClientException(HttpUrl url, boolean isAbort, int statusCode, Map<String, ?> response, Throwable originalError) {
        this.url = url;
        this.isAbort = isAbort;
        this.statusCode = statusCode;
        this.response = response;
        this.originalError = originalError;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public boolean isAbort() {
        return isAbort;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, ?> getResponse() {
        return response;
    }

    public Throwable getOriginalError() {
        return originalError;
    }

    @Override
    public String toString() {
        return "[ClientException]" +
                "\nurl=" + url +
                "\nisAbort=" + isAbort +
                "\nstatusCode=" + (statusCode >= 0 ? statusCode : "ECONNREFUSED") +
                "\nresponse=" + response +
                "\noriginalError=" + originalError;
    }
}
