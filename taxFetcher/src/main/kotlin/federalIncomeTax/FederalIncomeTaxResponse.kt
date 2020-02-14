package federalIncomeTax

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FederalIncomeTaxResponse(
    val federalIncomeTax: List<FederalIncomeTax>
)