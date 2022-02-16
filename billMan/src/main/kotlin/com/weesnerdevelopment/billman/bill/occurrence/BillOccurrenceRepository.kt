package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.shared.billMan.BillOccurrence

interface BillOccurrenceRepository {
    fun getAll(user: String): List<BillOccurrence>
    fun getAllFor(user: String, billId: String): List<BillOccurrence>
    fun get(user: String, id: String): BillOccurrence?
    fun add(new: BillOccurrence): BillOccurrence?
    fun update(updated: BillOccurrence): BillOccurrence?
    fun pay(id: String, payment: String): BillOccurrence?
    fun delete(user: String, id: String): Boolean
}