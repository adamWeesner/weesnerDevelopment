package auth

import BaseService
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import parse
import shared.auth.HashedUser
import shared.auth.User
import shared.base.InvalidAttributeException

class UsersService : BaseService<UsersTable, User>(
    UsersTable
) {
    suspend fun getUserFromHash(hashedUser: HashedUser) = tryCall {
        table.select {
            (table.username eq hashedUser.username) and (table.password eq hashedUser.password)
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    suspend fun getUserByUuid(uuid: String) = tryCall {
        table.select {
            table.uuid eq uuid
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    suspend fun getUserByUuidRedacted(uuid: String) =
        getUserByUuid(uuid)?.redacted()?.parse<User>()

    suspend fun addUser(item: User): User? {
        val savedUser = item.asHashed()?.let { getUserFromHash(it) } ?: item.uuid?.let { getUserByUuid(it) }

        if (savedUser != null) return null

        tryCall {
            table.insert {
                it.toRow(item)
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
            } get table.id
        }

        return when {
            item.username != null && item.password != null -> item.asHashed()?.let { getUserFromHash(it) }
            item.uuid != null -> getUserByUuid(item.uuid!!)
            else -> throw Throwable(HttpStatusCode.NotFound.description)
        }
    }

    @Deprecated("", ReplaceWith("addUser(user: User): User?", "user"))
    override suspend fun add(item: User): Int? = throw IllegalArgumentException("Should be using `add(User): User?`")

    override suspend fun toItem(row: ResultRow) = User(
        id = row[table.id],
        uuid = row[table.uuid],
        name = row[table.name],
        email = row[table.email],
        photoUrl = row[table.photoUrl],
        username = row[table.username],
        password = row[table.password],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: User) {
        item.uuid?.let { this[table.uuid] = it }
        this[table.name] = item.name ?: throw InvalidAttributeException("name")
        this[table.email] = item.email ?: throw InvalidAttributeException("email")
        item.photoUrl?.let { this[table.photoUrl] = it }
        item.username?.let { this[table.username] = it }
        item.password?.let { this[table.password] = it }
    }
}
