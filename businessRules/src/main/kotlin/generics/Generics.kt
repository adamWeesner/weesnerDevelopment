package generics

import com.squareup.moshi.JsonClass
import org.jetbrains.exposed.sql.Table

open class IdTable : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val dateCreated = long("dateCreated")
    val dateUpdated = long("dateUpdated")
}

@JsonClass(generateAdapter = true)
open class GenericItem(
    open val id: Int?,
    open val dateCreated: Long,
    open val dateUpdated: Long
)