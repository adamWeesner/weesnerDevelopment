package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.asUuid
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.billman.income.IncomeDao
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.currentTimeMillis
import diff
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import java.util.*

object CategoriesRepositoryImpl : CategoriesRepository {
    override fun getAll(user: UUID): List<Category> {
        return CategoryDao.action {
            runCatching {
                find {
                    (CategoryTable.owner eq user) or CategoryTable.owner.isNull()
                }.toCategories()
            }.getOrNull() ?: emptyList()
        }
    }

    override fun get(user: UUID?, id: UUID): Category? {
        return CategoryDao.action { getSingle(user, id)?.toCategory() }
    }

    override fun add(new: Category): Category? {
        return CategoryDao.action {
            runCatching {
                new(new.uuid.asUuid) {
                    name = new.name
                    dateCreated = new.dateCreated
                    dateUpdated = new.dateUpdated

                    owner = new.owner?.let { UserDao.action { get(UUID.fromString(it.uuid)) } }
                }.toCategory()
            }.getOrNull()
        }
    }

    override fun update(updated: Category): Category? {
        val foundCategory = CategoryDao.action { get(UUID.fromString(updated.uuid)) }

        foundCategory.apply {
            name = updated.name
            owner = updated.owner?.let { UserDao.action { get(UUID.fromString(it.uuid)) } }
            dateUpdated = currentTimeMillis()
        }

        // update history
        val categoryOwner = foundCategory.owner
        if (categoryOwner != null)
            foundCategory.toCategory().diff(updated).updates(categoryOwner.toUser()).forEach {
                HistoryDao.action {
                    new {
                        field = it.field
                        oldValue = it.oldValue
                        newValue = it.newValue
                        updatedBy = categoryOwner.uuid
                        dateCreated = it.dateCreated
                        dateUpdated = it.dateUpdated
                    }
                }
            }

        return get(foundCategory.owner?.uuid?.value, foundCategory.uuid.value)
    }

    override fun delete(user: UUID, id: UUID): Boolean {
        val foundIncome = IncomeDao.action { get(id) }

        if (foundIncome == null)
            return false

        foundIncome.delete()
        return true
    }

    private fun getSingle(user: UUID?, id: UUID): CategoryDao? = CategoryDao.action {
        find {
            if (user == null)
                CategoryTable.owner.isNull() and (CategoryTable.id eq id)
            else
                ((CategoryTable.owner eq user) or CategoryTable.owner.isNull()) and (CategoryTable.id eq id)
        }.firstOrNull()
    }
}