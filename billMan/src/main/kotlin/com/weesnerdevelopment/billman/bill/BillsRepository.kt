package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.shared.billMan.Bill

interface BillsRepository {
    fun getAll(user: String): List<Bill>
    fun get(user: String, id: String): Bill?
    fun add(new: Bill): Bill?
    fun update(updated: Bill): Bill?
    fun delete(user: String, id: String): Boolean
}