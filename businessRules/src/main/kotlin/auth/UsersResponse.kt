package auth

import com.squareup.moshi.JsonClass
import shared.auth.User
import shared.base.GenericResponse

@JsonClass(generateAdapter = true)
data class UsersResponse(
    override var items: List<User>? = null
) : GenericResponse<User>
