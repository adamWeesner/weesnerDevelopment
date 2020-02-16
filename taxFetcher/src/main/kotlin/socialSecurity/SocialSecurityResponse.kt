package socialSecurity

import com.squareup.moshi.JsonClass
import generics.GenericResponse

@JsonClass(generateAdapter = true)
data class SocialSecurityResponse(
    override var items: List<SocialSecurity>? = null
) : GenericResponse<SocialSecurity>(items)
