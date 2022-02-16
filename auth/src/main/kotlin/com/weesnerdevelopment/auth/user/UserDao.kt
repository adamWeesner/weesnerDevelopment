package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.history.toHistories
import com.weesnerdevelopment.shared.auth.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

val User.redact
    get() = User(
        uuid = this.uuid,
        name = this.name,
        email = this.email,
        photoUrl = this.photoUrl,
        username = String(Base64.getDecoder().decode(this.username)),
        dateCreated = this.dateCreated,
        dateUpdated = this.dateUpdated
    )

class UserDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDao>(UserTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    val uuid by UserTable.id
    var name by UserTable.name
    var email by UserTable.email
    var photoUrl by UserTable.photoUrl
    var username by UserTable.username
    var password by UserTable.password
    var dateCreated by UserTable.dateCreated
    var dateUpdated by UserTable.dateUpdated
    val history: SizedIterable<HistoryDao>? by HistoryDao via UserHistoryTable

    fun <T> action(event: UserDao.() -> T) = transaction { event() }
}

fun UserDao.toUser(): User = User(
    uuid = uuid.value.toString(),
    name = name,
    email = email,
    photoUrl = photoUrl,
    username = username,
    password = password,
    dateCreated = dateCreated,
    dateUpdated = dateUpdated,
    history = history?.toHistories()
)

fun SizedIterable<UserDao>.toUsers(): List<User> = map {
    it.toUser()
}
