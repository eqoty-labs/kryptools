package io.eqoty.kryptools.aes256gcm

internal const val TAG_SIZE_BITS = 128
internal const val TAG_SIZE_BYTES = 16

expect class Aes256Gcm() {

    /***
     * @param iv - initialization vector (should be of size 12)
     * @param key - the secret key (should be of size 16)
     * @param plaintext - the binary to encrypt
     */
    suspend fun encrypt(
        iv: UByteArray,
        key: UByteArray,
        plaintext: UByteArray
    ): UByteArray

    /***
     * @param iv - initialization vector (should be of size 12)
     * @param key - the secret key (should be of size 16)
     * @param ciphertext - the binary to decrypt
     */
    suspend fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray

    /***
     * @param iv - initialization vector (should be 12 bytes)
     * @param key - the secret key (should be 16 bytes)
     * @param ciphertext - the binary to decrypt
     * @param offset - the 16 byte offset to decrypt
     * @param hasTag - if true will ignore decrypting the tag bytes at the end of cyphertext (the last 16 bytes)
     *
     * Danger there be dragons: This does not authenticate the partially decrypted content. Only use
     * if you know what you are doing.
     */
    suspend fun decryptAtIndexUnauthenticated(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray,
        offset: Int,
        hasTag: Boolean = false
    ): UByteArray

}

fun Aes256Gcm.getCounterBytes(iv: UByteArray, offset: Int): UByteArray {
    // the GCM specification you can see that the IV for CTR is simply the IV, appended with four bytes 00000002
    // (i.e. a counter starting at zero, increased by one for calculating the authentication tag and again for the
    // starting value of the counter for encryption).
    // https://stackoverflow.com/a/49244840/1363742
    val ctrBytes = (2 + offset).toUByteArray()
    return iv + ctrBytes
}


internal fun Int.toUByteArray(): UByteArray {
    val bytes = UByteArray(4)
    (0..3).forEach { i -> bytes[bytes.size - 1 - i] = (this shr (i * 8)).toUByte() }
    return bytes
}