package bill

import category.Categories
import com.squareup.moshi.JsonClass
import generics.GenericItem
import generics.IdTable
import user.Users

object Bills : IdTable() {
    val owner = varchar("owner", 255)
    val name = varchar("name", 255)
    val amount = double("amount")
}

object BillsSharedUsers : IdTable() {
    val user = (varchar("user", 255) references Users.uuid)
    val bill = (integer("bill") references Bills.id)
}

object BillCategories : IdTable() {
    val category = (integer("category") references Categories.id)
    val bill = (integer("bill") references Bills.id)
}

object BillColors : IdTable() {
    val red = integer("red")
    val green = integer("green")
    val blue = integer("blue")
    val alpha = integer("alpha")
    val bill = (integer("bill") references Bills.id)
}

@JsonClass(generateAdapter = true)
data class BillResponse(
    val socialSecurity: List<Bill>
)

@JsonClass(generateAdapter = true)
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