package medicare

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicareResponse(
    val medicare: List<Medicare>
)