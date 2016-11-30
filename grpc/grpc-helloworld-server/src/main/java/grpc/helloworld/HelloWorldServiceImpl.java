package grpc.helloworld;

import grpc.helloworld.proto.HelloWorld;
import grpc.helloworld.proto.HelloWorldServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomUtils;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextLong;

/**
 * Created by rustambogubaev on 11/29/16.
 */
public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {
    private HelloWorld.Entity buildEntity(int i) {
        long id = nextLong(0, Integer.MAX_VALUE);
        String title = randomAlphanumeric(8) + " " + randomAlphanumeric(16);
        boolean active = RandomUtils.nextBoolean();

        return HelloWorld.Entity.newBuilder().setSequence(i+1).setId(id).setTitle(title).setActive(active).build();
    }

    @Override
    public void queryBlocking(HelloWorld.Request request, StreamObserver<HelloWorld.Entity> responseObserver) {
        int count = request.getCount();

        try {
            for (int i = 0; i < count; i++) {
                HelloWorld.Entity entity = buildEntity(i);
                responseObserver.onNext(entity);
                //System.out.println(System.nanoTime() + ": [1] entity (" + entity.getId() + "): " + entity);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<HelloWorld.Request> queryAsync(final StreamObserver<HelloWorld.Entity> responseObserver) {
        return new StreamObserver<HelloWorld.Request>(){
            private HelloWorld.Request request;

            public void onNext(HelloWorld.Request request) {
                this.request = request;
            }

            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            public void onCompleted() {
                for (int i = 0; i < request.getCount(); i++) {
                    HelloWorld.Entity entity = buildEntity(i);
                    responseObserver.onNext(entity);
                    //System.out.println(System.nanoTime() + ": [1] entity (" + entity.getId() + "): " + entity);
                }

                responseObserver.onCompleted();
            }
        };
    }
}
