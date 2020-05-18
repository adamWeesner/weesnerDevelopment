package occurrencesSharedUsers

import shared.base.GenericItem
import shared.currentTimeMillis

data class OccurrenceSharedUsers(
    override val id: Int? = null,
    val occurrenceId: Int,
    val userId: String,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
