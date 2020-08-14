package com.weesnerdevelopment.validator.complex

import shared.auth.User
import shared.base.GenericItem
import shared.base.History
import shared.base.HistoryItem
import shared.base.OwnedItem
import shared.billMan.Category
import shared.currentTimeMillis

data class ComplexValidatorItem(
    override val id: Int? = null,
    override val owner: User,
    val name: String,
    val amount: Double,
    val category: Category,
    override var history: List<History>? = null,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem, HistoryItem,
    OwnedItem