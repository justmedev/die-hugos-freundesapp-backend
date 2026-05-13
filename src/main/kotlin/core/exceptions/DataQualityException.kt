package core.exceptions

class DataQualityException(msg: String) : Exception("Data quality issue! $msg")