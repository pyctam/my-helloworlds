package grpc.helloworld;

import grpc.helloworld.proto.HelloWorld;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.*;

/**
 * Created by rustambogubaev on 11/29/16.
 */
public class HelloWorldClientTest {
    private HelloWorldServer server;
    private HelloWorldClient client;
    private int runs = 10000;

    @BeforeClass
    public void initTestClass() throws InterruptedException {
        server = new HelloWorldServer();
        server.start();
        assertFalse(server.isTerminated());

        client = new HelloWorldClient();
    }

    @Test(priority = 1)
    public void blockingResponseShouldContainRequestNumberOfEntities() throws InterruptedException {
        // given
        final int count = 10;

        long started = System.nanoTime();

        for (int i = 0; i < runs; i++) {
            // when
            List<HelloWorld.Entity> entities = client.query(count);

            // then
            assertEquals(entities.size(), count);

            if (i % 100 == 0) {
                System.out.println();
            }

            System.out.print(".");
        }

        long finished = System.nanoTime();
        long delta = TimeUnit.NANOSECONDS.toMillis(finished - started);
        System.out.println("\n\nBlocking execution of " + runs + " completed in " + delta + " millis.");
    }

    @Test(priority = 2)
    public void asyncResponseShouldContainRequestNumberOfEntities() throws InterruptedException {
        // given
        final int count = 10;

        final long started = System.nanoTime();

        for (int i = 0; i < runs; i++) {
            // when
            client.observeQuery(count).subscribe(new Observer<HelloWorld.Entity>() {
                private List<HelloWorld.Entity> entities = new ArrayList<HelloWorld.Entity>();
                private Disposable disposable;


                public void onSubscribe(Disposable disposable) {
                    this.disposable = disposable;
                }

                public void onNext(HelloWorld.Entity entity) {
                    entities.add(entity);
                }

                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                public void onComplete() {
                    if (disposable != null) {
                        disposable.dispose();
                    }

                    // then
                    assertEquals(entities.size(), count);

                    long finished = System.nanoTime();
                    long delta = TimeUnit.NANOSECONDS.toMillis(finished - started);
                    System.out.println("\n\nAsync execution of " + runs + " completed in " + delta + " millis.");
                }
            });

            if (i % 100 == 0) {
                System.out.println();
            }

            System.out.print(".");
        }

        Thread.sleep(30000);
    }

    @AfterClass
    public void finalizeTestClass() {
        if (client != null) {
            client.shutdown();
        }

        if (server != null) {
            server.shutdown();
        }
    }
}