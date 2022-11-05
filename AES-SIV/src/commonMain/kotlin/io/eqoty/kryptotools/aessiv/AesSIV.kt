package io.eqoty.kryptotools.aessiv

expect class AesSIV() {
    suspend fun encrypt(
        txEncryptionKey: UByteArray,
        plaintext: UByteArray,
        associatedData: UByteArray
    ): UByteArray

    suspend fun decrypt(
        txEncryptionKey: UByteArray,
        ciphertext: UByteArray,
        associatedData: UByteArray
    ): UByteArray
}