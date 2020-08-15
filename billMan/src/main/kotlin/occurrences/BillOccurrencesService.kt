package occurrences

import BaseService
import auth.UsersService
import diff
import history.HistoryService
import isNotValidId
import occurrencesSharedUsers.OccurrenceSharedUsers
import occurrencesSharedUsers.OccurrenceSharedUsersService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import payments.PaymentsService
import shared.auth.User
import shared.base.History
import shared.base.InvalidAttributeException
import shared.billMan.Occurrence
import shared.billMan.Payment

class BillOccurrencesService(
    internal val usersService: UsersService,
    private val paymentsService: PaymentsService,
    private val sharedUsersService: OccurrenceSharedUsersService,
    private val historyService: HistoryService
) : BaseService<BillOccurrencesTable, Occurrence>(
    BillOccurrencesTable
) {
    private val BillOccurrencesTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        })

    override suspend fun getAll() = tryCall {
        table.connections.selectAll().mapNotNull {
            toItem(it)
        }
    }

    override suspend fun get(op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        table.connections.select {
            op()
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    override suspend fun update(item: Occurrence, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            op()
        } ?: return null

        var paymentsAmount = 0.0
        item.payments?.forEach {
            paymentsAmount += it.amount.toDouble()
        }

        if (item.amountLeft.toDouble() < paymentsAmount)
            return null

        val history: List<History>

        oldItem.diff(item).updates(item.owner).also {
            history = it
        }.forEach {
            val added = historyService.add(it)
            if (added.isNotValidId)
                return added
        }

        if (history.any {
                it.field.contains("sharedUser".toRegex())
            }) {
            val deleted = sharedUsersService.deleteForOccurrence(item.id!!)
            if (deleted.isNotValidId)
                return deleted

            item.sharedUsers?.forEach {
                val addedSharedUser = sharedUsersService.add(
                    OccurrenceSharedUsers(
                        occurrenceId = item.id!!,
                        userId = it.uuid!!
                    )
                )

                if (addedSharedUser.isNotValidId)
                    return addedSharedUser
            }
        }

        return super.update(item, op)
    }

    suspend fun Int.payFor(payment: Double, user: User) = paymentsService.addForOccurrence(
        this,
        Payment(owner = user, amount = payment.toString())
    )

    override suspend fun toItem(row: ResultRow) = Occurrence(
        id = row[table.id],
        owner = usersService.toItemRedacted(row),
        amount = row[table.amount],
        sharedUsers = sharedUsersService.getByOccurrence(row[table.id]),
        itemId = row[table.itemId].toString(),
        dueDate = row[table.dueDate],
        amountLeft = row[table.amountLeft],
        payments = paymentsService.getForOccurrence(row[table.id]),
        every = row[table.every],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    ).let {
        val history = historyService.getFor<Occurrence>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: Occurrence) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.amount] = item.amount
        this[table.itemId] = item.itemId.toInt()
        this[table.dueDate] = item.dueDate
        this[table.amountLeft] = item.amountLeft
        this[table.every] = item.every
    }
}
