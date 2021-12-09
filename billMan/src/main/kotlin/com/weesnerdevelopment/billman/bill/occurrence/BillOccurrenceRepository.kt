package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.shared.billMan.BillOccurrence
import java.util.*

interface BillOccurrenceRepository {
    fun getAll(user: UUID): List<BillOccurrence>
    fun getAllFor(user: UUID, billId: UUID): List<BillOccurrence>
    fun get(user: UUID, id: UUID): BillOccurrence?
    fun add(new: BillOccurrence): BillOccurrence?
    fun update(updated: BillOccurrence): BillOccurrence?
    fun pay(id: UUID, payment: String): BillOccurrence?
    fun delete(user: UUID, id: UUID): Boolean
}