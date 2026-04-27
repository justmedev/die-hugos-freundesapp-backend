package at.ilja

-busch

import io.ktor.server.application.*
import dev.hayden.KHealth

fun Application.configureMonitoring() {
    install(KHealth)
}