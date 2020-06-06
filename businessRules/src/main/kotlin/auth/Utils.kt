package auth

import org.apache.commons.codec.binary.Base64
import shared.auth.HashedUser
import shared.auth.InvalidUserReason

/**
 * Verify data being passed for username and password are only made up of valid [Base64] string data.
 */
fun HashedUser.checkValidity(): InvalidUserReason? =
    if (!Base64.isBase64(username) || !Base64.isBase64(password)) InvalidUserReason.InvalidUserInfo
    else null
