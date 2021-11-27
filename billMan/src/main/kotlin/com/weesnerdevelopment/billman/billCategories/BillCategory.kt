package com.weesnerdevelopment.billman.billCategories

import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.currentTimeMillis

data class BillCategory(
    override val id: Int? = null,
    val billId: Int,
    val categoryId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
