package core.exceptions

open class Conflict(message: String = "Entity is in conflict with another entity") : Exception(message)