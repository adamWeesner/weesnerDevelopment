package medicare

import MaritalStatus
import com.squareup.moshi.JsonClass
import generics.GenericItem
import generics.IdTable

object Medicares : IdTable() {
    val year = integer("year").primaryKey()
    val percent = double("percent")
    val additionalPercent = double("additionalPercent")
}

object MedicareLimits : IdTable() {
    val year = (integer("year") references Medicares.year)
    val maritalStatus = varchar("maritalStatus", 255)
    val amount = integer("amount")
}

@JsonClass(generateAdapter = true)
data class MedicareResponse(
    val medicare: List<Medicare>
)

@JsonClass(generateAdapter = true)
data class Medicare(
    override val id: Int?,
    val year: Int,
    val percent: Double,
    val additionalPercent: Double,
    val limits: List<MedicareLimit>,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)

@JsonClass(generateAdapter = true)
data class MedicareLimit(
    override val id: Int?,
    val year: Int,
    val maritalStatus: MaritalStatus,
    val amount: Int,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)