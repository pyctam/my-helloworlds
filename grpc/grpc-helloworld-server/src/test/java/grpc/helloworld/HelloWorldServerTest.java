package grpc.helloworld;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

/**
 * Created by rustambogubaev on 11/29/16.
 */
public class HelloWorldServerTest {
    private HelloWorldServer server;

    @Test
    public void serverShouldBeStarted(){
        // given
        server = new HelloWorldServer();

        // when
        server.start();

        //then
        assertFalse(server.isTerminated());
    }

    @AfterClass
    public void finalizeTestClass(){
        if (server != null){
            server.shutdown();
        }
    }
}