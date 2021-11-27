package com.weesnerdevelopment.billman.occurrencesSharedUsers

import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.currentTimeMillis

data class OccurrenceSharedUsers(
    override val id: Int? = null,
    val occurrenceId: Int,
    val userId: String,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
