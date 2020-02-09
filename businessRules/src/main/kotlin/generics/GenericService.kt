package generics

import dbQuery
import model.ChangeType
import model.Notification
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

abstract class GenericService<O : GenericItem, T : IdTable>(open val table: T) {
    private val listeners = mutableMapOf<Int, suspend (Notification<O?>) -> Unit>()

    open fun addChangeListener(id: Int, listener: suspend (Notification<O?>) -> Unit) {
        listeners[id] = listener
    }

    open fun removeChangeListener(id: Int) = listeners.remove(id)

    suspend fun onChange(type: ChangeType, id: Int, entity: O? = null) {
        listeners.values.forEach {
            it.invoke(Notification(type, id, entity))
        }
    }

    open suspend fun getAll() = dbQuery { table.selectAll().map { to(it) } }

    open suspend fun getSingle(id: Int) =
        dbQuery { table.select { (table.id eq id) }.mapNotNull { to(it) }.singleOrNull() }

    open suspend fun update(item: O): O? {
        val id = item.id

        return if (id == null) {
            add(item)
        } else {
            dbQuery {
                table.update({ table.id eq id }) {
                    it.assignValues(item)
                    it[dateUpdated] = System.currentTimeMillis()
                }
            }
            getSingle(id).also {
                onChange(ChangeType.Update, id, it)
            }
        }
    }

    open suspend fun add(item: O): O? {
        var key = 0

        dbQuery {
            key = (table.insert {
                it.assignValues(item)
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
            } get table.id)
        }
        return getSingle(key).also {
            onChange(ChangeType.Create, key, it)
        }
    }

    open suspend fun delete(id: Int) = dbQuery {
        table.deleteWhere { table.id eq id } > 0
    }.also {
        if (it) onChange(ChangeType.Delete, id)
    }

    abstract suspend fun to(row: ResultRow): O
    abstract fun UpdateBuilder<Int>.assignValues(item: O)
}