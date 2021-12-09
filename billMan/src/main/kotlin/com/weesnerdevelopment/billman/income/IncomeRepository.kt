package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.shared.billMan.Income
import java.util.*

interface IncomeRepository {
    fun getAll(user: UUID): List<Income>
    fun get(user: UUID, id: UUID): Income?
    fun add(new: Income): Income?
    fun update(updated: Income): Income?
    fun delete(user: UUID, id: UUID): Boolean
}