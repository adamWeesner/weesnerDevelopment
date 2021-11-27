package auth

import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import org.apache.commons.codec.binary.Base64

/**
 * Verify data being passed for username and password are only made up of valid [Base64] string data.
 */
fun HashedUser.checkValidity(): InvalidUserReason? =
    if (!Base64.isBase64(username) || !Base64.isBase64(password)) InvalidUserReason.InvalidUserInfo
    else null
