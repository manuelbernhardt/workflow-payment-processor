package io.bernhardt.workflow.payment

import java.util.concurrent.TimeUnit

fun main() {
    App().factory.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
}