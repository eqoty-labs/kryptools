package io.eqoty.kryptools

import okio.ByteString.Companion.toByteString
import kotlin.math.roundToInt

const val crypto_auth_BYTES = 32
const val crypto_auth_KEYBYTES = 32

sealed class HKDFError(override val message: String) : Error()
class HKDFInvalidSaltError : HKDFError("Salt length must match exactly the key length of the HMAC function.")
class HKDFInvalidLenError : HKDFError("Length of len must not be larger than 255 times the length of the hash output.")
class HMACCalculationFailedError(message: String) : HKDFError("HMAC calculation failed: $message")


/**
This function calculates a key using a HKDF (RFC 5869) which uses HMAC-SHA-512/256.
- Parameters:
- ikm: Input keying material
- salt: A nonce used to seed the HKDF, which must be 32 bytes long, if provided (optional)
- info: Context and application specific information (optional)
- L: Length of the output keying material in bytes, must not be larger than 255 * 32 bytes
- Returns: Output keying material of length L bytes
 */
fun deriveHKDFKey(ikm: UByteArray, _salt: UByteArray? = null, info: String = "", len: Int): UByteArray {
    val hashOutputLength = crypto_auth_BYTES
    val salt = _salt ?: UByteArray(hashOutputLength) { 0.toUByte() }

    if (len > 255 * hashOutputLength) {
        throw HKDFInvalidLenError()
    }
    if (salt.size != crypto_auth_KEYBYTES) {
        throw HKDFInvalidSaltError()
    }

    // Step 1: Extract
    val prk = try {
        ikm.asByteArray().toByteString().hmacSha256(salt.asByteArray().toByteString())
    } catch (t: Throwable) {
        throw HMACCalculationFailedError(t.message!!)
    }

    // Step 2: Expand
    val N = (len.toDouble() / hashOutputLength.toDouble()).roundToInt()
    val T = arrayListOf<UByte>()

    var lastTi = ubyteArrayOf()
    for (i in 1..N) {
        val message = arrayListOf<UByte>()
        message.addAll(lastTi)
        message.addAll(info.encodeToByteArray().asUByteArray())
        message.add(i.toUByte())

        val currentTi = try {
            message.toUByteArray().asByteString().hmacSha256(prk).toUByteArray()
        } catch (t: Throwable) {
            throw HMACCalculationFailedError(t.message!!)
        }
        T.addAll(currentTi)
        lastTi = currentTi
    }

    return T.subList(0, len).toUByteArray()
}