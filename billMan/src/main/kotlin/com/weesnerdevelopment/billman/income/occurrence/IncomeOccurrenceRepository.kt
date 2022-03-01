package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.shared.billMan.IncomeOccurrence

interface IncomeOccurrenceRepository {
    fun getAll(user: String): List<IncomeOccurrence>
    fun getAllFor(user: String, incomeId: String): List<IncomeOccurrence>
    fun get(user: String, id: String): IncomeOccurrence?
    fun add(new: IncomeOccurrence): IncomeOccurrence?
    fun update(updated: IncomeOccurrence): IncomeOccurrence?
    fun delete(user: String, id: String): Boolean
}