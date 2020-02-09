import generics.GenericItem

data class Bill(
    override var id: Int?,
    val owner: String,
    var name: String,
    var amount: String,
    var sharedUsers: List<String>?,
    var categories: List<String>?,
    var color: Color? = null,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)

data class Color(
    var red: Int? = null,
    var green: Int? = null,
    var blue: Int? = null,
    var alpha: Int? = null
)