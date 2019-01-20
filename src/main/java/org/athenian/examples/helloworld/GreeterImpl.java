package org.athenian.examples.helloworld;

import com.google.common.collect.Lists;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

class GreeterImpl
    extends GreeterGrpc.GreeterImplBase {

  private static final Logger logger = Logger.getLogger(GreeterImpl.class.getName());

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
            logger.log(Level.WARNING, "Encountered error in sayHelloWithManyRequests3", t);
          }

          @Override
          public void onCompleted() {
            responseObserver.onNext(HelloReply.newBuilder()
                                              .setMessage("Hello " + String.join(", ", names))
                                              .build());
            responseObserver.onCompleted();
          }
        };
  }

  @Override
  public void sayHelloWithManyReplies(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    IntStream.range(0, 10)
             .forEach((i) -> {
               HelloReply reply = HelloReply.newBuilder()
                                            .setMessage(String.format("Hello %s [%d]", request.getName(), i))
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
                                                    .setMessage(String.format("Hello %s [%d]", request.getName(), i))
                                                    .build();
                       responseObserver.onNext(reply);
                     });
          }

          @Override
          public void onError(Throwable t) {
            logger.log(Level.WARNING, "Encountered error in sayHelloWithManyRequestsAndReplies", t);
          }

          @Override
          public void onCompleted() {
            responseObserver.onCompleted();
          }
        };
  }
}
