package category

import generics.GenericItem
import generics.IdTable

object Categories : IdTable() {
    val owner = varchar("owner", 255)
    val name = varchar("name", 255)
}

data class Category(
    override var id: Int?,
    val name: String?,
    val owner: String?,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)