package taxWithholding

import PayPeriod
import com.squareup.moshi.JsonClass
import generics.GenericItem

@JsonClass(generateAdapter = true)
data class TaxWithholding(
    override val id: Int? = null,
    val year: Int,
    val type: TaxWithholdingTypes,
    val payPeriod: PayPeriod,
    val amount: Double,
    override val dateCreated: Long = System.currentTimeMillis(),
    override val dateUpdated: Long = System.currentTimeMillis()
) : GenericItem
