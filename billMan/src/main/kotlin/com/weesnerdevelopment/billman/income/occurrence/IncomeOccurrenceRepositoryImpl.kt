package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.billman.income.IncomeDao
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import java.util.*

object IncomeOccurrenceRepositoryImpl : IncomeOccurrenceRepository {
    override fun getAll(user: String): List<IncomeOccurrence> {
        Log.info("Attempting to get all of user $user income occurrences")
        return getFor {
            (IncomeOccurrenceTable.owner eq user)
        }?.toIncomeOccurrences()
            ?: emptyList()
    }

    override fun getAllFor(user: String, incomeId: String): List<IncomeOccurrence> {
        Log.info("Attempting to get all of user $user income occurrences for bill $incomeId")
        return getFor {
            (IncomeOccurrenceTable.owner eq user) and (IncomeOccurrenceTable.income eq incomeId.asUuid)
        }?.toIncomeOccurrences()
            ?: emptyList()
    }

    override fun get(user: String, id: String) =
        getSingle(user, id)?.toIncomeOccurrence()

    override fun add(new: IncomeOccurrence): IncomeOccurrence? {
        val retrievedIncome = IncomeDao.action { get(UUID.fromString(new.itemId)) }

        if (retrievedIncome == null)
            return null

        return IncomeOccurrenceDao.action {
            Log.info("Adding new income occurrence")
            new(new.uuid.asUuid) {
                owner = new.owner
                income = retrievedIncome
                dueDate = new.dueDate
                amount = new.amount
                every = new.every
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated
            }.toIncomeOccurrence()
        }
    }

    override fun update(updated: IncomeOccurrence): IncomeOccurrence? {
        val occurrenceId = updated.uuid
        if (occurrenceId == null) {
            Log.error("Attempted to update a income occurrence without a valid uuid")
            return null
        }

        val foundIncomeOccurrence = getSingle(updated.owner, occurrenceId)
        if (foundIncomeOccurrence == null) {
            Log.error("Could not find income occurrence matching the owner and uuid")
            return null
        }

        // update history
//        foundIncomeOccurrence.toIncomeOccurrence().diff(updated).updates(foundIncomeOccurrence.owner.toUser()).forEach {
//            HistoryDao.action {
//                new {
//                    field = it.field
//                    oldValue = it.oldValue
//                    newValue = it.newValue
//                    updatedBy = foundIncomeOccurrence.owner.uuid
//                    dateCreated = it.dateCreated
//                    dateUpdated = it.dateUpdated
//                }
//            }
//        }

        Log.info("Updating income occurrence")
        IncomeOccurrenceDao.action {
            foundIncomeOccurrence.apply {
                dueDate = updated.dueDate
                amount = updated.amount
                every = updated.every
                owner = updated.owner
                dateUpdated = currentTimeMillis()
            }
        }

        return get(foundIncomeOccurrence.owner, foundIncomeOccurrence.uuid.value.toString())
    }

    override fun delete(user: String, id: String): Boolean {
        val foundOccurrence = getSingle(user, id)

        if (foundOccurrence == null) {
            Log.error("Could not find a income occurrence matching the id $id, for user $user to delete")
            return false
        }

        if (foundOccurrence.owner != user) {
            Log.warn("A user ($user) that was not the owner of the income occurrence ${foundOccurrence.id} tried to delete it.")
            return false
        }

        Log.info("Deleting income occurrence")
        IncomeOccurrenceDao.action { foundOccurrence.delete() }
        return true
    }

    private fun getFor(op: SqlExpressionBuilder.() -> Op<Boolean>) =
        IncomeOccurrenceDao.action { find(op) }

    private fun getSingle(user: String, id: String): IncomeOccurrenceDao? = IncomeOccurrenceDao.action {
        getFor {
            (IncomeOccurrenceTable.owner eq user) and (IncomeOccurrenceTable.id eq id.asUuid)
        }?.firstOrNull()
    }
}