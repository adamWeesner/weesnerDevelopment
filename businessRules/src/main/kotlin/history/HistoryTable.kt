package history

import auth.UsersTable
import generics.IdTable

object HistoryTable : IdTable() {
    val field = varchar("field", 255)
    val oldValue = varchar("oldValue", 500)
    val newValue = varchar("newValue", 500)
    val updatedBy = varchar("updatedBy", 255) references UsersTable.uuid
}
