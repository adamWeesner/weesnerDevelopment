package generics

import com.weesnerdevelopment.shared.base.GenericItem
import dbQuery
import kimchi.Kimchi
import model.ChangeType
import model.Notification
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

/**
 * The service for the given item type [O] and table [T].
 */
abstract class GenericService<O : GenericItem, T : IdTable>(open val table: T) {
    private val listeners = mutableMapOf<Int, suspend (Notification<O?>) -> Unit>()

    /**
     * Adds a notification listener for when the given [id] changes.
     */
    open fun addChangeListener(id: Int, listener: suspend (Notification<O?>) -> Unit) {
        listeners[id] = listener
    }

    /**
     * Removes the notification listener for the given [id].
     */

    open fun removeChangeListener(id: Int) = listeners.remove(id)

    /**
     * Triggered when a change to the [id] happens. This updated the [Notification] with the [type] of change and the
     * [entity] if there is one.
     */
    suspend fun onChange(type: ChangeType, id: Int, entity: O? = null) {
        listeners.values.forEach {
            it.invoke(Notification(type, id, entity))
        }
    }

    /**
     * Get all of the given [O] in the table.
     */
    open suspend fun getAll() = dbQuery {
        try {
            table.selectAll().map { to(it) }
        } catch (e: Throwable) {
            Kimchi.debug("getting all items threw $e")
            emptyList<O>()
        }
    }

    /**
     * Gets the single item, or null, that matches the [op] which is the table qualifier to determine what will be retrieved from
     * the table; sample:
     *
     * `table.id eq [O].id`
     */
    open suspend fun getSingle(op: SqlExpressionBuilder.() -> Op<Boolean>) =
        dbQuery {
            try {
                table.select { op() }.mapNotNull { to(it) }.singleOrNull()
            } catch (e: Throwable) {
                Kimchi.debug("getting single item threw $e")
                null
            }
        }

    /**
     * Updates the [item] that matches the [op] which is the table qualifier to determine what will be retrieved from
     * the table; sample:
     *
     * `table.id eq [item].id`
     *
     * If the [item] has a null id then a new item is added to the database instead.
     */
    open suspend fun update(item: O, op: SqlExpressionBuilder.() -> Op<Boolean>): O? {
        val id = item.id

        return if (id == null) {
            add(item)
        } else {
            dbQuery {
                try {
                    table.update({ op() }) {
                        it.assignValues(item)
                        it[dateUpdated] = System.currentTimeMillis()
                    }
                } catch (e: Throwable) {
                    Kimchi.debug("updating item threw $e")
                    null
                }
            }
            getSingle { op() }.also {
                onChange(ChangeType.Update, id, it)
            }
        }
    }

    /**
     * Adds the [item] to the database and returns the newly added item.
     */
    open suspend fun add(item: O): O? {
        var key = 0

        dbQuery {
            try {
                key = table.insert {
                    it.assignValues(item)
                    it[dateCreated] = System.currentTimeMillis()
                    it[dateUpdated] = System.currentTimeMillis()
                } get table.id
            } catch (e: Throwable) {
                Kimchi.debug("adding item threw $e")
            }
        }
        return getSingle { table.id eq key }?.also {
            onChange(ChangeType.Create, key, it)
        }
    }

    /**
     * Deletes the item that matches the [op] which is the table qualifier to determine what will be retrieved from
     * the table; sample:
     *
     * `table.id eq [O].id`
     */
    open suspend fun delete(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>) = dbQuery {
        try {
            table.deleteWhere { op() } > 0
        } catch (e: Throwable) {
            Kimchi.debug("deleting item threw $e")
            false
        }
    }.also {
        if (it) onChange(ChangeType.Delete, id)
    }

    /**
     * Converts the [ResultRow] from the table to a usable type [O].
     */
    abstract suspend fun to(row: ResultRow): O

    /**
     * Assign the values of [item] to the [UpdateBuilder] to be able to do something in the table with them.
     */
    abstract fun UpdateBuilder<Int>.assignValues(item: O)
}