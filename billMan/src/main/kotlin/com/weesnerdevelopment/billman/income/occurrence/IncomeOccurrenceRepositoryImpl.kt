package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.asUuid
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.billman.income.IncomeDao
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import com.weesnerdevelopment.shared.currentTimeMillis
import diff
import org.jetbrains.exposed.sql.and
import java.util.*

object IncomeOccurrenceRepositoryImpl : IncomeOccurrenceRepository {
    override fun getAll(user: UUID): List<IncomeOccurrence> {
        return IncomeOccurrenceDao.action {
            runCatching {
                find {
                    (IncomeOccurrenceTable.owner eq user)
                }.toCategories()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun getAllFor(user: UUID, incomeId: UUID): List<IncomeOccurrence> {
        return IncomeOccurrenceDao.action {
            runCatching {
                find {
                    (IncomeOccurrenceTable.owner eq user) and (IncomeOccurrenceTable.income eq incomeId)
                }.toCategories()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun get(user: UUID, id: UUID): IncomeOccurrence? {
        return IncomeOccurrenceDao.action { getSingle(user, id)?.toIncomeOccurrence() }
    }

    override fun add(new: IncomeOccurrence): IncomeOccurrence? {
        return IncomeOccurrenceDao.action {
            runCatching {
                new(new.uuid.asUuid) {
                    owner = UserDao.action { get(UUID.fromString(new.owner.uuid)) }
                    income = IncomeDao.action { get(UUID.fromString(new.itemId)) }
                    dueDate = new.dueDate
                    amount = new.amount
                    every = new.every
                    dateCreated = new.dateCreated
                    dateUpdated = new.dateUpdated
                }.toIncomeOccurrence()
            }.getOrNull()
        }
    }

    override fun update(updated: IncomeOccurrence): IncomeOccurrence? {
        val foundIncomeOccurrence = IncomeOccurrenceDao.action { get(UUID.fromString(updated.uuid)) }

        // update history
        foundIncomeOccurrence.toIncomeOccurrence().diff(updated).updates(foundIncomeOccurrence.owner.toUser()).forEach {
            HistoryDao.action {
                new {
                    field = it.field
                    oldValue = it.oldValue
                    newValue = it.newValue
                    updatedBy = foundIncomeOccurrence.owner.uuid
                    dateCreated = it.dateCreated
                    dateUpdated = it.dateUpdated
                }
            }
        }

        foundIncomeOccurrence.apply {
            dueDate = updated.dueDate
            amount = updated.amount
            every = updated.every
            owner = updated.owner.let { UserDao.action { get(UUID.fromString(it.uuid)) } }
            dateUpdated = currentTimeMillis()
        }

        return get(foundIncomeOccurrence.owner.uuid.value, foundIncomeOccurrence.uuid.value)
    }

    override fun delete(user: UUID, id: UUID): Boolean {
        val foundOccurrence = IncomeDao.action { get(id) }

        if (foundOccurrence == null)
            return false

        foundOccurrence.delete()
        return true
    }

    private fun getSingle(user: UUID, id: UUID): IncomeOccurrenceDao? = IncomeOccurrenceDao.action {
        find {
            (IncomeOccurrenceTable.owner eq user) and (IncomeOccurrenceTable.id eq id)
        }.firstOrNull()
    }
}