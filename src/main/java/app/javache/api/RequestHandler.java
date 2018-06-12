package app.javache.api;

public interface RequestHandler {
    byte[] handleRequest(String requestContent);

    boolean hasIntercepted();
}