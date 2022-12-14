// Curve25519 signatures (and also key agreement)
// like in the early Axolotl.
//
// Ported to Kotlin by Miguel Sandro Lucero. miguel.sandro@gmail.com. 2017.05.31
// You can use it under MIT or CC0 license.
//
// Curve25519 signatures idea and math by Trevor Perrin
// https://moderncrypto.org/mail-archive/curves/2014/000205.html
//
// Derived from axlsign.js written by Dmitry Chestnykh. https://github.com/wavesplatform/curve25519-js
package io.eqoty.kryptools.axlsign

import kotlin.random.Random


// optimized by miguel
fun crypto_hashblocks_hl(hh: IntArray, hl: IntArray, m: IntArray, _n: Int): Int {

    val wh = IntArray(16)
    val wl = IntArray(16)

    val bh = IntArray(8)
    val bl = IntArray(8)

    var th: Int
    var tl: Int
    var h: Int
    var l: Int
    var a: Int
    var b: Int
    var c: Int
    var d: Int

    val ah = IntArray(8)
    val al = IntArray(8)
    for (i in 0..7) {
        ah[i] = hh[i]
        al[i] = hl[i]
    }

    var pos = 0
    var n = _n
    while (n >= 128) {

        for (i in 0..15) {
            val j = 8 * i + pos
            wh[i] = (m[j + 0] shl 24) or (m[j + 1] shl 16) or (m[j + 2] shl 8) or m[j + 3]
            wl[i] = (m[j + 4] shl 24) or (m[j + 5] shl 16) or (m[j + 6] shl 8) or m[j + 7]
        }

        for (i in 0..79) {
            for (j in 0..6) {
                bh[j] = ah[j]
                bl[j] = al[j]
            }

            // add
            h = ah[7]
            l = al[7]

            a = l and 0xffff; b = l shr 16
            c = h and 0xffff; d = h shr 16

            // Sigma1
            h =
                ((ah[4] shr 14) or (al[4] shl (32 - 14))) xor ((ah[4] shr 18) or (al[4] shl (32 - 18))) xor ((al[4] shr (41 - 32)) or (ah[4] shl (32 - (41 - 32))))
            l =
                ((al[4] shr 14) or (ah[4] shl (32 - 14))) xor ((al[4] shr 18) or (ah[4] shl (32 - 18))) xor ((ah[4] shr (41 - 32)) or (al[4] shl (32 - (41 - 32))))

            a += l and 0xffff
            b += l shr 16
            c += h and 0xffff
            d += h shr 16

            // Ch
            h = (ah[4] and ah[5]) xor (ah[4].inv() and ah[6])
            l = (al[4] and al[5]) xor (al[4].inv() and al[6])

            a += l and 0xffff; b += l shr 16
            c += h and 0xffff; d += h shr 16

            // K
            h = K[i * 2]
            l = K[i * 2 + 1]

            a += l and 0xffff
            b += l shr 16
            c += h and 0xffff
            d += h shr 16

            // w
            h = wh[i % 16]
            l = wl[i % 16]

            a += l and 0xffff
            b += l shr 16
            c += h and 0xffff
            d += h shr 16

            b += a shr 16
            c += b shr 16
            d += c shr 16

            // *** R
            th = c and 0xffff or (d shl 16)
            tl = a and 0xffff or (b shl 16)

            // add
            h = th
            l = tl

            a = l and 0xffff
            b = l shr 16
            c = h and 0xffff
            d = h shr 16

            // Sigma0
            h =
                ((ah[0] shr 28) or (al[0] shl (32 - 28))) xor ((al[0] shr (34 - 32)) or (ah[0] shl (32 - (34 - 32)))) xor ((al[0] shr (39 - 32)) or (ah[0] shl (32 - (39 - 32))))
            l =
                ((al[0] shr 28) or (ah[0] shl (32 - 28))) xor ((ah[0] shr (34 - 32)) or (al[0] shl (32 - (34 - 32)))) xor ((ah[0] shr (39 - 32)) or (al[0] shl (32 - (39 - 32))))

            a += l and 0xffff
            b += l shr 16
            c += h and 0xffff
            d += h shr 16

            // Maj
            h = (ah[0] and ah[1]) xor (ah[0] and ah[2]) xor (ah[1] and ah[2])
            l = (al[0] and al[1]) xor (al[0] and al[2]) xor (al[1] and al[2])

            a += l and 0xffff; b += l shr 16
            c += h and 0xffff; d += h shr 16

            b += a shr 16
            c += b shr 16
            d += c shr 16

            bh[7] = (c and 0xffff) or (d shl 16)
            bl[7] = (a and 0xffff) or (b shl 16)

            // add
            h = bh[3]
            l = bl[3]

            a = l and 0xffff
            b = l shr 16
            c = h and 0xffff
            d = h shr 16

            h = th
            l = tl

            a += l and 0xffff
            b += l shr 16
            c += h and 0xffff
            d += h shr 16

            b += a shr 16
            c += b shr 16
            d += c shr 16

            bh[3] = (c and 0xffff) or (d shl 16)
            bl[3] = (a and 0xffff) or (b shl 16)

            for (j in 0..7) {
                val k = (j + 1) % 8
                ah[k] = bh[j]
                al[k] = bl[j]
            }

            if (i % 16 == 15) {
                for (j in 0..15) {
                    // add
                    h = wh[j]
                    l = wl[j]

                    a = l and 0xffff; b = l shr 16
                    c = h and 0xffff; d = h shr 16

                    h = wh[(j + 9) % 16]
                    l = wl[(j + 9) % 16]

                    a += l and 0xffff; b += l shr 16
                    c += h and 0xffff; d += h shr 16

                    // sigma0
                    th = wh[(j + 1) % 16]
                    tl = wl[(j + 1) % 16]

                    h = ((th shr 1) or (tl shl (32 - 1))) xor ((th shr 8) or (tl shl (32 - 8))) xor (th shr 7)
                    l =
                        ((tl shr 1) or (th shl (32 - 1))) xor ((tl shr 8) or (th shl (32 - 8))) xor ((tl shr 7) or (th shl (32 - 7)))

                    a += l and 0xffff; b += l shr 16
                    c += h and 0xffff; d += h shr 16

                    // sigma1
                    th = wh[(j + 14) % 16]
                    tl = wl[(j + 14) % 16]

                    h =
                        ((th shr 19) or (tl shl (32 - 19))) xor ((tl shr (61 - 32)) or (th shl (32 - (61 - 32)))) xor (th shr 6)
                    l =
                        ((tl shr 19) or (th shl (32 - 19))) xor ((th shr (61 - 32)) or (tl shl (32 - (61 - 32)))) xor ((tl shr 6) or (th shl (32 - 6)))

                    a += l and 0xffff; b += l shr 16
                    c += h and 0xffff; d += h shr 16

                    b += a shr 16
                    c += b shr 16
                    d += c shr 16

                    wh[j] = ((c and 0xffff) or (d shl 16))
                    wl[j] = ((a and 0xffff) or (b shl 16))
                }
            }
        }

        // add
        a = 0; b = 0; c = 0; d = 0
        for (k in 0..7) {
            if (k == 0) {
                h = ah[0]
                l = al[0]
                a = l and 0xffff; b = l shr 16
                c = h and 0xffff; d = h shr 16
            }

            h = hh[k]
            l = hl[k]

            a += l and 0xffff; b += l shr 16
            c += h and 0xffff; d += h shr 16

            b += a shr 16
            c += b shr 16
            d += c shr 16

            hh[k] = (c and 0xffff) or (d shl 16)
            ah[k] = (c and 0xffff) or (d shl 16)

            hl[k] = (a and 0xffff) or (b shl 16)
            al[k] = (a and 0xffff) or (b shl 16)

            if (k < 7) {
                h = ah[k + 1]
                l = al[k + 1]

                a = l and 0xffff; b = l shr 16
                c = h and 0xffff; d = h shr 16
            }
        }

        pos += 128
        n -= 128
    }

    return n
}


