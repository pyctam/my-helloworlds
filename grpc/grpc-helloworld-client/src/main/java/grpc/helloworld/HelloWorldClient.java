package grpc.helloworld;

import grpc.helloworld.proto.HelloWorld;
import grpc.helloworld.proto.HelloWorldServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hello world!
 */
public class HelloWorldClient {
    private final ManagedChannel channel;
    private final HelloWorldServiceGrpc.HelloWorldServiceStub stubAsync;
    private final HelloWorldServiceGrpc.HelloWorldServiceBlockingStub stubSync;

    public HelloWorldClient() {
        this.channel = ManagedChannelBuilder.forAddress("localhost", HelloWorldServer.port).usePlaintext(true).build();
        this.stubAsync = HelloWorldServiceGrpc.newStub(this.channel);
        this.stubSync = HelloWorldServiceGrpc.newBlockingStub(this.channel);
    }

    public io.reactivex.Observable<HelloWorld.Entity> observeQuery(final int count) {
        return Observable.create(new ObservableOnSubscribe<HelloWorld.Entity>() {
            public void subscribe(final ObservableEmitter<HelloWorld.Entity> observableEmitter) throws Exception {
                StreamObserver<HelloWorld.Request> requestObserver = stubAsync.queryAsync(new StreamObserver<HelloWorld.Entity>() {
                    public void onNext(HelloWorld.Entity entity) {
                        observableEmitter.onNext(entity);
                    }

                    public void onError(Throwable throwable) {
                        observableEmitter.onError(throwable);
                    }

                    public void onCompleted() {
                        observableEmitter.onComplete();
                    }
                });

                try{
                    HelloWorld.Request request = HelloWorld.Request.newBuilder().setCount(count).build();

                    requestObserver.onNext(request);
                    requestObserver.onCompleted();
                }catch (Exception e){
                    requestObserver.onError(e);
                }
            }
        });
    }

    public List<HelloWorld.Entity> query(int count) {
        HelloWorld.Request request = HelloWorld.Request.newBuilder().setCount(count).build();
        Iterator<HelloWorld.Entity> iterator = stubSync.queryBlocking(request);
        List<HelloWorld.Entity> list = new ArrayList<HelloWorld.Entity>();

        while (iterator.hasNext()) {
            HelloWorld.Entity entity = iterator.next();
            list.add(entity);
        }

        return list;
    }

    public void shutdown() {
        if (this.channel != null) {
            System.out.print("Shutting down managed channel.");
            this.channel.shutdown();
        }
    }
}
