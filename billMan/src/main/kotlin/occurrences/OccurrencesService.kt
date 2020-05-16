package occurrences

import HistoryTypes
import auth.UsersService
import generics.GenericService
import history.HistoryService
import occurrencesSharedUsers.OccurrenceSharedUsersService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import payments.PaymentsService
import shared.billMan.Occurrence

class OccurrencesService(
    private val usersService: UsersService,
    private val paymentsService: PaymentsService,
    private val sharedUsersService: OccurrenceSharedUsersService,
    private val historyService: HistoryService
) : GenericService<Occurrence, OccurrencesTable>(
    OccurrencesTable
) {
    override suspend fun to(row: ResultRow) = Occurrence(
        id = row[OccurrencesTable.id],
        owner = usersService.getUserByUuid(row[OccurrencesTable.ownerId])
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
        this[OccurrencesTable.ownerId] = item.owner.uuid!!
        this[OccurrencesTable.amount] = item.amount
    }
}