fun crypto_hash(out: IntArray, m: IntArray, _n: Int): Int {
    val hh = _HH.copyOf()
    val hl = _HL.copyOf()
    val x = IntArray(256)
    var n = _n
    val b = n

    crypto_hashblocks_hl(hh, hl, m, n)

    n %= 128

    for (i in 0 until n) {
        x[i] = m[b - n + i]
    }
    x[n] = 128

    // *** R n = 256-128 * (n<112?1:0);
    if (n < 112) {
        n = 256 - 128 * 1
    } else {
        n = 256 - 128 * 0
    }
    x[n - 9] = 0

    ts64(x, n - 8, ((b / 0x20000000) or 0), (b shl 3))

    crypto_hashblocks_hl(hh, hl, x, n)

    for (i in 0..7) {
        ts64(out, 8 * i, hh[i], hl[i])
    }

    return 0

}

private fun ts64(x: IntArray, i: Int, h: Int, l: Int) {
    x[i] = ((h shr 24) and 0xff)
    x[i + 1] = ((h shr 16) and 0xff)
    x[i + 2] = ((h shr 8) and 0xff)
    x[i + 3] = (h and 0xff)
    x[i + 4] = ((l shr 24) and 0xff)
    x[i + 5] = ((l shr 16) and 0xff)
    x[i + 6] = ((l shr 8) and 0xff)
    x[i + 7] = (l and 0xff)
}


