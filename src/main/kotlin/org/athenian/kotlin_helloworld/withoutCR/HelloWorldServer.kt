package org.athenian.kotlin_helloworld.withoutCR

import io.grpc.ServerBuilder

class HelloWorldServer(val port: Int) {
    private val server = ServerBuilder.forPort(port).addService(GreeterImpl()).build()

    private fun start() {
        server.start()
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
        @JvmStatic
        fun main(args: Array<String>) {
            val port = System.getenv("PORT")?.toInt() ?: 50051
            with(HelloWorldServer(port)) {
                start()
                server.awaitTermination()
            }
        }
    }
}
