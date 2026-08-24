package controller.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/cashpools")
class CashpoolResource {

    @Serializable
    @Resource("{cashpoolId}")
    class CashpoolId(val parent: CashpoolResource = CashpoolResource(), val cashpoolId: Int) {

        @Serializable
        @Resource("transactions")
        class Transactions(val parent: CashpoolId) {

            @Serializable
            @Resource("{transactionId}")
            class Transaction(val parent: Transactions, val transactionId: Int) {
                @Serializable
                @Resource("upload")
                class Upload(val transaction: Transaction)
            }
        }

        @Serializable
        @Resource("members")
        class Members(val parent: CashpoolId)

        @Serializable
        @Resource("settle")
        class Settle(val parent: CashpoolId) {
            @Serializable
            @Resource("suggest")
            class Suggest(val parent: Settle) {
                @Serializable
                @Resource("me")
                class Me(val parent: Suggest)
            }
        }
    }
}