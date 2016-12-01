package grpc.helloworld;

import grpc.helloworld.proto.HelloWorld;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.mockito.Mockito;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by rustambogubaev on 11/29/16.
 */
public class HelloWorldClientTest {
    private HelloWorldServer server;
    private HelloWorldClient client;
    private int runs = 10000;

    @BeforeMethod
    public void initTestClass() throws InterruptedException {
        server = new HelloWorldServer();
        server.start();
        assertFalse(server.isTerminated());
    }

    @Test(priority = 1)
    public void blockingResponseShouldContainRequestedNumberOfEntities() throws InterruptedException {
        client = new HelloWorldClient();

        System.out.println("Server: " + server);
        System.out.println("Client: " + client);

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
        System.out.println("\n\nBlocking execution of " + runs + "request[s] is completed in " + delta + " millis.");
    }

    @Test(priority = 2)
    public void asyncResponseShouldContainRequestedNumberOfEntities() throws InterruptedException {
        client = new HelloWorldClient();

        System.out.println("Server: " + server);
        System.out.println("Client: " + client);

        // given
        final int count = 10;

        final long started = System.nanoTime();

        for (int i = 0; i < runs; i++) {
            // when
            final int finalI = i;
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
                    System.out.println("\n\nAsync execution of " + (finalI + 1) + "th request is completed in " + delta + " millis.");
                }
            });

            if (i % 100 == 0) {
                System.out.println();
            }

            System.out.print(".");
        }

        Thread.sleep(1800);
    }

    @Test(priority = 3)
    public void blockingResponseShouldContainSomeNumberOfEntities() {
        client = new HelloWorldClient(500);

        System.out.println("Server: " + server);
        System.out.println("Client: " + client);

        // given
        int count = 20;

        // when
        List<HelloWorld.Entity> entities = client.query(count);

        // then
        assertTrue(entities.size() >= 10);
    }

    @Test(priority = 4)
    public void asyncResponseShouldContainSomeNumberOfEntities() throws InterruptedException {
        client = new HelloWorldClient(600);

        System.out.println("Server: " + server);
        System.out.println("Client: " + client);

        // given
        int count = 20;
        Observer<HelloWorld.Entity> observer = spy(new Observer<HelloWorld.Entity>() {
            private List<HelloWorld.Entity> entities = new ArrayList<HelloWorld.Entity>();
            private Disposable disposable;

            public void onSubscribe(Disposable disposable) {
                this.disposable = disposable;
            }

            public void onNext(HelloWorld.Entity entity) {
                entities.add(entity);
            }

            public void onError(Throwable throwable) {
                if (disposable != null) {
                    disposable.dispose();
                }
            }

            public void onComplete() {
                if (disposable != null) {
                    disposable.dispose();
                }
            }
        });

        client.observeQuery(count).subscribe(observer);
        Thread.sleep(2000);

        // then
        verify(observer, times(11)).onNext(any(HelloWorld.Entity.class));
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @AfterMethod
    public void finalizeTestClass() {
        if (client != null) {
            client.shutdown();
        }

        if (server != null) {
            server.shutdown();
        }
    }
}


