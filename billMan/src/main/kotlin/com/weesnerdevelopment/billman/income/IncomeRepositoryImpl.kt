package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.billman.color.ColorDao
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and

object IncomeRepositoryImpl : IncomeRepository {
    override fun getAll(user: String): List<Income> {
        Log.info("Attempting to get all of user $user income")
        return getFor {
            IncomeTable.owner eq user
        }?.toIncomes()
            ?: emptyList()
    }

    override fun get(user: String, id: String): Income? =
        getSingle(user, id)?.toIncome()

    override fun add(new: Income): Income? {
        Log.info("Adding new color for new income")
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

        if (newColor == null)
            return null

        Log.info("Adding new income")
        val newIncome = IncomeDao.action {
            new(new.uuid.asUuid) {
                owner = new.owner
                name = new.name
                color = newColor
                amount = new.amount
                varyingAmount = new.varyingAmount
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated
            }
        }

        return newIncome?.toIncome()
    }

    override fun update(updated: Income): Income? {
        val uuid = updated.uuid

        if (uuid == null) {
            Log.error("Attempted to update a income without a valid uuid")
            return null
        }

        val foundIncome = getSingle(updated.owner, uuid)
        if (foundIncome == null) {
            Log.error("Could not find income matching the owner and uuid")
            return null
        }
        val foundIncomeAsIncome = IncomeDao.action { foundIncome.toIncome() }

        if (foundIncomeAsIncome?.color != updated.color) {
            // check if color changed, if did, delete old one, add new one
            if (foundIncomeAsIncome?.color?.uuid != updated.color.uuid) {
                Log.info("Deleting and re-adding color for income, as it changed")
                ColorDao.action { foundIncome.color.delete() }
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
                if (newColor == null)
                    return null

                foundIncome.color = newColor
            } else {
                // update color
                Log.info("Updating color for updated income")
                ColorDao.action {
                    foundIncome.color.apply {
                        red = updated.color.red
                        green = updated.color.green
                        blue = updated.color.blue
                        alpha = updated.color.alpha
                        dateUpdated = currentTimeMillis()
                    }
                }
            }
        } else {
            Log.info("Updated incomes color matched found income, skipping update")
        }

        // update history
//        foundIncome.toIncome().diff(updated).updates(foundIncome.owner.toUser()).forEach {
//            HistoryDao.action {
//                new {
//                    field = it.field
//                    oldValue = it.oldValue
//                    newValue = it.newValue
//                    updatedBy = UserDao.action { get(String.fromString(it.updatedBy.uuid)).String }
//                    dateCreated = it.dateCreated
//                    dateUpdated = it.dateUpdated
//                }
//            }
//        }

        // update Income
        Log.info("Updating income")
        IncomeDao.action {
            foundIncome.apply {
                name = updated.name
                amount = updated.amount
                varyingAmount = updated.varyingAmount
                dateUpdated = currentTimeMillis()
            }
        }

        return get(foundIncome.owner, foundIncome.id.value.toString())
    }

    override fun delete(user: String, id: String): Boolean {
        val foundIncome = getSingle(user, id)

        if (foundIncome == null) {
            Log.error("Could not find a income matching the id $id, for user $user to delete")
            return false
        }

        if (foundIncome.owner != user) {
            Log.warn("A user ($user) that was not the owner of the income ${foundIncome.id} tried to delete it.")
            return false
        }

        Log.info("Deleting income")
        IncomeDao.action { foundIncome.delete() }
        return true
    }

    private fun getFor(op: SqlExpressionBuilder.() -> Op<Boolean>) =
        IncomeDao.action { find(op) }

    private fun getSingle(user: String, id: String): IncomeDao? =
        IncomeDao.action {
            getFor {
                (IncomeTable.owner eq user) and (IncomeTable.id eq id.asUuid)
            }?.firstOrNull()
        }
}