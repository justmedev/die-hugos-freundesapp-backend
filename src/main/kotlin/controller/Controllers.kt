package controller

import io.ktor.server.application.*

fun Application.configureControllers() {
    configureCashpoolController()
    configureCashpoolTransactionsController()
    configureCashpoolMemberController()
    configureCashpoolSuggestedSettlementController()
    configureCashpoolSettlementController()
    configureUserController()
}