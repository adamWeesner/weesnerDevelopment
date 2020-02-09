package model

import generics.GenericItem

data class User(
    override var id: Int?,
    val name: String?,
    val email: String?,
    val photoUrl: String?,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)

class UserNotValidException(
    reason: String = "Not a valid user, something happened"
) : IllegalArgumentException(reason)