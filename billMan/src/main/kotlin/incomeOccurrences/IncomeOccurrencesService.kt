package incomeOccurrences

import HistoryTypes
import auth.UsersService
import dbQuery
import generics.GenericService
import history.HistoryService
import incomeOccurrences.IncomeOccurrencesTable.itemId
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException
import shared.billMan.Occurrence
import shared.billMan.responses.OccurrencesResponse

class IncomeOccurrencesService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericService<Occurrence, IncomeOccurrencesTable>(
    IncomeOccurrencesTable
) {
    suspend fun getByIncome(incomeId: Int) =
        dbQuery { table.select { (itemId eq incomeId) }.mapNotNull { to(it) } }.run(::OccurrencesResponse)

    suspend fun deleteForIncome(incomeId: Int) = dbQuery {
        table.select { (itemId eq incomeId) }.mapNotNull { to(it).id }
    }.forEach {
        historyService.apply { getIdsFor(HistoryTypes.Occurrence.name, it).forEach { delete(it) { table.id eq it } } }
        delete(it) { table.id eq it }
    }

    override suspend fun to(row: ResultRow) = Occurrence(
        id = row[IncomeOccurrencesTable.id],
        owner = usersService.getUserByUuidRedacted(row[IncomeOccurrencesTable.ownerId])
            ?: throw IllegalArgumentException("No user found for occurrence."),
        amount = row[IncomeOccurrencesTable.amount],
        itemId = row[IncomeOccurrencesTable.itemId].toString(),
        dueDate = row[IncomeOccurrencesTable.dueDate],
        every = row[IncomeOccurrencesTable.every],
        history = historyService.getFor(HistoryTypes.Color.name, row[IncomeOccurrencesTable.id]),
        dateCreated = row[IncomeOccurrencesTable.dateCreated],
        dateUpdated = row[IncomeOccurrencesTable.dateUpdated],
        amountLeft = "0"
    )

    override fun UpdateBuilder<Int>.assignValues(item: Occurrence) {
        this[IncomeOccurrencesTable.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[IncomeOccurrencesTable.amount] = item.amount
        this[IncomeOccurrencesTable.itemId] = item.itemId.toInt()
        this[IncomeOccurrencesTable.dueDate] = item.dueDate
        this[IncomeOccurrencesTable.every] = item.every
    }
}
