package generics

import org.jetbrains.exposed.sql.Table

interface GenericTable {
    val id: Any
    val dateCreated: Any
    val dateUpdated: Any
}

/**
 * Generic [Table] that has [id], [dateCreated], and [dateUpdated] added by default.
 */
open class IdTable : Table(), GenericTable {
    override val id = integer("id").primaryKey().autoIncrement()
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")
}

/**
 * Generic item to map to the [IdTable].
 */
interface GenericItem {
    val id: Int?
    val dateCreated: Long
    val dateUpdated: Long
}

class InvalidAttributeException(value: String) : IllegalArgumentException("$value is required but missing or invalid")