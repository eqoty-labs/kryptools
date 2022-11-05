package io.eqoty.kryptotools.aes256gcm

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val TAG_SIZE_BITS = 128
private const val TAG_SIZE_BYTES = 16

actual class Aes256Gcm actual constructor() {

    actual fun encrypt(
        iv: UByteArray,
        key: UByteArray,
        plaintext: UByteArray
    ): UByteArray {
        val gcm: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
        val ivParam = GCMParameterSpec(TAG_SIZE_BITS, iv.toByteArray())
        gcm.init(Cipher.ENCRYPT_MODE, keySpec, ivParam)
        return gcm.doFinal(plaintext.toByteArray()).toUByteArray()
    }

    actual fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray {
        val gcm = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmParameterSpec = GCMParameterSpec(TAG_SIZE_BITS, iv.toByteArray())
        val keySpec: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
        gcm.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec)
        return gcm.doFinal(ciphertext.toByteArray()).toUByteArray()
    }

    fun Int.toUByteArray(): UByteArray {
        val bytes = UByteArray(4)
        (0..3).forEach { i -> bytes[bytes.size - 1 - i] = (this shr (i * 8)).toUByte() }
        return bytes
    }

    /***
     * @param iv - initialization vector (should be 12 bytes)
     * @param key - the secret key (should be 16 bytes)
     * @param ciphertext - the binary to decrypt
     * @param offset - the 16 byte offset to decrypt
     * @param length - the number of 16 byte blocks to decrypt at offset
     *
     * Danger: This does not authenticate the partially decrypted content. Only use
     * if you know what you are doing.
     */
    actual fun decryptAtIndexUnauthenticated(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray,
        offset: Int,
        hasTag: Boolean
    ): UByteArray {
        val ctr = Cipher.getInstance("AES/CTR/NoPadding")
        // WARNING: this is only correct for a 12 byte IV in GCM mode

        val counter = iv + UByteArray(4)

        // the GCM specification you can see that the IV for CTR is simply the IV, appended with four bytes 00000002
        // (i.e. a counter starting at zero, increased by one for calculating the authentication tag and again for the
        // starting value of the counter for encryption).
        // https://stackoverflow.com/a/49244840/1363742
        val ctrBytes = (2 + offset).toUByteArray()
        counter[counter.size - 4] = ctrBytes[0]
        counter[counter.size - 3] = ctrBytes[1]
        counter[counter.size - 2] = ctrBytes[2]
        counter[counter.size - 1] = ctrBytes[3]

        val ctrIV = IvParameterSpec(counter.toByteArray())
        val keySpec: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
        ctr.init(Cipher.DECRYPT_MODE, keySpec, ctrIV)
        val inputLen = if (hasTag) {
            ciphertext.size - TAG_SIZE_BYTES
        } else ciphertext.size

        return ctr.doFinal(ciphertext.toByteArray(), 0, inputLen).toUByteArray()
    }

}