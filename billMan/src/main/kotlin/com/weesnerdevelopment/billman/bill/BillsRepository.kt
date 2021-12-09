package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.shared.billMan.Bill
import java.util.*

interface BillsRepository {
    fun getAll(user: UUID): List<Bill>
    fun get(user: UUID, id: UUID): Bill?
    fun add(new: Bill): Bill?
    fun update(updated: Bill): Bill?
    fun delete(user: UUID, id: UUID): Boolean
}