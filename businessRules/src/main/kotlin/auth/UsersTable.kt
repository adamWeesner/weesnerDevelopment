package auth

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object UsersTable : IdTable(), HistoricTable {
    val uuid = varchar("uuid", 225).uniqueIndex()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val photoUrl = varchar("photoUrl", 255).nullable()
    val username = varchar("username", 255).uniqueIndex()
    val password = varchar("password", 255)
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
