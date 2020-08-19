package logging

import shared.base.GenericItem

data class Logger(
    override val id: Int? = null,
    val log: String,
    val cause: String?,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem
