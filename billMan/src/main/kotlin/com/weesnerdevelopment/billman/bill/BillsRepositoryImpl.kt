package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.asUuid
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.billman.category.CategoryDao
import com.weesnerdevelopment.billman.color.ColorDao
import com.weesnerdevelopment.billman.color.toColor
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.currentTimeMillis
import diff
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

object BillsRepositoryImpl : BillsRepository {
    override fun getAll(user: UUID): List<Bill> {
        return BillDao.action {
            runCatching {
                find {
                    BillTable.owner eq user
                }.toBills()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun get(user: UUID, id: UUID): Bill? {
        return BillDao.action { getSingle(user, id)?.toBill() }
    }

    override fun add(new: Bill): Bill? = runCatching {
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

        val billCategories = new.categories.map { CategoryDao.action { get(UUID.fromString(it.uuid)) } }
        val billSharedUsers = new.sharedUsers?.map { UserDao.action { get(UUID.fromString(it.uuid)) } }

        BillDao.action {
            new(new.uuid.asUuid) {
                name = new.name
                amount = new.amount
                varyingAmount = new.varyingAmount
                payoffAmount = new.payoffAmount
                color = newColor
                if (billSharedUsers != null)
                    sharedUsers = SizedCollection(billSharedUsers)
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated

                owner = new.owner.let { UserDao.action { get(UUID.fromString(it.uuid)) } }
                categories = SizedCollection(billCategories)
            }.toBill()
        }
    }.getOrNull()

    override fun update(updated: Bill): Bill? {
        val foundBill = getSingle(UUID.fromString(updated.owner.uuid), UUID.fromString(updated.uuid))
        if (foundBill == null)
            return null

        // check if color changed, if did, delete old one, add new one
        if (ColorDao.action { foundBill.color.toColor().uuid } != updated.color.uuid) {
            foundBill.color.delete()
            val newColor = ColorDao.action {
                new(updated.color.uuid.asUuid) {
                    red = updated.color.red
                    green = updated.color.green
                    blue = updated.color.blue
                    alpha = updated.color.alpha
                    dateCreated = updated.color.dateCreated
                    dateUpdated = updated.color.dateUpdated
                }
            }
            foundBill.color = newColor
        } else {
            // update color
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

        // update categories
        foundBill.categories.forEach {
            BillsCategoriesTable.deleteWhere {
                (BillsCategoriesTable.bill eq foundBill.id) and (BillsCategoriesTable.category eq it.uuid)
            }
        }

        val newBillCategories = updated.categories.map { CategoryDao.action { get(UUID.fromString(it.uuid)) } }
        foundBill.categories = SizedCollection(newBillCategories)

        // update shared users
        foundBill.sharedUsers.forEach {
            BillSharedUsersTable.deleteWhere {
                (BillSharedUsersTable.bill eq foundBill.id) and (BillSharedUsersTable.user eq it.uuid)
            }
        }

        val newSharedUsers = updated.sharedUsers?.map { UserDao.action { get(UUID.fromString(it.uuid)) } }
        if (newSharedUsers != null)
            foundBill.sharedUsers = SizedCollection(newSharedUsers)

        // update history
        foundBill.toBill().diff(updated).updates(foundBill.owner.toUser()).forEach {
            HistoryDao.action {
                new {
                    field = it.field
                    oldValue = it.oldValue
                    newValue = it.newValue
                    updatedBy = UserDao.action { get(UUID.fromString(it.updatedBy.uuid)).uuid }
                    dateCreated = it.dateCreated
                    dateUpdated = it.dateUpdated
                }
            }
        }

        // update bill
        foundBill.apply {
            name = updated.name
            amount = updated.amount
            varyingAmount = updated.varyingAmount
            payoffAmount = updated.payoffAmount
            dateUpdated = currentTimeMillis()
        }

        return get(foundBill.owner.id.value, foundBill.id.value)
    }

    override fun delete(user: UUID, id: UUID) = BillDao.action {
        val foundBill = getSingle(user, id)

        if (foundBill == null)
            return@action false

        foundBill.delete()
        return@action true
    }

    private fun getSingle(user: UUID, id: UUID): BillDao? = BillDao.action {
        find {
            (BillTable.owner eq user) and (BillTable.id eq id)
        }.firstOrNull()
    }
}