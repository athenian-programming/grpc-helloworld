package org.athenian.java_helloworld;

import com.google.common.collect.Lists;
import io.grpc.stub.StreamObserver;
import org.athenain.helloworld.GreeterGrpc;
import org.athenain.helloworld.HelloReply;
import org.athenain.helloworld.HelloRequest;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;

class GreeterImpl
    extends GreeterGrpc.GreeterImplBase {

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    HelloReply reply = HelloReply.newBuilder()
                                 .setMessage("Hello " + request.getName())
                                 .build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<HelloRequest> sayHelloWithManyRequests(StreamObserver<HelloReply> responseObserver) {
    return
        new StreamObserver<HelloRequest>() {
          final List<String> names = Lists.newArrayList();

          @Override
          public void onNext(HelloRequest request) {
            names.add(request.getName());
          }

          @Override
          public void onError(Throwable t) {
            System.out.println("Encountered error in sayHelloWithManyRequests()");
            t.printStackTrace();
          }

          @Override
          public void onCompleted() {
            HelloReply msg = HelloReply.newBuilder()
                                       .setMessage("Hello " + String.join(", ", names))
                                       .build();
            responseObserver.onNext(msg);
            responseObserver.onCompleted();
          }
        };
  }

  @Override
  public void sayHelloWithManyReplies(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    IntStream.range(0, 5)
             .forEach((i) -> {
               HelloReply reply = HelloReply.newBuilder()
                                            .setMessage(format("Hello %s [%d]", request.getName(), i))
                                            .build();
               responseObserver.onNext(reply);
             });

    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<HelloRequest> sayHelloWithManyRequestsAndReplies(StreamObserver<HelloReply> responseObserver) {
    return
        new StreamObserver<HelloRequest>() {
          @Override
          public void onNext(HelloRequest request) {
            IntStream.range(0, 5)
                     .forEach((i) -> {
                       HelloReply reply = HelloReply.newBuilder()
                                                    .setMessage(format("Hello %s [%d]", request.getName(), i))
                                                    .build();
                       responseObserver.onNext(reply);
                     });
          }

          @Override
          public void onError(Throwable t) {
            System.out.println("Encountered error in sayHelloWithManyRequestsAndReplies()");
            t.printStackTrace();
          }

          @Override
          public void onCompleted() {
            responseObserver.onCompleted();
          }
        };
  }
}
