package io.eqoty.kryptools.aes256gcm

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