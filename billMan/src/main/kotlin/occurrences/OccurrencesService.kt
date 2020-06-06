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

class OccurrencesService(
    private val usersService: UsersService,
    private val paymentsService: PaymentsService,
    private val sharedUsersService: OccurrenceSharedUsersService,
    private val historyService: HistoryService
) : GenericService<Occurrence, OccurrencesTable>(
    OccurrencesTable
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
        id = row[OccurrencesTable.id],
        owner = usersService.getUserByUuidRedacted(row[OccurrencesTable.ownerId])
            ?: throw IllegalArgumentException("No user found for occurrence."),
        amount = row[OccurrencesTable.amount],
        sharedUsers = sharedUsersService.getByOccurrence(row[OccurrencesTable.id]),
        itemId = row[OccurrencesTable.itemId].toString(),
        dueDate = row[OccurrencesTable.dueDate],
        amountLeft = row[OccurrencesTable.amountLeft],
        payments = paymentsService.getForOccurrence(row[OccurrencesTable.id]),
        every = row[OccurrencesTable.every],
        history = historyService.getFor(HistoryTypes.Color.name, row[OccurrencesTable.id]),
        dateCreated = row[OccurrencesTable.dateCreated],
        dateUpdated = row[OccurrencesTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Occurrence) {
        this[OccurrencesTable.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[OccurrencesTable.amount] = item.amount
        this[OccurrencesTable.itemId] = item.itemId.toInt()
        this[OccurrencesTable.dueDate] = item.dueDate
        this[OccurrencesTable.amountLeft] = item.amountLeft
        this[OccurrencesTable.every] = item.every
    }
}
