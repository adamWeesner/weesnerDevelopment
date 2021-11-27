package com.weesnerdevelopment.validator.complex

import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.base.HistoryItem
import com.weesnerdevelopment.shared.base.OwnedItem
import com.weesnerdevelopment.shared.currentTimeMillis

data class ComplexValidatorItem(
    override val id: Int? = null,
    override val owner: User,
    val name: String,
    val amount: Double,
    val category: Category,
    override var history: List<History>? = null,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem, HistoryItem, OwnedItem
