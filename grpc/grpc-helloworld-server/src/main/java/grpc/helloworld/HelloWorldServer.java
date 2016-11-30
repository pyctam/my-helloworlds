package grpc.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Hello world!
 */
public class HelloWorldServer {
    final public static int port = 8123;
    final private Server server;

    public HelloWorldServer() {
        this.server = ServerBuilder.forPort(port).addService(new HelloWorldServiceImpl()).build();
    }

    public void start() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Shutting down");
                    server.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Runnable r = new Runnable() {
            public void run() {
                try {
                    server.start();
                    System.out.println("Server started on port " + server.getPort());

                    server.awaitTermination();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(r).start();
    }

    public boolean isTerminated() {
        return server == null || server.isTerminated();
    }

    public void shutdown() {
        if (server != null){
            server.shutdown();
        }
    }
}
