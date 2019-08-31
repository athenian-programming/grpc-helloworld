package org.athenian.kotlin_helloworld

import io.grpc.Server
import io.grpc.ServerBuilder
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.opencensus.trace.Tracer
import io.opencensus.trace.Tracing
import io.prometheus.client.exporter.HTTPServer
import java.io.IOException


class HelloWorldServer {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {
        server = ServerBuilder.forPort(port)
                .addService(GreeterImpl())
                .build()
                .start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
                Thread {
                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                    System.err.println("*** shutting down gRPC server since JVM is shutting down")
                    server?.shutdown()
                    System.err.println("*** server shut down")
                })
    }

    companion object {
        const val port = 50051
        val tracer: Tracer = Tracing.getTracer()

        @Throws(IOException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            PrometheusStatsCollector.createAndRegister()
            RpcViews.registerServerGrpcViews()
            HTTPServer("localhost", 8888, true)

            HelloWorldServer()
                    .apply {
                        start()
                        server?.awaitTermination()
                    }
        }
    }
}
