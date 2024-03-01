package io.eqoty.kryptools.aessiv

import dev.whyoleg.cryptography.algorithms.symmetric.AES

actual class AesSIV {

    // splits an AES-128 SIV key
    private suspend fun split_siv_key(atu8_key: UByteArray): Pair<AES.CBC.Key, AES.CTR.Key> {
        require(32 == atu8_key.size) { "SIV key not 32 bytes" }

        // destructure halves
        val atu8_key_mac = atu8_key.sliceArray(0 until atu8_key.size / 2)
        val atu8_key_ctr = atu8_key.sliceArray(atu8_key_mac.size until atu8_key.size)

        // import each key
        val d_key_cbc = aes_cbc_key(atu8_key_mac)
        val d_key_ctr = aes_ctr_key(atu8_key_ctr)

        // return pair as tuple
        return d_key_cbc to d_key_ctr
    };

    // moved to separate function since it saves 2 bytes after terser
    fun zero_iv(atu8_iv: UByteArray) {
        atu8_iv[NB_AES_BLOCK - 8] = atu8_iv[NB_AES_BLOCK - 8] and 0x7fu
        atu8_iv[NB_AES_BLOCK - 4] = atu8_iv[NB_AES_BLOCK - 4] and 0x7fu
    };

    actual suspend fun encrypt(
        txEncryptionKey: UByteArray, plaintext: UByteArray, associatedData: List<UByteArray>
    ): UByteArray {
        val atu8_key = txEncryptionKey
        val atu8_plaintext = plaintext
        val a_ad = associatedData.toTypedArray()

        // construct aes keys
        val (d_key_cbc, d_key_ctr) = split_siv_key(atu8_key)

        // prep payload
        val atu8_payload = UByteArray(NB_AES_BLOCK + atu8_plaintext.size)

        // V = S2V(K1, AD1, ..., ADn, P))
        val atu8_iv = s2v(d_key_cbc, atu8_plaintext, a_ad)

        // set tag into payload
        atu8_iv.copyInto(atu8_payload)

        // Q = V bitand (1^64 || 0^1 || 1^31 || 0^1 || 1^31)
        zero_iv(atu8_iv)

        // encrypt plaintext into payload
        aesCtr(d_key_ctr, atu8_iv, atu8_plaintext).copyInto(atu8_payload, NB_AES_BLOCK)

        // return payload
        return atu8_payload.toUByteArray()
    }

    actual suspend fun decrypt(
        txEncryptionKey: UByteArray, ciphertext: UByteArray, associatedData: List<UByteArray>
    ): UByteArray {
        val atu8_key = txEncryptionKey
        val atu8_payload = ciphertext

        val a_ad = associatedData.toTypedArray()

        val (d_key_cbc, d_key_ctr) = split_siv_key(atu8_key)

        require(atu8_payload.size >= NB_AES_BLOCK) { "SIV payload ${atu8_payload.size} bytes < ${NB_AES_BLOCK} bytes" }

        // extract tag || ciphertext
        val atu8Tag = atu8_payload.sliceArray(0 until NB_AES_BLOCK)
        val atu8_ciphertext = atu8_payload.sliceArray(NB_AES_BLOCK until atu8_payload.size)

        // copy tag to iv
        val atu8_iv = atu8Tag.sliceArray(0 until atu8Tag.size)

        // zero-out top bits in last 32-bit words of iv
        zero_iv(atu8_iv)

        // decrypt ciphertext
        val atu8_plaintext = aesCtr(d_key_ctr, atu8_iv, atu8_ciphertext)

        // authenticate
        val atu8Cmac = s2v(d_key_cbc, atu8_plaintext, a_ad)

        // assert expected length
        require(!(atu8Cmac.size != NB_AES_BLOCK || atu8Tag.size != NB_AES_BLOCK)) { "Invalid tag/CMAC lengths" }

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
                    "\n\nentire plaintext:\n${atu8_plaintext.toUByteArray().asByteArray().decodeToString()}"
        }

        // plaintext
        return atu8_plaintext.toUByteArray()
    }
}