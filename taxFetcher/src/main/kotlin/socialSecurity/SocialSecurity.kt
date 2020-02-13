package socialSecurity

import com.squareup.moshi.JsonClass
import generics.GenericItem
import generics.IdTable

object SocialSecuritys : IdTable() {
    val year = integer("year").primaryKey()
    val percent = double("percent")
    val limit = integer("limit")
}

@JsonClass(generateAdapter = true)
data class SocialSecurityResponse(
    val socialSecurity: List<SocialSecurity>
)

@JsonClass(generateAdapter = true)
data class SocialSecurity(
    override val id: Int? = null,
    val year: Int,
    val percent: Double,
    val limit: Int,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)