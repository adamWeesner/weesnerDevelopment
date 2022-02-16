package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.shared.billMan.Category
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CategoryDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CategoryDao>(CategoryTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    val uuid by CategoryTable.id
    var owner by CategoryTable.owner
    var name by CategoryTable.name
    var dateCreated by CategoryTable.dateCreated
    var dateUpdated by CategoryTable.dateUpdated
//    val history by HistoryDao via CategoryHistoryTable

    fun <T> action(event: CategoryDao.() -> T) = transaction { event() }
}

fun SizedIterable<CategoryDao>.toCategories(): List<Category> = map {
    it.toCategory()
}

fun CategoryDao.toCategory(): Category = Category(
    uuid = uuid.value.toString(),
    name = name,
    owner = owner,
//    history = history.toHistories(),
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)
