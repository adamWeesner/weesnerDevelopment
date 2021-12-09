package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.asUuid
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.billman.bill.BillDao
import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentDao
import com.weesnerdevelopment.billman.income.IncomeDao
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.currentTimeMillis
import diff
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

object BillOccurrenceRepositoryImpl : BillOccurrenceRepository {
    override fun getAll(user: UUID): List<BillOccurrence> {
        return BillOccurrenceDao.action {
            runCatching {
                find {
                    (BillOccurrenceTable.owner eq user)
                }.toCategories()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun getAllFor(user: UUID, billId: UUID): List<BillOccurrence> {
        return BillOccurrenceDao.action {
            runCatching {
                find {
                    (BillOccurrenceTable.owner eq user) and (BillOccurrenceTable.bill eq billId)
                }.toCategories()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun get(user: UUID, id: UUID): BillOccurrence? {
        return BillOccurrenceDao.action { getSingle(user, id)?.toBillOccurrence() }
    }

    override fun add(new: BillOccurrence): BillOccurrence? {
        val billSharedUsers = new.sharedUsers?.map { UserDao.action { get(UUID.fromString(it.uuid)) } }

        return BillOccurrenceDao.action {
            runCatching {
                new(new.uuid.asUuid) {
                    owner = UserDao.action { get(UUID.fromString(new.owner.uuid)) }
                    bill = BillDao.action { get(UUID.fromString(new.itemId)) }
                    dueDate = new.dueDate
                    if (billSharedUsers != null)
                        sharedUsers = SizedCollection(billSharedUsers)
                    amount = new.amount
                    amountLeft = new.amountLeft
                    every = new.every
                    dateCreated = new.dateCreated
                    dateUpdated = new.dateUpdated
                }.toBillOccurrence()
            }.getOrNull()
        }
    }

    override fun update(updated: BillOccurrence): BillOccurrence? {
        val foundBillOccurrence = BillOccurrenceDao.action { get(UUID.fromString(updated.uuid)) }

        // update shared users
        foundBillOccurrence.sharedUsers.forEach {
            BillOccurrenceSharedUsersTable.deleteWhere {
                (BillOccurrenceSharedUsersTable.occurrence eq foundBillOccurrence.id) and (BillOccurrenceSharedUsersTable.user eq it.uuid)
            }
        }

        val newSharedUsers = updated.sharedUsers?.map { UserDao.action { get(UUID.fromString(it.uuid)) } }
        if (newSharedUsers != null)
            foundBillOccurrence.sharedUsers = SizedCollection(newSharedUsers)

        // update history
        foundBillOccurrence.toBillOccurrence().diff(updated).updates(foundBillOccurrence.owner.toUser()).forEach {
            HistoryDao.action {
                new {
                    field = it.field
                    oldValue = it.oldValue
                    newValue = it.newValue
                    updatedBy = foundBillOccurrence.owner.uuid
                    dateCreated = it.dateCreated
                    dateUpdated = it.dateUpdated
                }
            }
        }

        foundBillOccurrence.apply {
            dueDate = updated.dueDate
            amount = updated.amount
            amountLeft = updated.amountLeft
            every = updated.every
            owner = updated.owner.let { UserDao.action { get(UUID.fromString(it.uuid)) } }
            dateUpdated = currentTimeMillis()
        }

        return get(foundBillOccurrence.owner.uuid.value, foundBillOccurrence.uuid.value)
    }

    override fun pay(id: UUID, payment: String): BillOccurrence? {
        val foundBillOccurrence = BillOccurrenceDao.action { get(id) }

        // todo what should we do if they change their amount to be lower than the amountLeft?
        if (foundBillOccurrence.amountLeft.toDouble() < payment.toDouble())
            return null

        val newPayment = PaymentDao.action {
            new(UUID.randomUUID()) {
                owner = foundBillOccurrence.owner
                amount = payment
            }
        }

        foundBillOccurrence.apply {
            payments = SizedCollection(foundBillOccurrence.payments + newPayment)
        }

        return get(foundBillOccurrence.owner.uuid.value, foundBillOccurrence.uuid.value)
    }

    override fun delete(user: UUID, id: UUID): Boolean {
        val foundOccurrence = IncomeDao.action { get(id) }

        if (foundOccurrence == null)
            return false

        foundOccurrence.delete()
        return true
    }

    private fun getSingle(user: UUID, id: UUID): BillOccurrenceDao? = BillOccurrenceDao.action {
        find {
            (BillOccurrenceTable.owner eq user) and (BillOccurrenceTable.id eq id)
        }.firstOrNull()
    }
}