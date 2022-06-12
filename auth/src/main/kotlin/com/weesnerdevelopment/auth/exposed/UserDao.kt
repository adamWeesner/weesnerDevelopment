package com.weesnerdevelopment.auth.exposed

import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.auth.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

val User.redact
    get() = User(
        uuid = this.uuid,
        email = this.email,
        photoUrl = this.photoUrl,
        username = String(Base64.getDecoder().decode(this.username)),
        dateCreated = this.dateCreated,
        dateUpdated = this.dateUpdated
    )

class UserDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDao>(UserTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    val uuid by UserTable.id
    var email by UserTable.email
    var photoUrl by UserTable.photoUrl
    var username by UserTable.username
    var password by UserTable.password
    var dateCreated by UserTable.dateCreated
    var dateUpdated by UserTable.dateUpdated
//    val history: SizedIterable<HistoryDao>? by HistoryDao via UserHistoryTable

    fun <T> action(event: UserDao.() -> T) = tryTransaction(event)
}

fun UserDao.toUser(): User? = action {
    User(
        uuid = uuid.value.toString(),
        email = email,
        photoUrl = photoUrl,
        username = username,
        password = password,
        dateCreated = dateCreated,
        dateUpdated = dateUpdated,
//        history = history?.toHistories()
    )
}

fun SizedIterable<UserDao>.toUsers(): List<User> = UserDao.action {
    mapNotNull {
        it.toUser()
    }
} ?: emptyList()
