package controller

import io.ktor.server.application.Application

fun Application.configureControllers() {
    configureAuthController()
    configureCashpoolController()
    configureCashpoolTransactionsController()
}