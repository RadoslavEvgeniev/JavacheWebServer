package app;

import app.javache.Server;
import app.javache.WebConstants;
import app.javache.util.JavacheConfigService;
import app.javache.util.RequestHandlerLoadingService;

import java.io.IOException;

public class StartUp {
    public static void main(String[] args) {
//        start(args);
        new RequestHandlerLoadingService().loadRequestHandlers(new JavacheConfigService().getRequestHandlerPriority());
    }

    private static void start(String[] args) {
        int port = WebConstants.DEFAULT_SERVER_PORT;

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        Server server = new Server(port);

        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

