package org.athenian.kotlin_helloworld.withoutCR

import io.grpc.Server
import io.grpc.ServerBuilder

class HelloWorldServer {
    private lateinit var server: Server

    private fun start() {
        server = ServerBuilder.forPort(port).addService(GreeterImpl()).build().start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                server.shutdown()
                System.err.println("*** server shut down")
            })
    }

    companion object {
        const val port = 50051

        @JvmStatic
        fun main(args: Array<String>) {
            HelloWorldServer()
                .apply {
                    start()
                    server.awaitTermination()
                }
        }
    }
}
