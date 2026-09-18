package core.exceptions

class NotaCashpoolMember private constructor(msg: String) : Forbidden(msg) {
    constructor() : this("User is not a member of this cashpool!")
    constructor(userId: Int = 0) : this("User $userId is not a member of this cashpool!")
}