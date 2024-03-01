package io.eqoty.kryptools.aessiv

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import kotlin.math.ceil

const val NB_AES_BLOCK = 16

val ATU8_ZERO_BLOCK get() = UByteArray(NB_AES_BLOCK)

private val provider = CryptographyProvider.Default
private val cbcProvider = provider.get(AES.CBC)
private val ctrProvider = provider.get(AES.CTR)

suspend fun aes_ctr_key(atu8_key: UByteArray): AES.CTR.Key {
    return ctrProvider.keyDecoder().decodeFrom(AES.Key.Format.RAW, atu8_key.asByteArray())
}

suspend fun aes_cbc_key(atu8_key: UByteArray): AES.CBC.Key {
    return cbcProvider.keyDecoder().decodeFrom(AES.Key.Format.RAW, atu8_key.asByteArray())
}

// perform AES-CBC
@OptIn(DelicateCryptographyApi::class)
suspend fun aesCbc(d_key_cbc: AES.CBC.Key, atu8_data: UByteArray): UByteArray {
    val result = d_key_cbc.cipher().encrypt(ATU8_ZERO_BLOCK.asByteArray(), atu8_data.asByteArray()).asUByteArray()
    val d_cipher = UByteArray(NB_AES_BLOCK)
    result.copyInto(d_cipher, 0, 0, NB_AES_BLOCK)
    return d_cipher
}

// perform AES-CTR
@OptIn(DelicateCryptographyApi::class)
suspend fun aesCtr(d_key_ctr: AES.CTR.Key, atu8_iv: UByteArray, atu8_data: UByteArray): UByteArray {
    val d_cipher = d_key_ctr.cipher().encrypt(atu8_iv.asByteArray(), atu8_data.asByteArray()).asUByteArray()
    return d_cipher
}


// pseudo-constant-time select
fun select(xbValue: Int, xbRif1: Int, xbRif0: Int): Int = ((xbValue - 1).inv() and xbRif1) or ((xbValue - 1) and xbRif0)

// double block value in-place
fun doubleBlock(atu8Block: UByteArray) {
    var xbCarry = 0

    for (ibEach in NB_AES_BLOCK - 1 downTo 0) {
        val xbTmp = ((atu8Block[ibEach].toInt() and 0xff) ushr 7) and 0xff
        atu8Block[ibEach] = (((atu8Block[ibEach].toInt() and 0xff) shl 1) or xbCarry).toUByte()
        xbCarry = xbTmp
    }

    atu8Block[NB_AES_BLOCK - 1] =
        ((atu8Block[NB_AES_BLOCK - 1].toInt() and 0xff) xor select(xbCarry, 0x87, 0)).toUByte()
    @Suppress("UNUSED_VALUE")
    xbCarry = 0
}

fun xorBuffers(atu8A: UByteArray, atu8B: UByteArray) {
    for (ibEach in 0 until atu8A.size) {
        atu8A[ibEach] = (atu8A[ibEach].toInt() xor atu8B[ibEach].toInt()).toUByte()
    }
}

// Assuming aesCbc, doubleBlock, xorBuffers are defined similarly as suspend functions or adapted Kotlin/JS functions
suspend fun aesCmacInit(dKeyMac: AES.CBC.Key): suspend (atu8_data: UByteArray) -> UByteArray {
    // k1 subkey generation
    val atu8K1 = aesCbc(dKeyMac, ATU8_ZERO_BLOCK)
    doubleBlock(atu8K1)

    // k2 subkey generation
    val atu8K2 = atu8K1.sliceArray(0 until atu8K1.size)
    doubleBlock(atu8K2)

    // Return a suspend function for CMAC computation
    return { atu8Data ->
        // cache data byte count
        val nbData = atu8Data.size

        // number of blocks needed
        val nlBlocks = ceil(nbData.toDouble() / NB_AES_BLOCK.toDouble()).toInt()

        val atu8DataStart = if (nlBlocks == 0) 1 else nlBlocks

        // last block
        val atu8Last = UByteArray(NB_AES_BLOCK)
        atu8Data.sliceArray((atu8DataStart - 1) * NB_AES_BLOCK until atu8Data.size).copyInto(atu8Last)

        // cache size of last block
        val nbLast = nbData % NB_AES_BLOCK

        // last block requires padding
        if (nbLast > 0 || nlBlocks == 0) {
            // M_last := {ANS} XOR K2
            xorBuffers(atu8Last, atu8K2)

            // padding(M_n)
            atu8Last[nbLast] = atu8Last[nbLast] xor 0x80u
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
            xorBuffers(atu8Block, atu8Data.sliceArray(iBlock * NB_AES_BLOCK until atu8Data.size))

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
@OptIn(ExperimentalStdlibApi::class)
suspend fun s2v(
    d_key_rkd: AES.CBC.Key, atu8_plaintext: UByteArray, a_ad: Array<UByteArray> = arrayOf(UByteArray(0))
): UByteArray {
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
    val nb_plaintext = atu8_plaintext.size

    // last block of plaintext
    val atu8_sn = UByteArray(NB_AES_BLOCK)

    // if len(Sn) >= 128
    if (nb_plaintext >= NB_AES_BLOCK) {
        // Sn_end xor D
        atu8_plaintext.sliceArray(nb_plaintext - NB_AES_BLOCK until atu8_plaintext.size).copyInto(atu8_sn)
        xorBuffers(atu8_sn, atu8_cmac)

        // T = Sn xorend D
        atu8_cmac = atu8_plaintext.sliceArray(0 until atu8_plaintext.size)
        atu8_sn.copyInto(atu8_cmac, nb_plaintext - NB_AES_BLOCK)
    } else {
        // dbl(D)
        doubleBlock(atu8_cmac)

        // pad(Sn)
        atu8_plaintext.copyInto(atu8_sn)
        atu8_sn[nb_plaintext] = 0x80u

        // T = dbl(D) xor pad(Sn)
        xorBuffers(atu8_cmac, atu8_sn)
    }

    // V = AES-CMAC(K, T)
    return f_cmac(atu8_cmac)
}
