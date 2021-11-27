package com.weesnerdevelopment.billman.occurrences

import BaseService
import auth.UsersService
import com.weesnerdevelopment.billman.occurrencesSharedUsers.OccurrenceSharedUsers
import com.weesnerdevelopment.billman.occurrencesSharedUsers.OccurrenceSharedUsersService
import com.weesnerdevelopment.billman.payments.PaymentsService
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.billMan.Payment
import diff
import history.HistoryService
import isNotValidId
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillOccurrencesService(
    internal val usersService: UsersService,
    private val paymentsService: PaymentsService,
    private val sharedUsersService: OccurrenceSharedUsersService,
    private val historyService: HistoryService
) : BaseService<BillOccurrencesTable, BillOccurrence>(
    BillOccurrencesTable
) {
    override val BillOccurrencesTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        })

    override suspend fun update(item: BillOccurrence, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
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

    override suspend fun toItem(row: ResultRow) = BillOccurrence(
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
        val history = historyService.getFor<BillOccurrence>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: BillOccurrence) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.amount] = item.amount
        this[table.itemId] = item.itemId.toInt()
        this[table.dueDate] = item.dueDate
        this[table.amountLeft] = item.amountLeft
        this[table.every] = item.every
    }
}
