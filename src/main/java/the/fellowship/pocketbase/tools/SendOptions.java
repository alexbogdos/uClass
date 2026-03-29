package the.fellowship.pocketbase.tools;

import java.util.HashMap;
import java.util.Map;

public class SendOptions {
    /**
     * Method used for request.
     */
    private String method;

    /**
     * Custom headers to send with the requests.
     */
    private Map<String, String> headers;

    /**
     * The body of the request (serialized automatically for json requests).
     */
    private Object body;

    /**
     * Query parameters that will be appended to the request url.
     */
    private Map<String, Object> query;

    /**
     * The request identifier that can be used to cancel pending requests.
     */
    private String requestKey;

    public SendOptions() {
        this.headers = new HashMap<>();
        this.query = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Map<String, Object> getQuery() {
        return query;
    }

    public void setQuery(Map<String, Object> query) {
        this.query = query;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
