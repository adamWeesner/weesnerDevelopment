package generics

class InvalidAttributeException(value: String) : IllegalArgumentException("$value is required but missing or invalid")