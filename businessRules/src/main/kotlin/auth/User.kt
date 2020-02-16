package auth

import generics.GenericItem

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
) : GenericItem {
    fun asHashed() = if (username != null && password != null) HashedUser(username!!, password!!) else null
}

