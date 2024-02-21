package io.eqoty.kryptools.aessiv

expect class AesSIV() {
    suspend fun encrypt(
        txEncryptionKey: UByteArray,
        plaintext: UByteArray,
        associatedData: List<UByteArray>
    ): UByteArray

    suspend fun decrypt(
        txEncryptionKey: UByteArray,
        ciphertext: UByteArray,
        associatedData: List<UByteArray>
    ): UByteArray
}