package history

import auth.UsersTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object HistoryTable : IdTable() {
    val field = varchar("field", 255)
    val oldValue = varchar("oldValue", 500)
    val newValue = varchar("newValue", 500)
    val updatedBy = reference("updatedBy", UsersTable.uuid, ReferenceOption.CASCADE)
}
