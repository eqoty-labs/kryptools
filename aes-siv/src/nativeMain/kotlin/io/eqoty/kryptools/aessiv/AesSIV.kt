package io.eqoty.kryptools.aessiv

import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toCValues
import libaes_siv.*

actual class AesSIV actual constructor() {
    actual suspend fun encrypt(
        txEncryptionKey: UByteArray,
        plaintext: UByteArray,
        associatedData: List<UByteArray>
    ): UByteArray = memScoped {
        val ctx = AES_SIV_CTX_new()
        val outputLength = plaintext.size // For AES-SIV, output size is plaintext size + tag size.
        val tagSize = 16 // AES-SIV authentication tag size is 16 bytes
        val tagAndCiphertext = UByteArray(tagSize + outputLength)
        val tagPtr = tagAndCiphertext.refTo(0)
        val ciphertextPtr = if (outputLength == 0) null else tagAndCiphertext.refTo(tagSize)

        AES_SIV_Init(ctx, txEncryptionKey.toCValues(), txEncryptionKey.size.convert())

        associatedData.forEach {
            AES_SIV_AssociateData(ctx, it.toCValues(), it.size.convert())
        }
        val success = AES_SIV_EncryptFinal(
            ctx,
            tagPtr, ciphertextPtr,
            plaintext.toCValues(), plaintext.size.convert(),
        )
        if (success == 0) {
            throw Error("AES_SIV_Encrypt failed")
        }

        AES_SIV_CTX_free(ctx)
        return@memScoped tagAndCiphertext
    }


    actual suspend fun decrypt(
        txEncryptionKey: UByteArray,
        ciphertext: UByteArray,
        associatedData: List<UByteArray>
    ): UByteArray = memScoped {
        val ctx = AES_SIV_CTX_new()
        val tagSize = 16 // AES-SIV authentication tag size is 16 bytes
        val outputLength = ciphertext.size - tagSize // For AES-SIV, output size is plaintext size + tag size.
        if (outputLength == 0) {
            return@memScoped UByteArray(0)
        }
        val plaintext = UByteArray(outputLength)
        val tagPtr = ciphertext.refTo(0)
        val ciphertextPtr = ciphertext.refTo(tagSize)

        AES_SIV_Init(ctx, txEncryptionKey.toCValues(), txEncryptionKey.size.convert())

        associatedData.forEach {
            AES_SIV_AssociateData(ctx, it.toCValues(), it.size.convert())
        }

        val success = AES_SIV_DecryptFinal(
            ctx,
            plaintext.refTo(0),
            tagPtr, ciphertextPtr,
            outputLength.convert(),
        )
        if (success == 0) {
            throw Error("AES_SIV_Decrypt failed")
        }

        AES_SIV_CTX_free(ctx)
        return@memScoped plaintext
    }

}