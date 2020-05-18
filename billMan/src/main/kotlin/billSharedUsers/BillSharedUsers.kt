package billSharedUsers

import shared.base.GenericItem
import shared.currentTimeMillis

data class BillSharedUsers(
    override val id: Int? = null,
    val billId: Int,
    val userId: String,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
