package income

import HistoryTypes
import auth.UsersService
import colors.ColorsService
import generics.GenericService
import history.HistoryService
import incomeOccurrences.IncomeOccurrencesService
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Color
import shared.billMan.Income

class IncomeService(
    private val usersService: UsersService,
    private val colorsService: ColorsService,
    private val occurrencesService: IncomeOccurrencesService,
    private val historyService: HistoryService
) : GenericService<Income, IncomeTable>(
    IncomeTable
) {
    override suspend fun delete(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        occurrencesService.deleteForIncome(id)
        historyService.run {
            getFor(HistoryTypes.Bill.name, id).mapNotNull { it.id }.forEach { delete(it) { table.id eq it } }
        }
        return super.delete(id, op)
    }

    override suspend fun to(row: ResultRow) = Income(
        id = row[IncomeTable.id],
        owner = usersService.getUserByUuidRedacted(row[IncomeTable.ownerId])
            ?: throw IllegalArgumentException("No user found for income."),
        name = row[IncomeTable.name],
        amount = row[IncomeTable.amount],
        varyingAmount = row[IncomeTable.varyingAmount],
        color = Color(red = 255, green = 255, blue = 255, alpha = 0, dateCreated = 0, dateUpdated = 0),
        history = historyService.getFor(HistoryTypes.Bill.name, row[IncomeTable.id]),
        dateCreated = row[IncomeTable.dateCreated],
        dateUpdated = row[IncomeTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Income) {
        this[IncomeTable.ownerId] = item.owner.uuid!!
        this[IncomeTable.name] = item.name
        this[IncomeTable.amount] = item.amount
        this[IncomeTable.varyingAmount] = item.varyingAmount
    }
}
