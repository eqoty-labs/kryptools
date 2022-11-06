package io.eqoty.kryptools.aes256gcm

import Crypto
import jslibs.tsstdlib.AesCtrParams
import jslibs.tsstdlib.AesGcmParams
import jslibs.tsstdlib.CryptoKey
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import kotlin.js.Promise

private fun UByteArray.toUInt8Array(): Uint8Array = Uint8Array(toByteArray().toTypedArray())

// https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
@Serializable
private data class AesGcmParamsImpl(
    var additionalData: ByteArray? = null,
    var iv: ByteArray,
    var tagLength: Int?,
    var name: String
) {
    constructor(
        additionalData: UByteArray? = null,
        iv: UByteArray,
        tagLength: Int?,
        name: String
    ) : this(
        additionalData?.toByteArray(),
        iv.toByteArray(),
        tagLength,
        name
    )
}

@Serializable
private data class AesCtrParamsImpl(
    var counter: ByteArray,
    var length: Long,
    var name: String
) {
    constructor(
        counter: UByteArray,
        length: Long,
        name: String
    ) : this(
        counter.toByteArray(),
        length,
        name
    )
}


actual class Aes256Gcm {

    val crypto = Crypto()

    private fun Uint8Array.toUByteArray(): UByteArray {
        if (length.asDynamic() == undefined) {
            println("Error")
        }
        val result = UByteArray(length)
        for (i in 0 until length) {
            result[i] = get(i).toUByte()
        }

        return result
    }


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
        val params = AesGcmParamsImpl(
            name = "AES-GCM",
            iv = iv,
            tagLength = TAG_SIZE_BITS
        )

        // A stupid hacky workaround for creating js object instances
        // https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
        val jsparams = JSON.parse<AesGcmParams>(Json.encodeToString(params))
        // JSON.parse doesn't set the type of iv or additionalData data as Uint8Array,
        // it is just a normal array of Ints. So wrap them manually
        jsparams.iv = Uint8Array(jsparams.iv)
        if (jsparams.additionalData != undefined || jsparams.additionalData != null) {
            jsparams.additionalData = Uint8Array(jsparams.additionalData!!)
        }

        val encryptedBuffer = crypto.subtle.encrypt(
            jsparams,
            importSecretKey(crypto, key, "AES-GCM").await(),
            plaintext.toUInt8Array()
        ).await()
        return Uint8Array(encryptedBuffer).toUByteArray()
    }

    actual suspend fun decrypt(
        iv: UByteArray,
        key: UByteArray,
        ciphertext: UByteArray
    ): UByteArray {
        val params = AesGcmParamsImpl(
            name = "AES-GCM",
            iv = iv,
            tagLength = TAG_SIZE_BITS
        )

        // A stupid hacky workaround for creating js object instances
        // https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
        val jsparams = JSON.parse<AesGcmParams>(Json.encodeToString(params))
        // JSON.parse doesn't set the type of iv or additionalData data as Uint8Array,
        // it is just a normal array of Ints. So wrap them manually
        jsparams.iv = Uint8Array(jsparams.iv)
        if (jsparams.additionalData != undefined || jsparams.additionalData != null) {
            jsparams.additionalData = Uint8Array(jsparams.additionalData!!)
        }

        val plaintextBuffer = crypto.subtle.decrypt(
            jsparams,
            importSecretKey(crypto, key, "AES-GCM").await(),
            ciphertext.toUInt8Array()
        ).await()
        return Uint8Array(plaintextBuffer).toUByteArray()
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
        val params = AesCtrParamsImpl(
            name = "AES-CTR",
            counter = counter,
            length = inputLen.toLong()
        )

        // A stupid hacky workaround for creating js object instances
        // https://youtrack.jetbrains.com/issue/KT-44944/KJS-Non-mangled-types 🙃
        val jsparams = JSON.parse<AesCtrParams>(Json.encodeToString(params))
        // JSON.parse doesn't set the type of counter data as Uint8Array,
        // it is just a normal array of Ints. So wrap them manually
        jsparams.counter = Uint8Array(jsparams.counter)


        val plaintextBuffer = crypto.subtle.decrypt(
            jsparams,
            importSecretKey(crypto, key, "AES-CTR").await(),
            ciphertext.toUInt8Array()
        ).await()
        return Uint8Array(plaintextBuffer).toUByteArray()
    }

}