package domain.commands


data class CreateCashpoolCommand(
    val title: String,
    val description: String,
    val ownerId: Int
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(description.isNotBlank()) { "Description cannot be blank" }
    }
}