package academy.model;

public class LogEntry {
    public final String resource;
    public final int statusCode;
    public final long responseSize;

    public LogEntry(String resource, int statusCode, long responseSize) {
        this.resource = resource;
        this.statusCode = statusCode;
        this.responseSize = responseSize;
    }
}
