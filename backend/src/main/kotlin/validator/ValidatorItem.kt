package com.weesnerdevelopment.validator

import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.currentTimeMillis

data class ValidatorItem(
    override val id: Int? = null,
    val name: String,
    val amount: Double,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
