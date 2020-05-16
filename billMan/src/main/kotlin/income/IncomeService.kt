package income

import HistoryTypes
import auth.UsersService
import colors.ColorsService
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Income

class IncomeService(
    private val usersService: UsersService,
    private val colorsService: ColorsService,
    private val historyService: HistoryService
) : GenericService<Income, IncomeTable>(
    IncomeTable
) {
    override suspend fun to(row: ResultRow) = Income(
        id = row[IncomeTable.id],
        owner = usersService.getUserByUuid(row[IncomeTable.ownerId])
            ?: throw IllegalArgumentException("No user found for income."),
        name = row[IncomeTable.name],
        amount = row[IncomeTable.amount],
        varyingAmount = row[IncomeTable.varyingAmount],
        color = colorsService.getByBill(row[IncomeTable.id]),
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
