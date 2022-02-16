package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.billman.bill.BillDao
import com.weesnerdevelopment.billman.bill.BillSharedUsersDao
import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentDao
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object BillOccurrenceRepositoryImpl : BillOccurrenceRepository {
    override fun getAll(user: String): List<BillOccurrence> = BillOccurrenceDao.action {
        Log.info("Attempting to get all of user $user bill occurrences")
        runCatching {
            find {
                (BillOccurrenceTable.owner eq user)
            }.toBillOccurrences()
        }.getOrElse {
            Log.error("Failed to get bill occurrences for user $user", it)
            null
        } ?: emptyList()
    }

    override fun getAllFor(user: String, billId: String): List<BillOccurrence> = BillOccurrenceDao.action {
        Log.info("Attempting to get all of user $user bill occurrences for bill $billId")
        runCatching {
            find {
                (BillOccurrenceTable.owner eq user) and (BillOccurrenceTable.bill eq billId.asUuid)
            }.toBillOccurrences()
        }.getOrElse {
            Log.error("Failed to get bill occurrences for user $user for bill $billId", it)
            null
        } ?: emptyList()
    }

    override fun get(user: String, id: String): BillOccurrence? {
        return BillOccurrenceDao.action { getSingle(user, id)?.toBillOccurrence() }
    }

    override fun add(new: BillOccurrence): BillOccurrence? = runCatching {
        val retrievedBill = BillDao.action { get(UUID.fromString(new.itemId)) }

        Log.info("Adding new bill occurrence")
        val newOccurrence = BillOccurrenceDao.action {
            new(new.uuid.asUuid) {
                owner = new.owner
                bill = retrievedBill
                dueDate = new.dueDate
                amount = new.amount
                amountLeft = new.amountLeft
                every = new.every
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated
            }
        }

        Log.info("Linking shared users for added bill occurrence")
        new.sharedUsers?.forEach {
            BillOccurrenceSharedUsersDao.action {
                new(UUID.randomUUID()) {
                    occurrence = newOccurrence.id
                    user = it
                }
            }
        }


        return BillOccurrenceDao.action { newOccurrence.toBillOccurrence() }
    }.getOrElse {
        Log.error("Failed to add new bill occurrence", it)
        null
    }

    override fun update(updated: BillOccurrence): BillOccurrence? {
        val occurrenceId = updated.uuid
        if (occurrenceId == null) {
            Log.error("Attempted to update a bill occurrence without a valid uuid")
            return null
        }

        val foundBillOccurrence = getSingle(updated.owner, occurrenceId)
        if (foundBillOccurrence == null) {
            Log.error("Could not find bill occurrence matching the owner and uuid")
            return null
        }
        val foundBillOccurrenceAsOccurrence = BillOccurrenceDao.action { foundBillOccurrence?.toBillOccurrence() }

        if (foundBillOccurrenceAsOccurrence?.sharedUsers != updated.sharedUsers) {
            Log.info("Removing all bill occurrence shared users")
            // update shared users
            transaction {
                BillOccurrenceSharedUsersTable.deleteWhere {
                    (BillOccurrenceSharedUsersTable.occurrence eq foundBillOccurrence.id)
                }
            }

            Log.info("Linking shared users for updated bill occurrence")
            updated.sharedUsers?.forEach {
                BillSharedUsersDao.action {
                    new(it.asUuid) {
                        bill = foundBillOccurrence.id
                        user = it
                    }
                }
            }
        } else {
            Log.info("Bill occurrence shared users matched, skipping update")
        }

        // update history
//        foundBillOccurrence.toBillOccurrence().diff(updated).updates(foundBillOccurrence.owner.toUser()).forEach {
//            HistoryDao.action {
//                new {
//                    field = it.field
//                    oldValue = it.oldValue
//                    newValue = it.newValue
//                    updatedBy = foundBillOccurrence.owner.uuid
//                    dateCreated = it.dateCreated
//                    dateUpdated = it.dateUpdated
//                }
//            }
//        }

        Log.info("Updating bill occurrence")
        BillOccurrenceDao.action {
            foundBillOccurrence.apply {
                dueDate = updated.dueDate
                amount = updated.amount
                amountLeft = updated.amountLeft
                every = updated.every
                owner = updated.owner
                dateUpdated = currentTimeMillis()
            }
        }

        return get(foundBillOccurrence.owner, foundBillOccurrence.id.value.toString())
    }

    override fun pay(id: String, payment: String): BillOccurrence? = runCatching {
        val foundBillOccurrence = BillOccurrenceDao.action { get(id.asUuid) }

        val occurrenceAmountLeft = foundBillOccurrence.amountLeft.toDouble()
        val paymentAmount = payment.toDouble()
        // todo what should we do if they change their amount to be lower than the amountLeft?
        if (occurrenceAmountLeft < paymentAmount) {
            Log.error("The amount left ${foundBillOccurrence.amountLeft} for the bill occurence $id was less than the payment amount $payment")
            return null
        }

        Log.info("Adding new payment of $payment for bill occurrence $id")
        val newPayment = PaymentDao.action {
            new(UUID.randomUUID()) {
                owner = foundBillOccurrence.owner
                amount = payment
                occurrence = foundBillOccurrence.id
                dateCreated = currentTimeMillis()
                dateUpdated = currentTimeMillis()
            }
        }

        BillOccurrenceDao.action {
            foundBillOccurrence.apply {
                amountLeft = (occurrenceAmountLeft - paymentAmount).toString()
                payments = SizedCollection(foundBillOccurrence.payments + newPayment)
                dateUpdated = currentTimeMillis()
            }
        }

        return get(foundBillOccurrence.owner, foundBillOccurrence.id.value.toString())
    }.getOrElse {
        Log.error("Failed to pay for bill occurrence", it)
        null
    }

    override fun delete(user: String, id: String): Boolean {
        val foundOccurrence = getSingle(user, id)

        if (foundOccurrence == null) {
            Log.error("Could not find a bill occurrence matching the id $id, for user $user to delete")
            return false
        }

        if (foundOccurrence.owner != user) {
            Log.warn("A user ($user) that was not the owner of the bill occurrence ${foundOccurrence.id} tried to delete it.")
            return false
        }

        Log.info("Deleting bill occurrence")
        BillOccurrenceDao.action { foundOccurrence.delete() }
        return true
    }

    private fun getSingle(user: String, id: String): BillOccurrenceDao? = BillOccurrenceDao.action {
        runCatching {
            BillOccurrenceDao.find {
                (BillOccurrenceTable.owner eq user) and (BillOccurrenceTable.id eq id.asUuid)
            }.first()
        }.getOrElse {
            Log.error("Failed to get single bill occurrence with id $id for user $user", it)
            null
        }
    }
}