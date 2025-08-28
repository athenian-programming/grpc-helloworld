package org.athenian.helloworld;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class HelloWorldClient
    implements Closeable {
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterStub asyncStub;

    public HelloWorldClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build());
    }

    public HelloWorldClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = GreeterGrpc.newBlockingStub(channel);
        this.asyncStub = GreeterGrpc.newStub(channel);
    }

    public static void main(String... args)
        throws Exception {

        /* Access a service running on the local machine on port 50051 */
        try (HelloWorldClient client = new HelloWorldClient("localhost", 50051)) {
            String name = "world";

            if (args.length > 0)
                name = args[0]; /* Use the arg as the name to greet if provided */

            client.sayHello(name);
            client.sayHelloWithManyRequests(name);
            client.sayHelloWithManyReplies(name);
            client.sayHelloWithManyRequestsAndReplies(name);
        }
    }

    public void sayHello(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response = this.blockingStub.sayHello(request);
        System.out.printf("sayHello() response: %s\n%n", response.getMessage());
    }

    public void sayHelloWithManyRequests(String name) {

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<HelloReply> responseObserver =
            new StreamObserver<HelloReply>() {
                @Override
                public void onNext(HelloReply reply) {
                    System.out.printf("sayHelloWithManyRequests() response: %s\n%n", reply.getMessage());
                }

                @Override
                public void onError(Throwable t) {
                    Status status = Status.fromThrowable(t);
                    System.out.printf("sayHelloWithMayRequests() failed: %s%n", status);
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                }
            };

        StreamObserver<HelloRequest> requestObserver = asyncStub.sayHelloWithManyRequests(responseObserver);

        try {
            for (int i = 0; i < 5; i++) {
                HelloRequest request = HelloRequest.newBuilder().setName(format("%s-%d", name, i)).build();
                requestObserver.onNext(request);

                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return;
                }
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(e);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        // Receiving happens asynchronously
        try {
            finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sayHelloWithManyReplies(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();

        System.out.println("sayHelloWithManyReplies() responses:");
        blockingStub.sayHelloWithManyReplies(request).forEachRemaining((reply) -> System.out.println(reply.getMessage()));
        System.out.println();
    }

    public void sayHelloWithManyRequestsAndReplies(String name) {

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<HelloReply> responseObserver =
            new StreamObserver<HelloReply>() {
                @Override
                public void onNext(HelloReply reply) {
                    System.out.printf("sayHelloWithManyRequestsAndReplies() response: %s%n", reply.getMessage());
                }

                @Override
                public void onError(Throwable t) {
                    Status status = Status.fromThrowable(t);
                    System.out.printf("sayHelloWithManyRequestsAndReplies() failed: %s%n", status);
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                }
            };

        StreamObserver<HelloRequest> requestObserver = asyncStub.sayHelloWithManyRequestsAndReplies(responseObserver);

        try {
            for (int i = 0; i < 5; i++) {
                HelloRequest request = HelloRequest.newBuilder().setName(format("%s-%d", name, i)).build();
                System.out.printf("sayHelloWithManyRequestsAndReplies() request: %s%n", request.getName());
                requestObserver.onNext(request);

                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return;
                }
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(e);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        // Receiving happens asynchronously
        try {
            finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close()
        throws IOException {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
    }
}
