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
//
//
//private fun gf(): DoubleArray {
//    return gf(longArrayOf(0))
//}
//
//private fun gf(init: LongArray): DoubleArray {
//    val r = DoubleArray(16)
//    for (i in 0..init.size - 1) {
//        r[i] = init[i].toDouble()
//    }
//    return r
//}
//
//private fun vn(x: IntArray, xi: Int, y: IntArray, yi: Int, n: Int): Int {
//    var d = 0
//    for (i in 0..n - 1) {
//        d = d or (x[xi + i] xor y[yi + i])
//    }
//    return (1 and ((d - 1) ushr 8)) - 1
//}
//
//private fun crypto_verify_32(x: IntArray, xi: Int, y: IntArray, yi: Int): Int {
//    return vn(x, xi, y, yi, 32)
//}
//
//private fun set25519(r: DoubleArray, a: DoubleArray) {
//    for (i in 0..15) {
//        r[i] = (a[i].toLong() or 0).toDouble()
//    }
//}
//
//private fun car25519(o: DoubleArray) {
//    var v: Double
//    var c = 1.0
//    for (i in 0..15) {
//        v = o[i] + c + 65535
//        c = floorLng(v / 65536.0)
//        o[i] = v - c * 65536
//    }
//    o[0] = o[0] + c - 1 + 37 * (c - 1)
//}
//
//private fun sel25519(p: DoubleArray, q: DoubleArray, b: Int) {
//    var t: Double
//    val invb = (b - 1).inv()
//    val c = invb
//    for (i in 0..15) {
//        t = (c.toLong() and (p[i].toLong() xor q[i].toLong())).toDouble()
//        p[i] = (p[i].toLong() xor t.toLong()).toDouble()
//        q[i] = (q[i].toLong() xor t.toLong()).toDouble()
//    }
//}
//
//private fun pack25519(o: IntArray, n: DoubleArray) {
//    var b: Double
//    val m = gf()
//    val t = gf()
//
//    for (i in 0..15) {
//        t[i] = n[i]
//    }
//    car25519(t)
//    car25519(t)
//    car25519(t)
//
//    for (j in 0..1) {
//        m[0] = t[0] - 0xffed
//        for (i in 1..14) {
//            m[i] = t[i] - 0xffff - ((m[i - 1].toLong() shr 16) and 1)
//            m[i - 1] = (m[i - 1].toLong() and 0xffff).toDouble()
//        }
//        m[15] = t[15] - 0x7fff - ((m[14].toLong() shr 16) and 1)
//        b = ((m[15].toLong() shr 16) and 1).toDouble()
//        m[14] = (m[14].toLong() and 0xffff).toDouble()
//        sel25519(t, m, 1 - b.toInt())
//    }
//    for (i in 0..15) {
//        o[2 * i] = (t[i]).toInt() and 0xff
//        o[2 * i + 1] = (t[i]).toInt() shr 8
//    }
//}
//
//private fun neq25519(a: DoubleArray, b: DoubleArray): Int {
//    val c = IntArray(32)
//    val d = IntArray(32)
//    pack25519(c, a)
//    pack25519(d, b)
//    return crypto_verify_32(c, 0, d, 0)
//}
//
//private fun par25519(a: DoubleArray): Int {
//    val d = IntArray(32)
//    pack25519(d, a)
//    return d[0] and 1
//}
//
//private fun unpack25519(o: DoubleArray, n: IntArray) {
//    for (i in 0..15) {
//        val value: Int = n[2 * i] + (n[2 * i + 1] shl 8)
//        o[i] = value.toDouble()
//    }
//    o[15] = (o[15].toLong() and 0x7fff).toDouble()
//}
//
//private fun A(o: DoubleArray, a: DoubleArray, b: DoubleArray) {
//    for (i in 0..15) {
//        o[i] = a[i] + b[i]
//    }
//}
//
//private fun Z(o: DoubleArray, a: DoubleArray, b: DoubleArray) {
//    for (i in 0..15) {
//        o[i] = a[i] - b[i]
//    }
//}
//
//// optimized by Miguel
//private fun M(o: DoubleArray, a: DoubleArray, b: DoubleArray) {
//    val at = DoubleArray(32)
//    val ab = DoubleArray(16)
//
//    for (i in 0..15) {
//        ab[i] = b[i]
//    }
//
//    var v: Double
//    for (i in 0..15) {
//        v = a[i]
//        for (j in 0..15) {
//            at[j + i] += v * ab[j]
//        }
//    }
//
//    for (i in 0..14) {
//        at[i] += 38 * at[i + 16]
//    }
//    // t15 left as is
//
//    // first car
//    var c: Double = 1.0
//    for (i in 0..15) {
//        v = at[i] + c + 65535
//        c = floorLng(v / 65536.0)
//        at[i] = v - c * 65536
//    }
//    at[0] += c - 1 + 37 * (c - 1)
//
//    // second car
//    c = 1.0
//    for (i in 0..15) {
//        v = at[i] + c + 65535
//        c = floorLng(v / 65536.0)
//        at[i] = v - c * 65536
//    }
//    at[0] += c - 1 + 37 * (c - 1)
//
//    for (i in 0..15) {
//        o[i] = at[i]
//    }
//
//}
//
//private fun S(o: DoubleArray, a: DoubleArray) {
//    M(o, a, a)
//}
//
//private fun inv25519(o: DoubleArray, i: DoubleArray) {
//    val c = gf()
//    for (a in 0..15) {
//        c[a] = i[a]
//    }
//
//    for (a in 253 downTo 0) {
//        S(c, c)
//        if (a != 2 && a != 4) {
//            M(c, c, i)
//        }
//    }
//    for (a in 0..15) {
//        o[a] = c[a]
//    }
//}
//
//private fun pow2523(o: DoubleArray, i: DoubleArray) {
//    val c = gf()
//    for (a in 0..15) {
//        c[a] = i[a]
//    }
//    for (a in 250 downTo 0) {
//        S(c, c)
//        if (a != 1) {
//            M(c, c, i)
//        }
//    }
//    for (a in 0..15) {
//        o[a] = c[a]
//    }
//}
//
//private fun crypto_scalarmult(q: UIntArray, n: UIntArray, p: UIntArray): Int {
//    val z = IntArray(32)
//    val x = DoubleArray(80)
//    var r: Int
//
//    val a = gf()
//    val b = gf()
//    val c = gf()
//    val d = gf()
//    val e = gf()
//    val f = gf()
//
//    for (i in 0..30) {
//        z[i] = n[i]
//    }
//    z[31] = (n[31] and 127) or 64
//    z[0] = z[0] and 248
//
//    unpack25519(x, p)
//
//    for (i in 0..15) {
//        b[i] = x[i]
//        d[i] = 0.0
//        a[i] = 0.0
//        c[i] = 0.0
//    }
//    a[0] = 1.0
//    d[0] = 1.0
//
//    for (i in 254 downTo 0) {
//
//        r = (z[i ushr 3] ushr (i and 7)) and 1
//
//        sel25519(a, b, r)
//        sel25519(c, d, r)
//        A(e, a, c)
//        Z(a, a, c)
//        A(c, b, d)
//        Z(b, b, d)
//        S(d, e)
//        S(f, a)
//        M(a, c, a)
//        M(c, b, e)
//        A(e, a, c)
//        Z(a, a, c)
//        S(b, a)
//        Z(c, d, f)
//        M(a, c, _121665)
//        A(a, a, d)
//        M(c, c, a)
//        M(a, d, f)
//        M(d, b, x)
//        S(b, e)
//        sel25519(a, b, r)
//        sel25519(c, d, r)
//
//    }
//
//    for (i in 0..15) {
//        x[i + 16] = a[i]
//        x[i + 32] = c[i]
//        x[i + 48] = b[i]
//        x[i + 64] = d[i]
//    }
//
//    val x32 = x.copyOfRange(32, x.size) // from 32
//    val x16 = x.copyOfRange(16, x.size) // from 16
//
//    inv25519(x32, x32)
//
//    M(x16, x16, x32)
//
//    pack25519(q, x16)
//
//    return 0
//}
//
//private fun crypto_scalarmult_base(q: IntArray, n: IntArray): Int {
//    return crypto_scalarmult(q, n, _9)
//}
//
//
//private fun add(p: Array<DoubleArray>, q: Array<DoubleArray>) {
//    val a = gf()
//    val b = gf()
//    val c = gf()
//    val d = gf()
//    val e = gf()
//    val f = gf()
//    val g = gf()
//    val h = gf()
//    val t = gf()
//
//    Z(a, p[1], p[0])
//    Z(t, q[1], q[0])
//    M(a, a, t)
//    A(b, p[0], p[1])
//    A(t, q[0], q[1])
//    M(b, b, t)
//    M(c, p[3], q[3])
//    M(c, c, D2)
//    M(d, p[2], q[2])
//    A(d, d, d)
//    Z(e, b, a)
//    Z(f, d, c)
//    A(g, d, c)
//    A(h, b, a)
//
//    M(p[0], e, f)
//    M(p[1], h, g)
//    M(p[2], g, f)
//    M(p[3], e, h)
//}
//
//private fun cswap(p: Array<DoubleArray>, q: Array<DoubleArray>, b: Int) {
//    for (i in 0..3) {
//        sel25519(p[i], q[i], b)
//    }
//}
//
//private fun pack(r: IntArray, p: Array<DoubleArray>) {
//    val tx = gf()
//    val ty = gf()
//    val zi = gf()
//
//    inv25519(zi, p[2])
//
//    M(tx, p[0], zi)
//    M(ty, p[1], zi)
//
//    pack25519(r, ty)
//
//    r[31] = r[31] xor (par25519(tx) shl 7)
//
//}
//
//private fun scalarmult(p: Array<DoubleArray>, q: Array<DoubleArray>, s: IntArray) {
//    var b: Int
//
//    set25519(p[0], gf0)
//    set25519(p[1], gf1)
//    set25519(p[2], gf1)
//    set25519(p[3], gf0)
//
//    for (i in 255 downTo 0) {
//        b = (s[(i / 8) or 0] shr (i and 7)) and 1
//        cswap(p, q, b)
//        add(q, p)
//        add(p, p)
//        cswap(p, q, b)
//    }
//}
//
//private fun scalarbase(p: Array<DoubleArray>, s: IntArray) {
//    val q = arrayOf(gf(), gf(), gf(), gf())
//    set25519(q[0], X)
//    set25519(q[1], Y)
//    set25519(q[2], gf1)
//    M(q[3], X, Y)
//    scalarmult(p, q, s)
//}
//
//
//// Like crypto_sign, but uses secret key directly in hash.
//private fun crypto_sign_direct(sm: IntArray, m: IntArray, n: Int, sk: IntArray): Int {
//    val h = IntArray(64)
//    val r = IntArray(64)
//    val x = IntArray(64)
//    val p = arrayOf(gf(), gf(), gf(), gf())
//
//    for (i in 0..n - 1) {
//        sm[64 + i] = m[i]
//    }
//
//    for (i in 0..31) {
//        sm[32 + i] = sk[i]
//    }
//
//    crypto_hash(r, sm.copyOfRange(32, sm.size), n + 32)
//
//    reduce(r)
//
//    scalarbase(p, r)
//
//    pack(sm, p)
//
//    for (i in 0..31) {
//        sm[i + 32] = sk[32 + i]
//    }
//
//    crypto_hash(h, sm, n + 64)
//    reduce(h)
//
//    for (i in 0..63) {
//        x[i] = 0
//    }
//
//    for (i in 0..31) {
//        x[i] = r[i]
//    }
//
//    for (i in 0..31) {
//        for (j in 0..31) {
//            x[i + j] += h[i] * sk[j]
//        }
//    }
//
//    val tmp = sm.copyOfRange(32, sm.size)
//    modL(tmp, x)
//    for (i in 0..tmp.size - 1) {
//        sm[32 + i] = tmp[i]
//    }
//
//    return n + 64
//
//}
//
//
//private fun curve25519_sign(sm: UIntArray, m: UIntArray, n: Int, sk: UIntArray, opt_rnd: UIntArray?): Int {
//    // If opt_rnd is provided, sm must have n + 128,
//    // otherwise it must have n + 64 bytes.
//
//    // Convert Curve25519 secret key into Ed25519 secret key (includes pub key).
//    val edsk = UIntArray(64)
//    val p = arrayOf(gf(), gf(), gf(), gf())
//
//    for (i in 0..31) {
//        edsk[i] = sk[i]
//    }
//
//    // Ensure private key is in the correct format.
//    edsk[0] = edsk[0] and 248u
//    edsk[31] = edsk[31] and 127u
//    edsk[31] = edsk[31] or 64u
//
//    scalarbase(p, edsk)
//
//    val tmp = edsk.copyOfRange(32, edsk.size)
//    pack(tmp, p)
//    for (i in 0 until tmp.size) {
//        edsk[32 + i] = tmp[i]
//    }
//
//    // Remember sign bit.
//    val signBit = edsk[63] and 128u
//    val smlen: Int
//
//    if (opt_rnd != null) {
//        smlen = crypto_sign_direct_rnd(sm, m, n, edsk, opt_rnd)
//    } else {
//        smlen = crypto_sign_direct(sm, m, n, edsk)
//    }
//
//    // Copy sign bit from public key into signature.
//    sm[63] = sm[63] or signBit
//    return smlen
//}
//
//private fun unpackneg(r: Array<DoubleArray>, p: IntArray): Int {
//    val t = gf()
//    val chk = gf()
//    val num = gf()
//    val den = gf()
//    val den2 = gf()
//    val den4 = gf()
//    val den6 = gf()
//
//    set25519(r[2], gf1)
//    unpack25519(r[1], p)
//
//    S(num, r[1])
//    M(den, num, D)
//    Z(num, num, r[2])
//    A(den, r[2], den)
//
//    S(den2, den)
//    S(den4, den2)
//    M(den6, den4, den2)
//    M(t, den6, num)
//    M(t, t, den)
//
//    pow2523(t, t)
//    M(t, t, num)
//    M(t, t, den)
//    M(t, t, den)
//    M(r[0], t, den)
//
//    S(chk, r[0])
//    M(chk, chk, den)
//
//    if (neq25519(chk, num) != 0) {
//        M(r[0], r[0], I)
//    }
//
//    S(chk, r[0])
//    M(chk, chk, den)
//
//    if (neq25519(chk, num) != 0) {
//        return -1
//    }
//
//    if (par25519(r[0]) == (p[31] shr 7)) {
//        Z(r[0], gf0, r[0])
//    }
//
//    M(r[3], r[0], r[1])
//
//    return 0
//}
//
//private fun crypto_sign_open(m: IntArray, sm: IntArray, _n: Int, pk: IntArray): Int {
//    val t = IntArray(32)
//    val h = IntArray(64)
//    val p = arrayOf(gf(), gf(), gf(), gf())
//    val q = arrayOf(gf(), gf(), gf(), gf())
//    var n = _n
//
//    var mlen = -1
//    if (n < 64) {
//        return mlen
//    }
//
//    if (unpackneg(q, pk) != 0) {
//        return mlen
//    }
//
//    for (i in 0..n - 1) {
//        m[i] = sm[i]
//    }
//
//    for (i in 0..31) {
//        m[i + 32] = pk[i]
//    }
//
//    crypto_hash(h, m, n)
//    reduce(h)
//    scalarmult(p, q, h)
//
//    scalarbase(q, sm.copyOfRange(32, sm.size))
//    add(p, q)
//    pack(t, p)
//
//    n -= 64
//    if (crypto_verify_32(sm, 0, t, 0) != 0) {
//        for (i in 0..n - 1) {
//            m[i] = 0
//        }
//        return -1
//    }
//
//    for (i in 0..n - 1) {
//        m[i] = sm[i + 64]
//    }
//
//    mlen = n
//    return mlen
//
//}
//
//// Converts Curve25519 public key back to Ed25519 public key.
//// edwardsY = (montgomeryX - 1) / (montgomeryX + 1)
//private fun convertPublicKey(pk: IntArray): IntArray {
//    val z = IntArray(32)
//    val x = gf()
//    val a = gf()
//    val b = gf()
//
//    unpack25519(x, pk)
//
//    A(a, x, gf1)
//    Z(b, x, gf1)
//    inv25519(a, a)
//    M(a, a, b)
//
//    pack25519(z, a)
//    return z
//}
//

// Class
class AxlSignDouble : AxlSign() {
    override val curve25519: Curve25519Long
        get() = TODO("Not yet implemented")

}