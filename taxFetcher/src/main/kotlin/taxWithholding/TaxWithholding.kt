package taxWithholding

import PayPeriod
import com.squareup.moshi.JsonClass
import generics.GenericItem
import generics.IdTable

enum class TaxWithholdingTypes { General, NonResident }

object TaxWithholdings : IdTable() {
    val year = integer("year").primaryKey()
    val type = varchar("type", 255)
    val payPeriod = varchar("payPeriod", 255)
    val amount = double("amount")
}

@JsonClass(generateAdapter = true)
data class TaxWithholdingResponse(
    val taxWithholding: List<TaxWithholding>
)

@JsonClass(generateAdapter = true)
data class TaxWithholding(
    override val id: Int?,
    val year: Int,
    val type: TaxWithholdingTypes,
    val payPeriod: PayPeriod,
    val amount: Double,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)