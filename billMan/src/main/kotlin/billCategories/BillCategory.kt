package billCategories

import shared.base.GenericItem
import shared.currentTimeMillis

data class BillCategory(
    override val id: Int? = null,
    val billId: Int,
    val categoryId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
