package categories

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object CategoriesTable : IdTable(), HistoricTable {
    val name = varchar("name", 255).uniqueIndex()
    val ownerId = (varchar("ownerId", 255) references UsersTable.uuid).nullable()
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
