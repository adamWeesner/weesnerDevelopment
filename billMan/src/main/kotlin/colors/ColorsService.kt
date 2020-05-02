package colors

import HistoryTypes
import dbQuery
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Color

class ColorsService(
    private val historyService: HistoryService
) : GenericService<Color, ColorsTable>(
    ColorsTable
) {
    suspend fun getByBill(id: Int) =
        dbQuery { table.select { (table.billId eq id) }.mapNotNull { to(it) } }.first()

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
