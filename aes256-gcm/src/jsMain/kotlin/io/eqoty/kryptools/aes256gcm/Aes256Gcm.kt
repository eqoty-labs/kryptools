package io.eqoty.kryptools.aes256gcm

actual class Aes256Gcm actual constructor() {
    actual suspend fun encrypt(
        iv: UByteArray,
        key: UByteArray,
        plaintext: UByteArray
    ): UByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun decryptAtIndexUnauthenticated(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray,
        offset: Int,
        hasTag: Boolean
    ): UByteArray {
        TODO("Not yet implemented")
    }

}