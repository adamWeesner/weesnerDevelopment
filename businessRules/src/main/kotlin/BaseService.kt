import generics.IdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import shared.base.GenericItem

abstract class BaseService<T : IdTable, I : GenericItem>(
    override val table: T
) : Service<I> {
    suspend fun <T : Any> tryCall(block: suspend () -> T?) = try {
        dbQuery(block)
    } catch (e: Exception) {
        null
    }

    override suspend fun add(item: I) =
        tryCall {
            try {
                table.insert {
                    it.toRow(item)
                    it[dateCreated] = System.currentTimeMillis()
                    it[dateUpdated] = System.currentTimeMillis()
                } get table.id
            } catch (e: ExposedSQLException) {
                -1
            }
        }

    override suspend fun getAll() = tryCall {
        table.selectAll().mapNotNull {
            toItem(it)
        }
    }

    override suspend fun get(op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        table.select {
            op()
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    override suspend fun update(item: I, op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        if (item.id == null) return@tryCall -1
        table.update({
            op()
        }) {
            it.toRow(item)
            it[dateUpdated] = System.currentTimeMillis()
        }
    }

    override suspend fun delete(item: I, op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        table.deleteWhere {
            op()
        } > 0
    } ?: false
}
