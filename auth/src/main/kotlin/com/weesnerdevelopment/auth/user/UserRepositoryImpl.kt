package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.currentTimeMillis
import diff
import org.jetbrains.exposed.sql.and
import java.util.*

object UserRepositoryImpl : UserRepository {
    override fun account(id: UUID): User? {
        return runCatching {
            UserDao.action { get(id) }.toUser()
        }.getOrNull()
    }

    override fun create(new: User): User {
        return UserDao.action {
            new(new.uuid.asUuid) {
                name = new.name!!
                email = new.email!!
                photoUrl = new.photoUrl
                username = new.username!!
                password = new.password!!
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated
            }
        }.toUser()
    }

    override fun login(user: HashedUser): User? {
        return UserDao.action {
            find {
                (UserTable.username eq user.username) and (UserTable.password eq user.password)
            }.firstOrNull()
                ?.toUser()
        }
    }

    override fun update(updated: User): User? {
        val foundUser = UserDao.action { get(UUID.fromString(updated.uuid)) }

        foundUser.apply {
            name = updated.name!!
            email = updated.email!!
            photoUrl = updated.photoUrl
            username = updated.username!!
            password = updated.password!!
            dateUpdated = currentTimeMillis()
        }

        // update history
        foundUser.toUser().diff(updated).updates(foundUser.toUser()).forEach {
            HistoryDao.action {
                new {
                    field = it.field
                    oldValue = it.oldValue
                    newValue = it.newValue
                    updatedBy = foundUser.uuid
                    dateCreated = it.dateCreated
                    dateUpdated = it.dateUpdated
                }
            }
        }

        return account(foundUser.uuid.value)
    }

    override fun delete(id: UUID): Boolean {
        val foundUser = UserDao.action { get(id) }

        if (foundUser == null)
            return false

        foundUser.delete()
        return true
    }

    override fun info(id: String): User? {
        return account(id.asUuid)
    }
}