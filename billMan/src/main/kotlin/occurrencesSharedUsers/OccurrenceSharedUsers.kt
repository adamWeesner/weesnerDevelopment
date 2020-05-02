package occurrencesSharedUsers

import shared.base.GenericItem
import shared.base.History
import shared.base.HistoryItem
import shared.currentTimeMillis

data class OccurrenceSharedUsers(
    override val id: Int? = null,
    val occurrenceId: Int,
    val userId: String,
    override var history: List<History>? = null,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem, HistoryItem
