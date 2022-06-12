package com.weesnerdevelopment.auth.repository

import auth.JwtProvider
import auth.asToken
import com.weesnerdevelopment.auth.exposed.UserDao
import com.weesnerdevelopment.auth.exposed.UserTable
import com.weesnerdevelopment.auth.exposed.toUser
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.auth.TokenUser
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.and

class UserRepositoryImpl(
    private val jwtProvider: JwtProvider
) : UserRepository {
    override suspend fun account(id: String): Result<User> =
        UserDao.action { get(id.asUuid) }?.toUser().let {
            if (it == null) Result.failure(Throwable())
            else Result.success(it)
        }

    override suspend fun create(new: User): Result<TokenUser> {
        return UserDao.action {
            new(new.uuid.asUuid) {
                email = new.email!!
                photoUrl = new.photoUrl
                username = new.username!!
                password = new.password!!
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated
            }
        }?.toUser().let {
            if (it == null) Result.failure(Throwable())
            else Result.success(
                TokenUser(
                    id = it.id,
                    uuid = it.uuid,
                    email = it.email,
                    photoUrl = it.photoUrl,
                    username = it.username,
                    authToken = it.asToken(jwtProvider) ?: "",
                    refreshToken = "",
                    expiresIn = 0L
                )
            )
        }
    }

    override suspend fun login(email: String, password: String): Result<TokenUser> {
        return UserDao.action {
            find {
                (UserTable.email eq email) and (UserTable.password eq password)
            }.firstOrNull()
                ?.toUser()
        }.let {
            if (it == null) Result.failure(Throwable())
            else Result.success(
                TokenUser(
                    id = it.id,
                    uuid = it.uuid,
                    email = it.email,
                    photoUrl = it.photoUrl,
                    username = it.username,
                    authToken = it.asToken(jwtProvider) ?: "",
                    refreshToken = "",
                    expiresIn = 0L
                )
            )
        }
    }

    override suspend fun update(updated: TokenUser): Result<TokenUser> {
        val foundUser = UserDao.action { get(updated.uuid.asUuid) }

        if (foundUser == null) {
            Log.error("Could not find user matching the uuid ${updated.uuid}")
            return Result.failure(Throwable()) // change
        }

        val updatedUsername = updated.username
        val updatedPassword = updated.password

        if (updatedUsername == null || updatedPassword == null) {
            Log.error("Trying to update a user with no username or password, this is not valid")
            return Result.failure(Throwable()) // change
        }

        UserDao.action {
            foundUser.apply {
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

        return account(foundUser.id.value.toString()).map {
            TokenUser(
                id = it.id,
                uuid = it.uuid,
                email = it.email,
                photoUrl = it.photoUrl,
                username = it.username,
                password = it.password,
                authToken = it.asToken(jwtProvider) ?: "",
                refreshToken = "",
                expiresIn = 0L,
                history = it.history,
                dateCreated = it.dateCreated,
                dateUpdated = it.dateUpdated
            )
        }
    }

    override suspend fun delete(id: String): Boolean {
        val foundUser = UserDao.action { get(id.asUuid) }

        if (foundUser == null) {
            Log.error("Could not find a user matching the id $id to delete")
            return false
        }

        Log.info("Deleting user")
        UserDao.action { foundUser.delete() }
        return true
    }

    override suspend fun info(id: String): Result<User> {
        return account(id)
    }
}