package federalIncomeTax

import MaritalStatus
import PayPeriod
import com.squareup.moshi.JsonClass
import generics.GenericItem

@JsonClass(generateAdapter = true)
data class FederalIncomeTaxResponse(
    val federalIncomeTax: List<FederalIncomeTax>
)

@JsonClass(generateAdapter = true)
data class FederalIncomeTax(
    override val id: Int?,
    val year: Int,
    val maritalStatus: MaritalStatus,
    val payPeriod: PayPeriod,
    val over: Double,
    val notOver: Double,
    val plus: Double,
    val percent: Double,
    val nonTaxable: Double,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem(id, dateCreated, dateUpdated)