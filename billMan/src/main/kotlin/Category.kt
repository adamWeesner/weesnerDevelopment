import generics.GenericItem

data class Category(
    override var id: Int?,
    val name: String?,
    val owner: String?,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)