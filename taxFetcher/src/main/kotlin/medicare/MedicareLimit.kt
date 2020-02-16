package medicare

import MaritalStatus
import com.squareup.moshi.JsonClass
import generics.GenericItem

@JsonClass(generateAdapter = true)
data class MedicareLimit(
    override val id: Int? = null,
    val year: Int,
    val maritalStatus: MaritalStatus,
    val amount: Int,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem
