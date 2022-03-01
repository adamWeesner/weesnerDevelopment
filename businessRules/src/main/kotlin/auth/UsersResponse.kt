package auth

import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.GenericResponse
import kotlinx.serialization.Serializable

@Serializable
data class UsersResponse(
    override var items: List<User> = emptyList()
) : GenericResponse<User>
