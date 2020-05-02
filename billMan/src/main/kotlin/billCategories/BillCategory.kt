package billCategories

import shared.base.GenericItem
import shared.base.History
import shared.base.HistoryItem
import shared.currentTimeMillis

data class BillCategory(
    override val id: Int? = null,
    val billId: Int,
    val categoryId: Int,
    override var history: List<History>? = null,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem, HistoryItem
