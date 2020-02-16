package auth

import generics.IdTable

object UsersTable : IdTable() {
    val uuid = varchar("uuid", 225).nullable()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val photoUrl = varchar("photoUrl", 255).nullable()
    val username = varchar("username", 255).nullable()
    val password = varchar("password", 255).nullable()
}