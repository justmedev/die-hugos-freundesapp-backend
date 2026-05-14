package core.exceptions

open class Forbidden(override val message: String = "You are not allowed to access this resource") : Exception()