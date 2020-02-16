package auth

import dbQuery
import generics.GenericService
import generics.InvalidAttributeException
import io.ktor.http.HttpStatusCode
import model.ChangeType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class UsersService : GenericService<User, UsersTable>(
    UsersTable
) {
    suspend fun getUserFromHash(hashedUser: HashedUser) = dbQuery {
        table.select { (table.username eq hashedUser.username) and (table.password eq hashedUser.password) }
            .mapNotNull { to(it) }.singleOrNull()
    }

    suspend fun getUserByUuid(uuid: String) =
        dbQuery { table.select { (table.uuid eq uuid) }.mapNotNull { to(it) }.singleOrNull() }

    override suspend fun add(item: User): User? {
        var key = 0

        val savedUser = item.asHashed()?.let { getUserFromHash(it) } ?: item.uuid?.let { getUserByUuid(it) }

        if (savedUser != null) {
            onChange(ChangeType.Error, key)
            return null
        }

        dbQuery {
            key = (table.insert {
                it.assignValues(item)
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
            } get table.id)
        }

        return when {
            item.username != null && item.password != null -> item.asHashed()?.let { getUserFromHash(it) }.also {
                if (it == null) onChange(ChangeType.Error, key)
                else onChange(ChangeType.Create, key, it)
            }
            item.uuid != null -> getUserByUuid(item.uuid).also {
                if (it == null) onChange(ChangeType.Error, key)
                else onChange(ChangeType.Create, key, it)
            }
            else -> throw Throwable(HttpStatusCode.NotFound.description)
        }
    }

    override suspend fun to(row: ResultRow) = User(
        id = row[UsersTable.id],
        uuid = row[UsersTable.uuid],
        name = row[UsersTable.name],
        email = row[UsersTable.email],
        photoUrl = row[UsersTable.photoUrl],
        username = row[UsersTable.username],
        password = row[UsersTable.password],
        dateCreated = row[UsersTable.dateCreated],
        dateUpdated = row[UsersTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: User) {
        item.uuid?.let { this[UsersTable.uuid] = it }
        this[UsersTable.name] = item.name ?: throw InvalidAttributeException("name")
        this[UsersTable.email] = item.email ?: throw InvalidAttributeException("email")
        item.photoUrl?.let { this[UsersTable.photoUrl] = it }
        item.username?.let { this[UsersTable.username] = it }
        item.password?.let { this[UsersTable.password] = it }
    }
}