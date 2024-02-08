package io.eqoty.kryptools.aessiv

import io.eqoty.kryptools.utils.toUByteArray
import io.eqoty.kryptools.utils.toUInt8Array
import jslibs.neutrino.aes_128_siv_decrypt
import jslibs.neutrino.aes_128_siv_encrypt
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array


actual class AesSIV {

    actual suspend fun encrypt(
        txEncryptionKey: UByteArray,
        plaintext: UByteArray,
        associatedData: UByteArray
    ): UByteArray {
        val key = txEncryptionKey.toUInt8Array()
        val array = JsArray<Uint8Array>()
        array[0] = associatedData.toUInt8Array()
        val encrypted: Uint8Array = aes_128_siv_encrypt(key, plaintext.toUInt8Array(), array).await()
        return encrypted.toUByteArray()
    }

    actual suspend fun decrypt(
        txEncryptionKey: UByteArray,
        ciphertext: UByteArray,
        associatedData: UByteArray
    ): UByteArray {
        val key = txEncryptionKey.toUInt8Array()
        val array = JsArray<Uint8Array>()
        array[0] = associatedData.toUInt8Array()
        val decrypted: Uint8Array = aes_128_siv_decrypt(key, ciphertext.toUInt8Array(), array).await()
        return decrypted.toUByteArray()
    }
}