package io.eqoty.kryptools.aes256gcm

actual class Aes256Gcm actual constructor() {
    suspend actual fun encrypt(
        iv: UByteArray,
        key: UByteArray,
        plaintext: UByteArray
    ): UByteArray {
        TODO("Not yet implemented")
    }

    suspend actual fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray {
        TODO("Not yet implemented")
    }

    suspend actual fun decryptAtIndexUnauthenticated(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray,
        offset: Int,
        hasTag: Boolean
    ): UByteArray {
        TODO("Not yet implemented")
    }

}