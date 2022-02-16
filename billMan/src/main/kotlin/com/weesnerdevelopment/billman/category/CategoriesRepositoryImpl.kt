package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.asUuid
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.currentTimeMillis
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

object CategoriesRepositoryImpl : CategoriesRepository {
    override fun getAll(user: String): List<Category> = CategoryDao.action {
        Log.info("Attempting to get all of user $user categories")
        runCatching {
            find {
                (CategoryTable.owner eq user) or CategoryTable.owner.isNull()
            }.toCategories()
        }.getOrElse {
            Log.error("Failed to get categories for user $user", it)
            null
        } ?: emptyList()
    }

    override fun get(user: String?, id: String): Category? {
        return CategoryDao.action { getSingle(user, id)?.toCategory() }
    }

    override fun add(new: Category): Category? {
        Log.info("Adding new Category")
        return CategoryDao.action {
            runCatching {
                new(new.uuid.asUuid) {
                    name = new.name
                    dateCreated = new.dateCreated
                    dateUpdated = new.dateUpdated

                    owner = new.owner
                }.toCategory()
            }.getOrNull()
        }
    }

    override fun update(updated: Category): Category? {
        val updatedUuid = updated.uuid
        if (updatedUuid == null)
            return null

        val foundCategory = CategoryDao.action { getSingle(updated.owner, updatedUuid) }

        if (foundCategory?.owner == null || foundCategory.owner != updated.owner)
            return null

        Log.info("Updating category")
        CategoryDao.action {
            foundCategory.apply {
                name = updated.name
                owner = updated.owner
                dateUpdated = currentTimeMillis()
            }
        }

        // update history
//        val categoryOwner = foundCategory.owner
//        if (categoryOwner != null)
//            foundCategory.toCategory().diff(updated).updates(categoryOwner.toUser()).forEach {
//                HistoryDao.action {
//                    new {
//                        field = it.field
//                        oldValue = it.oldValue
//                        newValue = it.newValue
//                        updatedBy = categoryOwner.uuid
//                        dateCreated = it.dateCreated
//                        dateUpdated = it.dateUpdated
//                    }
//                }
//            }

        return get(foundCategory.owner, foundCategory.uuid.value.toString())
    }

    override fun delete(user: String, id: String): Boolean {
        val foundCategory = CategoryDao.action { getSingle(user, id) }

        if (foundCategory == null) {
            Log.error("Could not find a category matching the id $id, for user $user to delete")
            return false
        }

        if (foundCategory.owner != user) {
            Log.warn("A user ($user) that was not the owner of the category $id tried to delete it.")
            return false
        }

        Log.info("Deleting category")
        CategoryDao.action { foundCategory.delete() }
        return true
    }

    private fun getSingle(user: String?, id: String): CategoryDao? = CategoryDao.action {
        runCatching {
            find {
                if (user == null)
                    CategoryTable.owner.isNull() and (CategoryTable.id eq id.asUuid)
                else
                    ((CategoryTable.owner eq user) or CategoryTable.owner.isNull()) and (CategoryTable.id eq id.asUuid)
            }.first()
        }.getOrElse {
            Log.error("Failed to get single category with id $id for user $user", it)
            null
        }
    }
}