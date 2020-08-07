package occurrences

import HistoryTypes
import auth.UsersService
import dbQuery
import generics.GenericService
import history.HistoryService
import occurrencesSharedUsers.OccurrenceSharedUsersService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import payments.PaymentsService
import shared.base.InvalidAttributeException
import shared.billMan.Occurrence
import shared.billMan.responses.OccurrencesResponse

class BillOccurrencesService(
    private val usersService: UsersService,
    private val paymentsService: PaymentsService,
    private val sharedUsersService: OccurrenceSharedUsersService,
    private val historyService: HistoryService
) : GenericService<Occurrence, BillOccurrencesTable>(
    BillOccurrencesTable
) {
    suspend fun getByBill(billId: Int) =
        dbQuery { table.select { (table.itemId eq billId) }.mapNotNull { to(it) } }.run(::OccurrencesResponse)

    suspend fun deleteForBill(billId: Int) = dbQuery {
        table.select { (table.itemId eq billId) }.mapNotNull { to(it).id }
    }.forEach {
        paymentsService.deleteForOccurrence(it)
        sharedUsersService.deleteForOccurrence(it)
        historyService.apply { getIdsFor(HistoryTypes.Occurrence.name, it).forEach { delete(it) { table.id eq it } } }
        delete(it) { table.id eq it }
    }

    override suspend fun to(row: ResultRow) = Occurrence(
        id = row[BillOccurrencesTable.id],
        owner = usersService.getUserByUuidRedacted(row[BillOccurrencesTable.ownerId])
            ?: throw IllegalArgumentException("No user found for occurrence."),
        amount = row[BillOccurrencesTable.amount],
        sharedUsers = sharedUsersService.getByOccurrence(row[BillOccurrencesTable.id]),
        itemId = row[BillOccurrencesTable.itemId].toString(),
        dueDate = row[BillOccurrencesTable.dueDate],
        amountLeft = row[BillOccurrencesTable.amountLeft],
        payments = paymentsService.getForOccurrence(row[BillOccurrencesTable.id]),
        every = row[BillOccurrencesTable.every],
        history = historyService.getFor(HistoryTypes.Color.name, row[BillOccurrencesTable.id]),
        dateCreated = row[BillOccurrencesTable.dateCreated],
        dateUpdated = row[BillOccurrencesTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Occurrence) {
        this[BillOccurrencesTable.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[BillOccurrencesTable.amount] = item.amount
        this[BillOccurrencesTable.itemId] = item.itemId.toInt()
        this[BillOccurrencesTable.dueDate] = item.dueDate
        this[BillOccurrencesTable.amountLeft] = item.amountLeft
        this[BillOccurrencesTable.every] = item.every
    }
}
