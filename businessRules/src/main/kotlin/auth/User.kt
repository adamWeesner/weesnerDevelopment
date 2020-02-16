package auth

import generics.GenericItem
import generics.IdTable

object UsersTable : IdTable() {
    val uuid = varchar("uuid", 225).nullable()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val photoUrl = varchar("photoUrl", 255).nullable()
    val username = varchar("username", 255).nullable()
    val password = varchar("password", 255).nullable()
}

data class User(
    override var id: Int? = null,
    val uuid: String? = null,
    val name: String?,
    val email: String?,
    val photoUrl: String? = null,
    var username: String? = null,
    var password: String? = null,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated) {
    fun asHashed() = if (username != null && password != null) HashedUser(username!!, password!!) else null
}

data class HashedUser(
    val username: String,
    val password: String
)

data class InvalidUserException(
    val url: String,
    val statusCode: Int? = -1,
    val reasonCode: Int
)

enum class InvalidUserReason(val code: Int) {
    General(1000),
    Expired(1001),
    InvalidJwt(1002)
}