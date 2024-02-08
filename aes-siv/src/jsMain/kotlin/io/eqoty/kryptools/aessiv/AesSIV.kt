package io.eqoty.kryptools.aessiv

import io.eqoty.kryptools.utils.asUByteArray
import io.eqoty.kryptools.utils.asUInt8Array
import jslibs.neutrino.aes_128_siv_encrypt
import kotlinx.coroutines.await


actual class AesSIV {

    actual suspend fun encrypt(
        txEncryptionKey: UByteArray,
        plaintext: UByteArray,
        associatedData: UByteArray
    ): UByteArray {
        val key = txEncryptionKey.asUInt8Array()
        return aes_128_siv_encrypt(key, plaintext.asUInt8Array(), arrayOf(associatedData.asUInt8Array())).await()
            .asUByteArray()
    }

    actual suspend fun decrypt(
        txEncryptionKey: UByteArray,
        ciphertext: UByteArray,
        associatedData: UByteArray
    ): UByteArray {
        val key = txEncryptionKey.asUInt8Array()
        return jslibs.neutrino.aes_128_siv_decrypt(
            key,
            ciphertext.asUInt8Array(),
            arrayOf(associatedData.asUInt8Array())
        ).await()
            .asUByteArray()
    }
}