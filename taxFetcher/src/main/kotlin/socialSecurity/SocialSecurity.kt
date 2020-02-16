package socialSecurity

import com.squareup.moshi.JsonClass
import generics.GenericItem

@JsonClass(generateAdapter = true)
data class SocialSecurity(
    override val id: Int? = null,
    val year: Int,
    val percent: Double,
    val limit: Int,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)
