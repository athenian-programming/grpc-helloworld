package org.athenian.kotlin_helloworld

import org.athenian.helloworld.HelloRequest

object Msgs {
    fun helloRequest(block: HelloRequest.Builder.() -> Unit): HelloRequest =
        HelloRequest.newBuilder().let {
            block.invoke(it)
            it.build()
        }
}