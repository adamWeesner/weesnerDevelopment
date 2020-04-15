package generics

import org.jetbrains.exposed.sql.Table

/**
 * Table definition for the database.
 */
interface GenericTable {
    val id: Any
    val dateCreated: Any
    val dateUpdated: Any
}

/**
 * [GenericTable] that has [id], [dateCreated], and [dateUpdated] added by default.
 */
open class IdTable : Table(), GenericTable {
    override val id = integer("id").primaryKey().autoIncrement()
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")
}

class InvalidAttributeException(value: String) : IllegalArgumentException("$value is required but missing or invalid")