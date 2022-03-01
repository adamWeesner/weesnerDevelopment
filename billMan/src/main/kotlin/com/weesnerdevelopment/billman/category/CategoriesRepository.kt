package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.shared.billMan.Category

interface CategoriesRepository {
    fun getAll(user: String): List<Category>
    fun get(user: String?, id: String): Category?
    fun add(new: Category): Category?
    fun update(updated: Category): Category?
    fun delete(user: String, id: String): Boolean
}