abstract class AxlSign {

    abstract val curve25519: Curve25519

    fun sharedKey(secretKey: IntArray, publicKey: IntArray): IntArray {
        val sharedKey = IntArray(32)
        curve25519.crypto_scalarmult(sharedKey, secretKey, publicKey)
        return sharedKey
    }

    fun signMessage(secretKey: IntArray, msg: IntArray, opt_random: IntArray?): IntArray {
        if (opt_random != null) {
            val buf = IntArray(128 + msg.size)
            curve25519.sign(buf, msg, msg.size, secretKey, opt_random)
            return buf.copyOfRange(0, 64 + msg.size)
        } else {
            val signedMsg = IntArray(64 + msg.size)
            curve25519.sign(signedMsg, msg, msg.size, secretKey, null)
            return signedMsg
        }
    }

    fun openMessage(publicKey: IntArray, signedMsg: IntArray): IntArray? {
        val tmp = IntArray(signedMsg.size)
        val mlen = curve25519.sign_open(tmp, signedMsg, signedMsg.size, publicKey)
        if (mlen < 0) {
            return null
        }
        val m = IntArray(mlen)
        for (i in 0..m.size - 1) {
            m[i] = tmp[i]
        }
        return m
    }

    // add by Miguel
    fun openMessageStr(publicKey: IntArray, signedMsg: IntArray): String {
        val m = openMessage(publicKey, signedMsg) ?: return ""
        var msg = ""
        for (element in m) {
            msg += element.toChar()
        }
        return msg
    }

    fun sign(secretKey: IntArray, msg: IntArray, opt_random: IntArray?): IntArray {
        var len = 64
        if (opt_random != null) {
            len = 128
        }
        val buf = IntArray(len + msg.size)
        curve25519.sign(buf, msg, msg.size, secretKey, opt_random)

        val signature = IntArray(64)
        for (i in 0 until signature.size) {
            signature[i] = buf[i]
        }
        return signature
    }

    fun verify(publicKey: IntArray, msg: IntArray, signature: IntArray): Int {
        val sm = IntArray(64 + msg.size)
        val m = IntArray(64 + msg.size)

        for (i in 0..63) {
            sm[i] = signature[i]
        }

        for (i in 0 until msg.size) {
            sm[i + 64] = msg[i]
        }

        if (curve25519.sign_open(m, sm, sm.size, publicKey) >= 0) {
            return 1
        } else {
            return 0
        }
    }

    fun generateKeyPair(seed: IntArray): Keys {
        val sk = IntArray(32)
        val pk = IntArray(32)

        for (i in 0..31) {
            sk[i] = seed[i]
        }

        curve25519.crypto_scalarmult_base(pk, sk)

        // Turn secret key into the correct format.
        sk[0] = sk[0] and 248
        sk[31] = sk[31] and 127
        sk[31] = sk[31] or 64

        // Remove sign bit from public key.
        pk[31] = pk[31] and 127

        return Keys(pk, sk)
    }

    fun randomBytes(size: Int): IntArray {
        val High = 255
        val Low = 0
        val seed = IntArray(size)
        val rnd = Random
        for (i in seed.indices) {
            seed[i] = rnd.nextInt(High - Low) + Low
        }
        return seed
    }

    class Keys(pk: IntArray, sk: IntArray) {
        var publicKey = pk
        var privateKey = sk
    }

}