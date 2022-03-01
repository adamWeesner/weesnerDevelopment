package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.shared.billMan.Income

interface IncomeRepository {
    fun getAll(user: String): List<Income>
    fun get(user: String, id: String): Income?
    fun add(new: Income): Income?
    fun update(updated: Income): Income?
    fun delete(user: String, id: String): Boolean
}