package domain.commands

import io.ktor.utils.io.*

data class AttachImageCashpoolTransactionCommand(
    val requestingUserId: Int,
    val cashpoolId: Int,
    val transactionId: Int,
    val imageProvider: ByteReadChannel,
) {
}