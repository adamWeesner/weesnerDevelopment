package com.weesnerdevelopment.businessRules

import BaseService
import auth.CustomPrincipal
import com.weesnerdevelopment.shared.fromJson
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kimchi.Kimchi
import kimchi.logger.KimchiLogger
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

val String?.asUuid
    get() = runCatching { UUID.fromString(this) }.getOrNull() ?: UUID.randomUUID()

val Log: KimchiLogger = Kimchi

/**
 * Helper function to try a database transaction or return null, if the transaction fails
 */
fun <T : UUIDEntity, R> T.tryTransaction(event: T.() -> R) =
    runCatching {
        transaction { event() }
    }.getOrElse {
        Log.error("Failed to complete database transaction from entity", it)
        null
    }

/**
 * Helper function to try a database transaction or return null, if the transaction fails
 */
fun <T : UUIDEntityClass<U>, U, R> T.tryTransaction(event: T.() -> R) =
    runCatching {
        transaction { event() }
    }.getOrElse {
        Log.error("Failed to complete database transaction", it)
        null
    }

/**
 * Helper function to try a database transaction or return null, if the transaction fails
 */
fun <T : Table, R> T.tryTransaction(event: T.() -> R) =
    runCatching {
        transaction { event() }
    }.getOrElse {
        Log.error("Failed to complete database transaction from table", it)
        null
    }

/**
 * Gets the Authentication principal from the [ApplicationCall].
 */
fun ApplicationCall.loggedUserData() = authentication.principal<CustomPrincipal>()

inline fun <reified T> String?.parse(): T =
    this?.fromJson<T>() ?: throw Throwable("failed to parse $this to ${T::class}.")

/**
 * Checks whether the Int is a valid successful id, added to the database.
 */
val Int?.isNotValidId get() = this == null || this == -1

/**
 * Helper function to be able to do something like `service.get { id eq item.id }`
 * instead of `service.get { service.table.id eq item.id }`.
 */
suspend inline fun <reified O, reified T, reified R : BaseService<O, T>> R.get(crossinline query: O.(SqlExpressionBuilder) -> Op<Boolean>) {
    this.get { this@get.table.query(this) }
}

/**
 * Helper function to query [T] in the table.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }
