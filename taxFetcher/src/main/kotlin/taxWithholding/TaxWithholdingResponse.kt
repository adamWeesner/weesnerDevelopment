package taxWithholding

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaxWithholdingResponse(
    val taxWithholding: List<TaxWithholding>
)