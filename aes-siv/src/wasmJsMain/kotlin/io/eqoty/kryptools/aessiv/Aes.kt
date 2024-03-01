package io.eqoty.kryptools.aessiv

import jslibs.tsstdlib.AesCbcParams
import jslibs.tsstdlib.AesCtrParams
import jslibs.tsstdlib.Crypto
import jslibs.tsstdlib.CryptoKey
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.experimental.xor
import kotlin.math.ceil

const val NB_AES_BLOCK = 16

val ATU8_ZERO_BLOCK get() = Uint8Array(NB_AES_BLOCK)

// https://github.com/whyoleg/cryptography-kotlin/blob/d524143a0719e6926b0ae190977a7341673fa718/cryptography-random/src/wasmJsMain/kotlin/CryptographyRandom.wasmJs.kt#L40-L54
//language=JavaScript
private fun getCrypto(): Crypto {
    js(
        code = """
    
        var isNodeJs = typeof process !== 'undefined' && process.versions != null && process.versions.node != null
        if (isNodeJs) {
            return (eval('require')('node:crypto').webcrypto);
        } else {
            return (window ? (window.crypto ? window.crypto : window.msCrypto) : self.crypto);
        }
    
               """
    )
}

// language=JavaScript
fun createAesCbcParams(iv: Uint8Array): AesCbcParams =
    js("({ name: 'AES-CBC', iv: iv})")

// language=JavaScript
fun createAesCtrParams(atu8_iv: Uint8Array, length: Int): AesCtrParams =
    js("({ name: 'AES-CTR', counter: atu8_iv, length: length})")


suspend fun aes_key(atu8_key: Uint8Array, si_algo: String): CryptoKey {
    val crypto = getCrypto()
    val keyUsages = JsArray<JsString>()
    keyUsages[0] = "encrypt".toJsString()
    return crypto.subtle.importKey("raw", atu8_key, si_algo, false, keyUsages).await()
}

// perform AES-CBC
suspend fun aesCbc(d_key_cbc: CryptoKey, atu8_data: Uint8Array): Uint8Array {
    val crypto = getCrypto()
    val d_cipher = Uint8Array(
        crypto.subtle.encrypt(
            createAesCbcParams(ATU8_ZERO_BLOCK).unsafeCast<AesCbcParams>(), d_key_cbc, atu8_data
        ).await(), 0, NB_AES_BLOCK
    )
    return d_cipher
}

// perform AES-CTR
suspend fun aesCtr(d_key_ctr: CryptoKey, atu8_iv: Uint8Array, atu8_data: Uint8Array): Uint8Array {
    val crypto = getCrypto()
    val d_cipher: ArrayBuffer = crypto.subtle.encrypt(
        createAesCtrParams(atu8_iv, NB_AES_BLOCK).unsafeCast<AesCtrParams>(), d_key_ctr, atu8_data
    ).await()
    return Uint8Array(d_cipher)
}


// pseudo-constant-time select
fun select(xbValue: Int, xbRif1: Int, xbRif0: Int): Int = ((xbValue - 1).inv() and xbRif1) or ((xbValue - 1) and xbRif0)

// double block value in-place
fun doubleBlock(atu8Block: Uint8Array) {
    var xbCarry = 0

    for (ibEach in NB_AES_BLOCK - 1 downTo 0) {
        val xbTmp = ((atu8Block[ibEach].toInt() and 0xff) ushr 7) and 0xff
        atu8Block[ibEach] = (((atu8Block[ibEach].toInt() and 0xff) shl 1) or xbCarry).toByte()
        xbCarry = xbTmp
    }

    atu8Block[NB_AES_BLOCK - 1] = ((atu8Block[NB_AES_BLOCK - 1].toInt() and 0xff) xor select(xbCarry, 0x87, 0)).toByte()
    @Suppress("UNUSED_VALUE")
    xbCarry = 0
}

fun xorBuffers(atu8A: Uint8Array, atu8B: Uint8Array) {
    for (ibEach in 0 until atu8B.length) {
        atu8A[ibEach] = (atu8A[ibEach].toInt() xor atu8B[ibEach].toInt()).toByte()
    }
}

