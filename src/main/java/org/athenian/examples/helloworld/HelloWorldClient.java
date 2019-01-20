package org.athenian.examples.helloworld;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HelloWorldClient {
  private final ManagedChannel                  channel;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;
  private final GreeterGrpc.GreeterStub         asyncStub;

  public HelloWorldClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port)
                              // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                              // needing certificates.
                              .usePlaintext()
                              .build());
  }

  HelloWorldClient(ManagedChannel channel) {
    this.channel = channel;
    this.blockingStub = GreeterGrpc.newBlockingStub(channel);
    this.asyncStub = GreeterGrpc.newStub(channel);
  }

  public static void main(String... args)
      throws Exception {
    HelloWorldClient client = new HelloWorldClient("localhost", 50051);
    try {
      /* Access a service running on the local machine on port 50051 */
      String user = "world";
      if (args.length > 0) {
        user = args[0]; /* Use the arg as the name to greet if provided */
      }
      client.sayHello(user);
      client.sayHelloWithManyRequests(user);
      client.sayHelloWithManyReplies(user);
    }
    finally {
      client.shutdown();
    }
  }

  public void shutdown()
      throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void sayHello(String name) {
    HelloRequest request = HelloRequest.newBuilder()
                                       .setName(name)
                                       .build();
    HelloReply response;
    try {
      response = this.blockingStub.sayHello(request);
    }
    catch (StatusRuntimeException e) {
      System.out.println(String.format("sayHello() failed: %s", e.getStatus()));
      return;
    }
    System.out.println(String.format("sayHello() response: %s\n", response.getMessage()));
  }

  public void sayHelloWithManyRequests(String name) {

    final CountDownLatch finishLatch = new CountDownLatch(1);

    StreamObserver<HelloReply> responseObserver =
        new StreamObserver<HelloReply>() {
          @Override
          public void onNext(HelloReply reply) {
            System.out.println(String.format("sayHelloWithManyRequests() response: %s\n", reply.getMessage()));
          }

          @Override
          public void onError(Throwable t) {
            Status status = Status.fromThrowable(t);
            System.out.println(String.format("sayHelloWithMayRequests() failed: %s", status));
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
        HelloRequest request = HelloRequest.newBuilder()
                                           .setName(String.format("%s-%d", name, i))
                                           .build();
        requestObserver.onNext(request);

        if (finishLatch.getCount() == 0) {
          // RPC completed or errored before we finished sending.
          // Sending further requests won't error, but they will just be thrown away.
          return;
        }
      }
    }
    catch (RuntimeException e) {
      // Cancel RPC
      requestObserver.onError(e);
      throw e;
    }

    // Mark the end of requests
    requestObserver.onCompleted();

    // Receiving happens asynchronously
    try {
      finishLatch.await(1, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void sayHelloWithManyReplies(String name) {

    HelloRequest request = HelloRequest.newBuilder()
                                       .setName(name)
                                       .build();

    Iterator<HelloReply> replies = blockingStub.sayHelloWithManyReplies(request);

    System.out.println("sayHelloWithManyReplies() responses:");
    replies.forEachRemaining((reply) -> {
      System.out.println(reply.getMessage());
    });
    System.out.println();
  }
}
