package controller.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/cashpools")
class CashpoolResource {

    @Serializable
    @Resource("{id}")
    class Id(val parent: CashpoolResource = CashpoolResource(), val id: Int) {

        @Serializable
        @Resource("transactions")
        class Transactions(val parent: Id) {

            @Serializable
            @Resource("{transactionId}")
            class Transaction(val parent: Transactions, val transactionId: Int)
        }

        @Serializable
        @Resource("members")
        class Members(val parent: Id)

        @Serializable
        @Resource("settle")
        class Settle(val parent: Id) {
            @Serializable
            @Resource("suggest")
            class Suggest(val parent: Settle)
        }
    }
}