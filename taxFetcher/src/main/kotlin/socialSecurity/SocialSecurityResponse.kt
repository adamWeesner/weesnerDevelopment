package socialSecurity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SocialSecurityResponse(
    val socialSecurity: List<SocialSecurity>
)