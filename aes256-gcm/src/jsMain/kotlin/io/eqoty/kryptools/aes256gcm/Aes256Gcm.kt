package io.eqoty.kryptools.aes256gcm

import Crypto
import jslibs.tsstdlib.AesCtrParams
import jslibs.tsstdlib.AesGcmParams
import jslibs.tsstdlib.CryptoKey
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

private fun UByteArray.toUInt8Array(): Uint8Array = Uint8Array(asByteArray().unsafeCast<Int8Array>().buffer)
private fun Uint8Array.asByteArray(): ByteArray = this.unsafeCast<ByteArray>()
private fun ArrayBuffer.asUByteArray(): UByteArray = Int8Array(this).unsafeCast<ByteArray>().asUByteArray()

actual class Aes256Gcm {

    val crypto = Crypto()
    val cryptoKeysCtr = mutableMapOf<UByteArray, CryptoKey>()
    val cryptoKeysGcm = mutableMapOf<UByteArray, CryptoKey>()


    /***
    Import an AES secret key from an ArrayBuffer containing the raw bytes.
    Takes an ArrayBuffer string containing the bytes, and returns a Promise
    that will resolve to a CryptoKey representing the secret key.
     */
    fun importSecretKey(crypto: Crypto, rawKey: UByteArray, algorithm: String): Promise<CryptoKey> {
        return crypto.subtle.importKey(
            "raw",
            rawKey.toUInt8Array(),
            algorithm,
            true,
            arrayOf("encrypt", "decrypt")
        )
    }


    actual suspend fun encrypt(
        iv: UByteArray,
        key: UByteArray,
        plaintext: UByteArray
    ): UByteArray {
        // A stupid hacky workaround for creating js object instances
        // https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
        val jsparams: dynamic = Unit
        jsparams.name = "AES-GCM"
        jsparams.iv = iv.toUInt8Array()
        jsparams.tagLength = TAG_SIZE_BITS

        val cryptoKey = cryptoKeysGcm.getOrPut(key) { importSecretKey(crypto, key, "AES-GCM").await() }
        val encryptedBuffer = crypto.subtle.encrypt(
            jsparams as AesGcmParams,
            cryptoKey,
            plaintext.toUInt8Array()
        ).await()
        return encryptedBuffer.asUByteArray()
    }

    actual suspend fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray {
        // A stupid hacky workaround for creating js object instances
        // https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
        val jsparams: dynamic = Unit
        jsparams.name = "AES-GCM"
        jsparams.iv = iv.toUInt8Array()
        jsparams.tagLength = TAG_SIZE_BITS

        val cryptoKey = cryptoKeysGcm.getOrPut(key) { importSecretKey(crypto, key, "AES-GCM").await() }
        val plaintextBuffer = crypto.subtle.decrypt(
            jsparams as AesGcmParams,
            cryptoKey,
            ciphertext.toUInt8Array()
        ).await()
        return plaintextBuffer.asUByteArray()
    }

    actual suspend fun decryptAtIndexUnauthenticated(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray,
        offset: Int,
        hasTag: Boolean
    ): UByteArray {
        val counter = getCounterBytes(iv, offset)
        val inputLen = if (hasTag) {
            (ciphertext.size - TAG_SIZE_BYTES) * 8
        } else ciphertext.size
        // A stupid hacky workaround for creating js object instances
        // https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
        val jsparams: dynamic = Unit
        jsparams.name = "AES-CTR"
        jsparams.counter = counter.toUInt8Array()
        jsparams.length = inputLen

        val cryptoKey = cryptoKeysCtr.getOrPut(key) { importSecretKey(crypto, key, "AES-CTR").await() }
        val plaintextBuffer = crypto.subtle.decrypt(
            jsparams as AesCtrParams,
            cryptoKey,
            ciphertext.toUInt8Array()
        ).await()
        return plaintextBuffer.asUByteArray()
    }
}