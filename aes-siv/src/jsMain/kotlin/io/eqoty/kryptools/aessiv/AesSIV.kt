package io.eqoty.kryptools.aessiv

import io.eqoty.kryptools.utils.asUByteArray
import io.eqoty.kryptools.utils.asUInt8Array
import jslibs.tsstdlib.CryptoKey
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.experimental.and


actual class AesSIV {

    // splits an AES-128 SIV key
    private suspend fun split_siv_key(atu8_key: Uint8Array): Pair<CryptoKey, CryptoKey> {
        require(32 == atu8_key.byteLength) { "SIV key not 32 bytes" }

        // destructure halves
        val atu8_key_mac = atu8_key.subarray(0, atu8_key.length / 2)
        val atu8_key_ctr = atu8_key.subarray(atu8_key_mac.length, atu8_key.length)

        // import each key
        val d_key_cbc = aes_key(atu8_key_mac, "AES-CBC")
        val d_key_ctr = aes_key(atu8_key_ctr, "AES-CTR")

        // return pair as tuple
        return d_key_cbc to d_key_ctr
    };

    // moved to separate function since it saves 2 bytes after terser
    fun zero_iv(atu8_iv: Uint8Array) {
        atu8_iv[NB_AES_BLOCK - 8] = atu8_iv[NB_AES_BLOCK - 8] and 0x7f
        atu8_iv[NB_AES_BLOCK - 4] = atu8_iv[NB_AES_BLOCK - 4] and 0x7f
    };

    actual suspend fun encrypt(
        txEncryptionKey: UByteArray, plaintext: UByteArray, associatedData: List<UByteArray>
    ): UByteArray {
        val atu8_key = txEncryptionKey.asUInt8Array()
        val atu8_plaintext = plaintext.asUInt8Array()
        val a_ad = associatedData.map { it.asUInt8Array() }.toTypedArray()
//        val encrypted: Uint8Array = aes_128_siv_encrypt(key, plaintext.toUInt8Array(), array).await()

        // construct aes keys
        val (d_key_cbc, d_key_ctr) = split_siv_key(atu8_key)

        // prep payload
        val atu8_payload = Uint8Array(NB_AES_BLOCK + atu8_plaintext.byteLength)

        // V = S2V(K1, AD1, ..., ADn, P))
        val atu8_iv = s2v(d_key_cbc, atu8_plaintext, a_ad)

        // set tag into payload
        atu8_payload.set(atu8_iv, 0)

        // Q = V bitand (1^64 || 0^1 || 1^31 || 0^1 || 1^31)
        zero_iv(atu8_iv)

        // encrypt plaintext into payload
        atu8_payload.set(aesCtr(d_key_ctr, atu8_iv, atu8_plaintext), NB_AES_BLOCK)

        // return payload
        return atu8_payload.asUByteArray()
    }

    actual suspend fun decrypt(
        txEncryptionKey: UByteArray, ciphertext: UByteArray, associatedData: List<UByteArray>
    ): UByteArray {
        val atu8_key = txEncryptionKey.asUInt8Array()
        val atu8_payload = ciphertext.asUInt8Array()

        val a_ad = associatedData.map { it.asUInt8Array() }.toTypedArray()

        val (d_key_cbc, d_key_ctr) = split_siv_key(atu8_key)

        require(atu8_payload.byteLength >= NB_AES_BLOCK) { "SIV payload ${atu8_payload.byteLength} bytes < ${NB_AES_BLOCK} bytes" }

        // extract tag || ciphertext
        val atu8Tag = atu8_payload.subarray(0, NB_AES_BLOCK)
        val atu8_ciphertext = atu8_payload.subarray(NB_AES_BLOCK, atu8_payload.length)

        // copy tag to iv
        val atu8_iv = Uint8Array(atu8Tag.buffer.slice(0, atu8Tag.length))

        // zero-out top bits in last 32-bit words of iv
        zero_iv(atu8_iv)

        // decrypt ciphertext
        val atu8_plaintext = aesCtr(d_key_ctr, atu8_iv, atu8_ciphertext)

        // authenticate
        val atu8Cmac = s2v(d_key_cbc, atu8_plaintext, a_ad)

        // assert expected length
        require(!(atu8Cmac.length != NB_AES_BLOCK || atu8Tag.length != NB_AES_BLOCK)) { "Invalid tag/CMAC lengths" }

        // compare for equality
        var xbCmp = 0;
        for (ibEach in 0 until NB_AES_BLOCK) {
            xbCmp = xbCmp or (atu8Tag[ibEach].toInt() xor atu8Cmac[ibEach].toInt())
        }

        // not equal
        require(xbCmp != 1) {
            " SIV tag / CMAC mismatch; decoded:\n" +
//                    "${
//                        base64_to_text(/^([+a - z\d /]*)/i.exec(bytes_to_text(atu8_plaintext))![1])
//                    }" +
                    "\n\nentire plaintext:\n${atu8_plaintext.asUByteArray().asByteArray().decodeToString()}"
        }

        // plaintext
        return atu8_plaintext.asUByteArray()
    }
}