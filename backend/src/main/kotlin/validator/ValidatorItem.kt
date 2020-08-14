package com.weesnerdevelopment.validator

import shared.base.GenericItem
import shared.currentTimeMillis

data class ValidatorItem(
    override val id: Int? = null,
    val name: String,
    val amount: Double,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
