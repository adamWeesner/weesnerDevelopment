package generics

import com.squareup.moshi.JsonClass
import org.jetbrains.exposed.sql.Table

/**
 * Generic [Table] that has [id], [dateCreated], and [dateUpdated] added by default.
 */
open class IdTable : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val dateCreated = long("dateCreated")
    val dateUpdated = long("dateUpdated")
}

/**
 * Generic item to map to the [IdTable].
 */
@JsonClass(generateAdapter = true)
open class GenericItem(
    open val id: Int?,
    open val dateCreated: Long,
    open val dateUpdated: Long
)

class InvalidAttributeException(value: String) : IllegalArgumentException("$value is required but missing or invalid")