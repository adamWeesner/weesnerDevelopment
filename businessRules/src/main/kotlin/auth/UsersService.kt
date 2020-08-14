package auth

import BaseService
import diff
import history.HistoryService
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import parse
import shared.auth.HashedUser
import shared.auth.User
import shared.base.InvalidAttributeException

class UsersService(
    private val historyService: HistoryService
) : BaseService<UsersTable, User>(
    UsersTable
) {
    suspend fun getUserFromHash(hashedUser: HashedUser) = tryCall {
        table.select {
            (table.username eq hashedUser.username) and (table.password eq hashedUser.password)
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    suspend fun getUserByUuid(uuid: String?) = tryCall {
        if (uuid == null)
            throw InvalidAttributeException("Uuid")

        table.select {
            table.uuid eq uuid
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    suspend fun getUserByUuidRedacted(uuid: String) = getUserByUuid(uuid = uuid)?.redacted()?.parse<User>()

    @Deprecated("", ReplaceWith("addUser(user: User): User?", "user"))
    override suspend fun add(item: User): Int? = throw IllegalArgumentException("Should be using `add(User): User?`")

    suspend fun addUser(item: User): User? {
        val uuid = tryCall {
            table.insert {
                it.toRow(item)
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
            } get table.uuid
        }

        return when {
            uuid != null -> getUserByUuid(uuid)
            else -> throw Throwable(HttpStatusCode.NotFound.description)
        }
    }

    override suspend fun update(item: User, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldUserInfo = getUserByUuid(item.uuid)

        oldUserInfo?.diff(item)?.updates(item)?.forEach {
            val addedHistory = historyService.add(it)

            if (addedHistory == -1 || addedHistory == null)
                return addedHistory
        }

        return super.update(item, op)
    }

    override suspend fun delete(item: User, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        item.history?.forEach {
            historyService.delete(it) {
                historyService.table.id eq it.id!!
            }
        }

        return super.delete(item, op)
    }


    override suspend fun toItem(row: ResultRow): User = User(
        id = row[table.id],
        uuid = row[table.uuid],
        name = row[table.name],
        email = row[table.email],
        photoUrl = row[table.photoUrl],
        username = row[table.username],
        password = row[table.password],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    ).let {
        row[table.history]?.let { _ ->
            it.copy(history = historyService.getFor<User>(row[table.id], it))
        } ?: it
    }

    suspend fun toItemRedacted(row: ResultRow) = toItem(row).redacted().parse<User>()

    override fun UpdateBuilder<Int>.toRow(item: User) {
        this[table.uuid] = item.uuid ?: throw InvalidAttributeException("uuid")
        this[table.name] = item.name ?: throw InvalidAttributeException("name")
        this[table.email] = item.email ?: throw InvalidAttributeException("email")
        this[table.photoUrl] = item.photoUrl
        this[table.username] = item.username ?: throw InvalidAttributeException("username")
        this[table.password] = item.password ?: throw InvalidAttributeException("password")
    }
}
