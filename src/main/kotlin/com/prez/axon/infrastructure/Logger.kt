package com.prez.axon.infrastructure

import mu.KotlinLogging
import org.axonframework.modelling.command.AggregateLifecycle

object Logger {
    val log = KotlinLogging.logger {}

    fun debugLive(message: Any) {
        if (AggregateLifecycle.isLive()) {
            log.debug { message }
        }
    }
}