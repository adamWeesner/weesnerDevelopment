package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import java.util.*

interface IncomeOccurrenceRepository {
    fun getAll(user: UUID): List<IncomeOccurrence>
    fun getAllFor(user: UUID, incomeId: UUID): List<IncomeOccurrence>
    fun get(user: UUID, id: UUID): IncomeOccurrence?
    fun add(new: IncomeOccurrence): IncomeOccurrence?
    fun update(updated: IncomeOccurrence): IncomeOccurrence?
    fun delete(user: UUID, id: UUID): Boolean
}