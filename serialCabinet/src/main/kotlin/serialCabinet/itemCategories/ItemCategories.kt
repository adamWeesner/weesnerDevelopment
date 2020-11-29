package serialCabinet.itemCategories

import shared.base.GenericItem
import shared.currentTimeMillis

data class ItemCategories(
    override val id: Int? = null,
    val categoryId: Int,
    val itemId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
