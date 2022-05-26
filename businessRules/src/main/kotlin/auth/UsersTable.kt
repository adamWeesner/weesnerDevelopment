package auth

import generics.IdTable

@Deprecated("You should not use this anymore")
object UsersTable : IdTable() {
    val uuid = varchar("uuid", 225).uniqueIndex()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val photoUrl = varchar("photoUrl", 255).nullable()
    val username = varchar("username", 255).uniqueIndex()
    val password = varchar("password", 255)
//    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
