package colors

import BaseService
import com.weesnerdevelopment.shared.billMan.Color
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class ColorsService : BaseService<ColorsTable, Color>(
    ColorsTable
) {
    override val ColorsTable.connections: Join?
        get() = null

    @Deprecated("Use add(billId, color) instead", ReplaceWith("add(bill.id, color)"), DeprecationLevel.ERROR)
    override suspend fun add(item: Color): Int? = null

    suspend fun add(bill: Int, item: Color) = tryCall {
        table.insert {
            it[billId] = bill
            it.toRow(item)
            it[dateCreated] = System.currentTimeMillis()
            it[dateUpdated] = System.currentTimeMillis()
        } get table.id
    }

    override suspend fun toItem(row: ResultRow) = Color(
        id = row[table.id],
        red = row[table.red],
        green = row[table.green],
        blue = row[table.blue],
        alpha = row[table.alpha],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Color) {
        this[table.red] = item.red
        this[table.green] = item.green
        this[table.blue] = item.blue
        this[table.alpha] = item.alpha
    }
}
