package io.eqoty.kryptools.aes256gcm

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


actual class Aes256Gcm actual constructor() {

    actual suspend fun encrypt(
        iv: UByteArray,
        key: UByteArray,
        plaintext: UByteArray
    ): UByteArray {
        val gcm: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec: SecretKey = SecretKeySpec(key.asByteArray(), "AES")
        val ivParam = GCMParameterSpec(TAG_SIZE_BITS, iv.asByteArray())
        gcm.init(Cipher.ENCRYPT_MODE, keySpec, ivParam)
        return gcm.doFinal(plaintext.asByteArray()).asUByteArray()
    }

    actual suspend fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray {
        val gcm = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmParameterSpec = GCMParameterSpec(TAG_SIZE_BITS, iv.asByteArray())
        val keySpec: SecretKey = SecretKeySpec(key.asByteArray(), "AES")
        gcm.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec)
        return gcm.doFinal(ciphertext.asByteArray()).asUByteArray()
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
     *
     * Note: this is only correct for a 12 byte IV in GCM mode
     */
    actual suspend fun decryptAtIndexUnauthenticated(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray,
        offset: Int,
        hasTag: Boolean
    ): UByteArray {
        val ctr = Cipher.getInstance("AES/CTR/NoPadding")
        val counter = getCounterBytes(iv, offset)

        val ctrIV = IvParameterSpec(counter.asByteArray())
        val keySpec: SecretKey = SecretKeySpec(key.asByteArray(), "AES")
        ctr.init(Cipher.DECRYPT_MODE, keySpec, ctrIV)
        val inputLen = if (hasTag) {
            ciphertext.size - TAG_SIZE_BYTES
        } else ciphertext.size

        return ctr.doFinal(ciphertext.asByteArray(), 0, inputLen).asUByteArray()
    }

}