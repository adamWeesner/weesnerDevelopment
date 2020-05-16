package payments

import HistoryTypes
import auth.UsersService
import dbQuery
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Payment

class PaymentsService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericService<Payment, PaymentsTable>(
    PaymentsTable
) {
    suspend fun getForOccurrence(id: Int) =
        dbQuery { table.select { (PaymentsTable.occurrenceId eq id) }.mapNotNull { to(it) } }

    override suspend fun to(row: ResultRow) = Payment(
        id = row[PaymentsTable.id],
        owner = usersService.getUserByUuid(row[PaymentsTable.ownerId])
            ?: throw IllegalArgumentException("No user found for payment."),
        amount = row[PaymentsTable.amount],
        history = historyService.getFor(HistoryTypes.Color.name, row[PaymentsTable.id]),
        dateCreated = row[PaymentsTable.dateCreated],
        dateUpdated = row[PaymentsTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Payment) {
        this[PaymentsTable.ownerId] = item.owner.uuid!!
//        this[PaymentsTable.occurrenceId]
        this[PaymentsTable.amount] = item.amount
    }
}
