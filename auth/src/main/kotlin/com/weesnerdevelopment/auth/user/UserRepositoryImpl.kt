package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.and

object UserRepositoryImpl : UserRepository {
    override fun account(id: String): User? =
        UserDao.action { get(id.asUuid) }?.toUser()

    override fun create(new: User): User? {
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
        }?.toUser()
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
        val foundUser = UserDao.action { get(updated.uuid.asUuid) }

        if (foundUser == null) {
            Log.error("Could not find user matching the uuid ${updated.uuid}")
            return null
        }

        val updatedUsername = updated.username
        val updatedPassword = updated.password

        if (updatedUsername == null || updatedPassword == null) {
            Log.error("Trying to update a user with no username or password, this is not valid")
            return null
        }

        UserDao.action {
            foundUser.apply {
                name = updated.name
                email = updated.email
                photoUrl = updated.photoUrl
                username = updatedUsername
                password = updatedPassword
                dateUpdated = currentTimeMillis()
            }
        }

        // update history
//        foundUser.toUser().diff(updated).updates(foundUser.toUser()).forEach {
//            HistoryDao.action {
//                new {
//                    field = it.field
//                    oldValue = it.oldValue
//                    newValue = it.newValue
//                    updatedBy = foundUser.uuid
//                    dateCreated = it.dateCreated
//                    dateUpdated = it.dateUpdated
//                }
//            }
//        }

        return account(foundUser.id.value.toString())
    }

    override fun delete(id: String): Boolean {
        val foundUser = UserDao.action { get(id.asUuid) }

        if (foundUser == null) {
            Log.error("Could not find a user matching the id $id to delete")
            return false
        }

        Log.info("Deleting user")
        UserDao.action { foundUser.delete() }
        return true
    }

    override fun info(id: String): User? {
        return account(id)
    }
}