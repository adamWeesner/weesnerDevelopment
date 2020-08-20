package generics

import org.jetbrains.exposed.sql.Table

/**
 * [GenericTable] that has [id], [dateCreated], and [dateUpdated] added by default.
 */
open class IdTable : Table(), GenericTable {
    final override val id = integer("id").autoIncrement()
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
