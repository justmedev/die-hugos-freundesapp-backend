package controller

import io.ktor.server.application.*

fun Application.configureControllers() {
    configureAuthController()
    configureCashpoolController()
    configureCashpoolTransactionsController()
    configureCashpoolMemberController()
    configureCashpoolSettlementController()
    configureUserController()
}