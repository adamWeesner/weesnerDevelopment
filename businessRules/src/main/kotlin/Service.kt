import generics.IdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.GenericItem

interface Service<I : GenericItem> {
    val table: IdTable

    suspend fun add(item: I): Int?
    suspend fun getAll(): List<I>?
    suspend fun getAll(op: SqlExpressionBuilder.() -> Op<Boolean>): Query?
    suspend fun get(op: SqlExpressionBuilder.() -> Op<Boolean>): I?
    suspend fun update(item: I, op: SqlExpressionBuilder.() -> Op<Boolean>): Int?
    suspend fun delete(item: I, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean
    suspend fun toItem(row: ResultRow): I
    fun UpdateBuilder<Int>.toRow(item: I)
}
