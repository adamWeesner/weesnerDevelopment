package medicare

import com.squareup.moshi.JsonClass
import generics.GenericItem

@JsonClass(generateAdapter = true)
data class Medicare(
    override val id: Int? = null,
    val year: Int,
    val percent: Double,
    val additionalPercent: Double,
    val limits: List<MedicareLimit>,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)

