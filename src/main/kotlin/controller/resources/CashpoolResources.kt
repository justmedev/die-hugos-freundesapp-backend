package controller.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/cashpools")
class CashpoolsResource {

    @Serializable
    @Resource("{id}")
    class Id(val parent: CashpoolsResource = CashpoolsResource(), val id: Int) {

        @Serializable
        @Resource("transactions")
        class Transactions(val parent: Id) {

            @Serializable
            @Resource("{transactionId}")
            class Transaction(val parent: Transactions, val transactionId: Int)
        }
    }
}