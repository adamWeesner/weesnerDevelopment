package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.asUuid
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.billman.color.ColorDao
import com.weesnerdevelopment.billman.color.toColor
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.currentTimeMillis
import diff
import org.jetbrains.exposed.sql.and
import java.util.*

object IncomeRepositoryImpl : IncomeRepository {
    override fun getAll(user: UUID): List<Income> {
        return IncomeDao.action {
            runCatching {
                find {
                    IncomeTable.owner eq user
                }.toIncomes()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun get(user: UUID, id: UUID): Income? {
        return IncomeDao.action { getSingle(user, id)?.toIncome() }
    }

    override fun add(new: Income): Income? = runCatching {
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

        IncomeDao.action {
            new(new.uuid.asUuid) {
                name = new.name
                amount = new.amount
                varyingAmount = new.varyingAmount
                color = newColor
                dateCreated = new.dateCreated
                dateUpdated = new.dateUpdated

                owner = new.owner.let { UserDao.action { get(UUID.fromString(it.uuid)) } }
            }.toIncome()
        }
    }.getOrNull()

    override fun update(updated: Income): Income? {
        val foundIncome = getSingle(UUID.fromString(updated.owner.uuid), UUID.fromString(updated.uuid))
        if (foundIncome == null)
            return null

        // check if color changed, if did, delete old one, add new one
        if (ColorDao.action { foundIncome.color.toColor().uuid } != updated.color.uuid) {
            foundIncome.color.delete()
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
            foundIncome.color = newColor
        } else {
            // update color
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

        // update history
        foundIncome.toIncome().diff(updated).updates(foundIncome.owner.toUser()).forEach {
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

        // update Income
        foundIncome.apply {
            name = updated.name
            amount = updated.amount
            varyingAmount = updated.varyingAmount
            dateUpdated = currentTimeMillis()
        }

        return get(foundIncome.owner.id.value, foundIncome.id.value)
    }

    override fun delete(user: UUID, id: UUID) = IncomeDao.action {
        val foundIncome = getSingle(user, id)

        if (foundIncome == null)
            return@action false

        foundIncome.delete()
        return@action true
    }

    private fun getSingle(user: UUID, id: UUID): IncomeDao? = IncomeDao.action {
        find {
            (IncomeTable.owner eq user) and (IncomeTable.id eq id)
        }.firstOrNull()
    }
}