package user

import generics.GenericItem
import generics.IdTable

object Users : IdTable() {
    val uuid = varchar("uuid", 225)
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val photoUrl = varchar("photoUrl", 255)
}

data class User(
    override var id: Int? = null,
    val uuid: String?,
    val name: String?,
    val email: String?,
    val photoUrl: String?,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)