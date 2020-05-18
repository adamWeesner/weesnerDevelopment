package colors

import HistoryTypes
import dbQuery
import generics.GenericService
import history.HistoryService
import model.ChangeType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Color

class ColorsService(
    private val historyService: HistoryService
) : GenericService<Color, ColorsTable>(
    ColorsTable
) {
    suspend fun getByBill(id: Int) =
        dbQuery { table.select { (table.billId eq id) }.mapNotNull { to(it) } }.firstOrNull()
            ?: throw IllegalArgumentException("No color found for bill..")

    suspend fun deleteForBill(billId: Int) = dbQuery {
        table.select { (table.billId eq billId) }.mapNotNull { to(it).id }
    }.forEach { delete(it) { table.id eq it } }

    @Deprecated("Use add(billId, color) instead", ReplaceWith("add(bill.id, color)"), DeprecationLevel.ERROR)
    override suspend fun add(item: Color): Color? = null

    suspend fun add(bill: Int, item: Color): Color? {
        var key = 0

        dbQuery {
            key = table.insert {
                it[billId] = bill
                it.assignValues(item)
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
            } get table.id
        }
        return getSingle { table.id eq key }?.also {
            onChange(ChangeType.Create, key, it)
        }
    }

    override suspend fun delete(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        historyService.run {
            getFor(HistoryTypes.Bill.name, id).mapNotNull { it.id }.forEach { delete(it) { table.id eq it } }
        }
        return super.delete(id, op)
    }

    override suspend fun to(row: ResultRow) = Color(
        id = row[ColorsTable.id],
        red = row[ColorsTable.red],
        green = row[ColorsTable.green],
        blue = row[ColorsTable.blue],
        alpha = row[ColorsTable.alpha],
        history = historyService.getFor(HistoryTypes.Color.name, row[ColorsTable.id]),
        dateCreated = row[ColorsTable.dateCreated],
        dateUpdated = row[ColorsTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Color) {
        this[ColorsTable.red] = item.red
        this[ColorsTable.green] = item.green
        this[ColorsTable.blue] = item.blue
        this[ColorsTable.alpha] = item.alpha
    }
}
