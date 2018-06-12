package app.javache;

import app.javache.api.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

public class ConnectionHandler extends Thread {
    private static final int CONNECTION_KILL_LIMIT = 5000;

    private static final String REQUEST_CONTENT_LOADING_FAILURE_EXCEPTION_MESSAGE = "Failed loading request content.";

    private Socket clientSocket;

    private InputStream clientSocketInputStream;

    private OutputStream clientSocketOutputStream;

    private Set<RequestHandler> requestHandlers;

    public ConnectionHandler(Socket clientSocket, Set<RequestHandler> requestHandlers) {
        this.initializeConnection(clientSocket);
        this.requestHandlers = requestHandlers;
    }

    private void initializeConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.clientSocketInputStream = this.clientSocket.getInputStream();
            this.clientSocketOutputStream = this.clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClientConnection() {
        for (RequestHandler requestHandler : this.requestHandlers) {
            requestHandler.handleRequest(this.clientSocketInputStream, this.clientSocketOutputStream);

            if (requestHandler.hasIntercepted()) break;
        }

    }

    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocketInputStream.close();
            this.clientSocketOutputStream.close();
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}






