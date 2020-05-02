package auth

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object UsersTable : IdTable(), HistoricTable {
    val uuid = varchar("uuid", 225)
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val photoUrl = varchar("photoUrl", 255).nullable()
    val username = varchar("username", 255)
    val password = varchar("password", 255)
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
