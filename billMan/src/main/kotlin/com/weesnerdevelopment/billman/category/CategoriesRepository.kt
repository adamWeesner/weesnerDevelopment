package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.shared.billMan.Category
import java.util.*

interface CategoriesRepository {
    fun getAll(user: UUID): List<Category>
    fun get(user: UUID?, id: UUID): Category?
    fun add(new: Category): Category?
    fun update(updated: Category): Category?
    fun delete(user: UUID, id: UUID): Boolean
}