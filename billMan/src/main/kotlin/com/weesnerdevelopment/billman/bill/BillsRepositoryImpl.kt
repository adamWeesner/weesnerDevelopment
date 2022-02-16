package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.billman.category.CategoryDao
import com.weesnerdevelopment.billman.color.ColorDao
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object BillsRepositoryImpl : BillsRepository {
    override fun getAll(user: String): List<Bill> = BillDao.action {
        Log.info("Attempting to get all of user $user bills")
        runCatching {
            find {
                BillTable.owner eq user
            }.toBills()
        }.getOrElse {
            Log.error("Failed to get bills for user $user", it)
            null
        } ?: emptyList()
    }

    override fun get(user: String, id: String): Bill? = BillDao.action {
        getSingle(user, id)?.toBill()
    }

    override fun add(new: Bill): Bill? = runCatching {
        Log.info("Adding new color for new bill")
        val newColor = ColorDao.action {
            new(new.color.uuid.asUuid) {
                red = new.color.red
                green = new.color.green
                blue = new.color.blue
                alpha = new.color.alpha
                dateCreated = new.color.dateCreated
                dateUpdated = new.color.dateUpdated
            }
        }

        Log.info("Linking categories for new bill")
        val billCategories = new.categories.map { CategoryDao.action { get(it.uuid.asUuid) } }

        Log.info("Adding new bill")
        val newBill = BillDao.action {
            new(new.uuid.asUuid) {
                owner = new.owner
                name = new.name
                amount = new.amount
                varyingAmount = new.varyingAmount
                payoffAmount = new.payoffAmount
                color = newColor
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated

                categories = SizedCollection(billCategories)
            }
        }

        Log.info("Adding and linking shared users for new bill")
        new.sharedUsers.forEach {
            BillSharedUsersDao.action {
                new(UUID.randomUUID()) {
                    user = it
                    bill = newBill.id
                }
            }
        }

        Log.info("Converting new bill")
        BillDao.action {
            newBill.toBill()
        }
    }.getOrElse {
        Log.error("Failed to add new bill", it)
        null
    }

    override fun update(updated: Bill): Bill? {
        val uuid = updated.uuid
        if (uuid == null) {
            Log.error("Attempted to update a bill without a valid uuid")
            return null
        }

        val foundBill = getSingle(updated.owner, uuid)
        if (foundBill == null) {
            Log.error("Could not find bill matching the owner and uuid")
            return null
        }
        val foundBillAsBill = BillDao.action { foundBill.toBill() }

        if (foundBillAsBill.color != updated.color) {
            // check if color changed, if did, delete old one, add new one
            if (ColorDao.action { foundBillAsBill.color.uuid } != updated.color.uuid) {
                Log.info("Deleting and re-adding color for bill, as it changed")
                ColorDao.action { foundBill.color.delete() }
                val newColor = ColorDao.action {
                    new(updated.color.uuid.asUuid) {
                        red = updated.color.red
                        green = updated.color.green
                        blue = updated.color.blue
                        alpha = updated.color.alpha
                        dateCreated = updated.color.dateCreated
                        dateUpdated = currentTimeMillis()
                    }
                }
                foundBill.color = newColor
            } else {
                // update color
                Log.info("Updating color for updated bill")
                ColorDao.action {
                    foundBill.color.apply {
                        red = updated.color.red
                        green = updated.color.green
                        blue = updated.color.blue
                        alpha = updated.color.alpha
                        dateUpdated = currentTimeMillis()
                    }
                }
            }
        } else {
            Log.info("Updated bills color matched found bill, skipping update")
        }


        if (foundBillAsBill.categories != updated.categories) {
            Log.info("Removing old categories for updated bill")
            foundBill.categories.forEach {
                transaction {
                    BillsCategoriesTable.deleteWhere {
                        (BillsCategoriesTable.bill eq foundBill.id) and (BillsCategoriesTable.category eq it.uuid)
                    }
                }
            }

            Log.info("Adding new categories for updated bill")
            val newBillCategories = updated.categories.map { CategoryDao.action { get(UUID.fromString(it.uuid)) } }
            foundBill.categories = SizedCollection(newBillCategories)
        } else {
            Log.info("Updated bills categories matched found bill, skipping update")
        }

        if (foundBillAsBill.sharedUsers != updated.sharedUsers) {

            // update shared users
            Log.info("Removing shared users for found bill")
            BillSharedUsersDao.action {
                BillSharedUsersTable.deleteWhere {
                    (BillSharedUsersTable.bill eq foundBill.id)
                }
            }

            Log.info("Adding and linking shared users for new bill")
            updated.sharedUsers.map {
                BillSharedUsersDao.action {
                    new(UUID.randomUUID()) {
                        user = it
                        bill = foundBill.id
                    }
                }
            }
        } else {
            Log.info("Updated bills shared users matched found bill, skipping update")
        }

        // update history
//        foundBill.toBill().diff(updated).updates(foundBill.owner.toUser()).forEach {
//            HistoryDao.action {
//                new {
//                    field = it.field
//                    oldValue = it.oldValue
//                    newValue = it.newValue
//                    updatedBy = UserDao.action { get(UUID.fromString(it.updatedBy.uuid)).uuid }
//                    dateCreated = it.dateCreated
//                    dateUpdated = it.dateUpdated
//                }
//            }
//        }

        // update bill
        Log.info("Updating bill")
        BillDao.action {
            foundBill.apply {
                name = updated.name
                amount = updated.amount
                varyingAmount = updated.varyingAmount
                payoffAmount = updated.payoffAmount
                dateUpdated = currentTimeMillis()
            }
        }

        return get(foundBill.owner, foundBill.id.value.toString())
    }

    override fun delete(user: String, id: String): Boolean {
        val foundBill = getSingle(user, id)

        if (foundBill == null) {
            Log.error("Could not find a bill matching the id $id, for user $user to delete")
            return false
        }

        if (foundBill.owner != user) {
            Log.warn("A user ($user) that was not the owner of the bill ${foundBill.id} tried to delete it.")
            return false
        }

        Log.info("Deleting bill")
        BillDao.action { foundBill.delete() }
        return true
    }

    private fun getSingle(user: String, id: String): BillDao? = BillDao.action {
        runCatching {
            find {
                (BillTable.owner eq user) and (BillTable.id eq id.asUuid)
            }.first()
        }.getOrElse {
            Log.error("Failed to get single bill with id $id for user $user", it)
            null
        }
    }
}