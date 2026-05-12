package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.resources.Resources

fun Application.configureResources() {
    install(Resources)
}