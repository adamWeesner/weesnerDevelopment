package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import shared.auth.HashedUser
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class JwtProvider(
    private val issuer: String,
    private val audience: String,
    private val validFor: Long,
    private val cipher: auth.Cipher
) {
    /**
     * Builds a [JWTVerifier] with the given [issuer].
     */
    val verifier: JWTVerifier = JWT
        .require(cipher.algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Decodes the given [token].
     */
    fun decodeJWT(token: String): DecodedJWT = JWT.require(cipher.algorithm).build().verify(token)

    /**
     * Create a JWT token for the given [hashedUser].
     */
    fun createJWT(hashedUser: HashedUser): String? = JWT.create()
        .withIssuedAt(Date())
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("attr-username", hashedUser.username)
        .withClaim("attr-password", hashedUser.password)
        .withExpiresAt(Date(System.currentTimeMillis() + validFor)).sign(cipher.algorithm)
}

/**
 * @author Fidel Nunez Kanut
 * two way symmetric encryption using 256 bit key + "AES/GCM/NoPadding"
 * [https://github.com/fnunezkanut/kotlin-symmetric-encryption-example/blob/master/src/main/kotlin/com/github/fnunezkanut/SymmetricEncryption.kt]
 */
class SymmetricEncryption {
    //symmetric encryption constants for 256 bit AES/GCM/NoPadding
    private val algorithem: String = "AES/GCM/NoPadding"
    private val nonceSize: Int = 12
    private val tagSize: Int = 128
    private val keySize: Int = 256
    private val pbkdf2Name: String = "PBKDF2WithHmacSHA256"
    private val saltSize: Int = 16
    private val iterations = 32767

    fun encrypt(data: String, secret: String): String {

        //generate a salt using a CSPRNG
        val secureRandom = SecureRandom()
        val salt = ByteArray(saltSize)
        secureRandom.nextBytes(salt)

        //create an instance of PBKDF2 and derive a key.
        val pwSpec = PBEKeySpec(secret.toCharArray(), salt, iterations, keySize)
        val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(pbkdf2Name)
        val key: ByteArray = keyFactory.generateSecret(pwSpec).encoded

        //encrypt and prepend salt.
        val dataWNonce: ByteArray = encryptByteArray(data.toByteArray(StandardCharsets.UTF_8), key)
        val dataWNonceAndSalt = ByteArray(salt.size + dataWNonce.size)
        System.arraycopy(salt, 0, dataWNonceAndSalt, 0, salt.size)
        System.arraycopy(dataWNonce, 0, dataWNonceAndSalt, salt.size, dataWNonce.size)

        //ensure data is properly encoded
        return Base64.getEncoder().encodeToString(dataWNonceAndSalt)
    }

    fun decrypt(data: String, secret: String): String {
        //decode from base64
        val decodedData: ByteArray = Base64.getDecoder().decode(data)

        //retrieve the salt and ciphertextAndNonce.
        val salt = ByteArray(saltSize)
        val decodedDataWNonce = ByteArray(decodedData.size - saltSize)
        System.arraycopy(decodedData, 0, salt, 0, salt.size)
        System.arraycopy(decodedData, salt.size, decodedDataWNonce, 0, decodedDataWNonce.size)

        //create an instance of PBKDF2 and derive the key.
        val pwSpec = PBEKeySpec(secret.toCharArray(), salt, iterations, keySize)
        val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(pbkdf2Name)
        val key: ByteArray = keyFactory.generateSecret(pwSpec).encoded

        //decrypt and return result.
        return String(decryptByteArray(decodedDataWNonce, key))
    }

    private fun encryptByteArray(data: ByteArray, key: ByteArray): ByteArray {
        //generate a 96-bit nonce using a CSPRNG.
        val secureRandom = SecureRandom()
        val nonce = ByteArray(nonceSize)
        secureRandom.nextBytes(nonce)

        //create the cipher instance and initialize.
        val cipher: Cipher = Cipher.getInstance(algorithem)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(tagSize, nonce))

        //encrypt and prepend nonce.
        val encryptedData: ByteArray = cipher.doFinal(data)
        val encryptedDataWNonce = ByteArray(nonce.size + encryptedData.size)
        System.arraycopy(nonce, 0, encryptedDataWNonce, 0, nonce.size)
        System.arraycopy(encryptedData, 0, encryptedDataWNonce, nonce.size, encryptedData.size)

        return encryptedDataWNonce
    }

    private fun decryptByteArray(data: ByteArray, key: ByteArray): ByteArray {
        //retrieve the nonce and ciphertext.
        val nonce = ByteArray(nonceSize)
        val dataBytes = ByteArray(data.size - nonceSize)
        System.arraycopy(data, 0, nonce, 0, nonce.size)
        System.arraycopy(data, nonce.size, dataBytes, 0, dataBytes.size)

        //create the cipher instance and initialize.
        val cipher: Cipher = Cipher.getInstance(algorithem)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(tagSize, nonce))

        //decrypt and return result.
        return cipher.doFinal(dataBytes)
    }
}

class Cipher(
    secret: String
) {
    val algorithm = Algorithm.HMAC256(secret)

    fun encrypt(data: String?): ByteArray = algorithm.sign(data?.toByteArray())
}