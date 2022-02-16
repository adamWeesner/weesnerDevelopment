package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.billMan.Category
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class CategoryDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CategoryDao>(CategoryTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    val uuid by CategoryTable.id
    var owner by CategoryTable.owner
    var name by CategoryTable.name
    var dateCreated by CategoryTable.dateCreated
    var dateUpdated by CategoryTable.dateUpdated
//    val history by HistoryDao via CategoryHistoryTable

    fun <T> action(event: CategoryDao.() -> T) = tryTransaction(event)
}

fun CategoryDao.toCategory(): Category? = action {
    Category(
        uuid = uuid.value.toString(),
        name = name,
        owner = owner,
//    history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}

fun SizedIterable<CategoryDao>.toCategories(): List<Category> = CategoryDao.action {
    mapNotNull {
        it.toCategory()
    }
} ?: emptyList()