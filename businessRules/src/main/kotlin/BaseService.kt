import com.weesnerdevelopment.businessRules.dbQuery
import com.weesnerdevelopment.shared.base.GenericItem
import generics.IdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*

abstract class BaseService<T : IdTable, I : GenericItem>(
    override val table: T
) : Service<I> {
    abstract val T.connections: Join?

    suspend fun <T : Any> tryCall(block: suspend () -> T?) = try {
        dbQuery(block)
    } catch (e: Exception) {
        null
    }

    override suspend fun add(item: I) = tryCall {
        if (item.id != null) return@tryCall -1

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
        (table.connections ?: table).selectAll().mapNotNull {
            toItem(it)
        }
    }

    override suspend fun getAll(op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        (table.connections ?: table).select {
            op()
        }
    }

    override suspend fun get(op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        (table.connections ?: table).select {
            op()
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    override suspend fun update(item: I, op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        if (item.id == null) return@tryCall -1

        if (get { table.id eq item.id!! } == null) return@tryCall null

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