// Assuming aesCbc, doubleBlock, xorBuffers are defined similarly as suspend functions or adapted Kotlin/JS functions
suspend fun aesCmacInit(dKeyMac: CryptoKey): suspend (atu8_data: Uint8Array) -> Uint8Array {
    // k1 subkey generation
    val atu8K1 = aesCbc(dKeyMac, ATU8_ZERO_BLOCK)
    doubleBlock(atu8K1)

    // k2 subkey generation
    val atu8K2 = Uint8Array(atu8K1.buffer.slice(0, atu8K1.length))
    doubleBlock(atu8K2)

    // Return a suspend function for CMAC computation
    return { atu8Data ->
        // cache data byte count
        val nbData = atu8Data.byteLength

        // number of blocks needed
        val nlBlocks = ceil(nbData.toDouble() / NB_AES_BLOCK.toDouble()).toInt()

        val atu8DataStart = if (nlBlocks == 0) 1 else nlBlocks

        // last block
        val atu8Last = Uint8Array(NB_AES_BLOCK)
        atu8Last.set(atu8Data.subarray((atu8DataStart - 1) * NB_AES_BLOCK, atu8Data.length))

        // cache size of last block
        val nbLast = nbData % NB_AES_BLOCK

        // last block requires padding
        if (nbLast > 0 || nlBlocks == 0) {
            // M_last := {ANS} XOR K2
            xorBuffers(atu8Last, atu8K2)

            // padding(M_n)
            atu8Last[nbLast] = atu8Last[nbLast] xor 0x80.toByte()
        }
        // no padding needed; xor with k1
        else {
            // M_last := M_n XOR K1
            xorBuffers(atu8Last, atu8K1)
        }

        var atu8Block = ATU8_ZERO_BLOCK

        // for i := 1 to n-1
        for (iBlock in 0 until nlBlocks - 1) {
            // Y := X XOR M_i
            xorBuffers(atu8Block, atu8Data.subarray(iBlock * NB_AES_BLOCK, atu8Data.length))

            // X := AES-128(K,Y)
            atu8Block = aesCbc(dKeyMac, atu8Block)
        }

        // Y := M_last XOR X
        xorBuffers(atu8Block, atu8Last)

        // T := AES-128(K,Y)
        aesCbc(dKeyMac, atu8Block)
    }
}

// performs S2V operation
suspend fun s2v(
    d_key_rkd: CryptoKey, atu8_plaintext: Uint8Array, a_ad: Array<Uint8Array> = arrayOf(Uint8Array(0))
): Uint8Array {
    val f_cmac = aesCmacInit(d_key_rkd)

    // D = AES-CMAC(K, <zero>)
    var atu8_cmac = f_cmac(ATU8_ZERO_BLOCK)

    // for i = 1 to n-1
    a_ad.forEach { atu8_ad ->
        // dbl(D)
        doubleBlock(atu8_cmac)

        // D = {ANS} xor AES-CMAC(K, Si)
        xorBuffers(atu8_cmac, f_cmac(atu8_ad))
    }

    // cache plaintext byte count
    val nb_plaintext = atu8_plaintext.byteLength

    // last block of plaintext
    val atu8_sn = Uint8Array(NB_AES_BLOCK)

    // if len(Sn) >= 128
    if (nb_plaintext >= NB_AES_BLOCK) {
        // Sn_end xor D
        atu8_sn.set(atu8_plaintext.subarray(nb_plaintext - NB_AES_BLOCK, atu8_plaintext.length))
        xorBuffers(atu8_sn, atu8_cmac)

        // T = Sn xorend D
        atu8_cmac = Uint8Array(atu8_plaintext.buffer.slice(0, atu8_plaintext.length))
        atu8_cmac.set(atu8_sn, nb_plaintext - NB_AES_BLOCK)
    } else {
        // dbl(D)
        doubleBlock(atu8_cmac)

        // pad(Sn)
        atu8_sn.set(atu8_plaintext)
        atu8_sn[nb_plaintext] = 0x80.toByte()

        // T = dbl(D) xor pad(Sn)
        xorBuffers(atu8_cmac, atu8_sn)
    }

    // V = AES-CMAC(K, T)
    return f_cmac(atu8_cmac)
}
