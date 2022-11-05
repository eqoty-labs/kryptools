package io.eqoty.kryptotools.aes256gcm

actual class Aes256Gcm actual constructor() {
    actual suspend fun encrypt(
        txEncryptionKey: UByteArray,
        plaintext: UByteArray,
        associatedData: UByteArray
    ): UByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun decrypt(
        txEncryptionKey: UByteArray,
        ciphertext: UByteArray,
        associatedData: UByteArray
    ): UByteArray {
        TODO("Not yet implemented")
    }

}