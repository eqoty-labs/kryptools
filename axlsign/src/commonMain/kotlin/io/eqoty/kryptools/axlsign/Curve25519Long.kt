package io.eqoty.kryptools.axlsign

import kotlin.math.floor

class Curve25519Long : Curve25519() {

    val X = gf(
        longArrayOf(
            0xd51a, 0x8f25, 0x2d60, 0xc956,
            0xa7b2, 0x9525, 0xc760, 0x692c,
            0xdc5c, 0xfdd6, 0xe231, 0xc0a4,
            0x53fe, 0xcd6e, 0x36d3, 0x2169
        )
    )

    val Y = gf(
        longArrayOf(
            0x6658, 0x6666, 0x6666, 0x6666,
            0x6666, 0x6666, 0x6666, 0x6666,
            0x6666, 0x6666, 0x6666, 0x6666,
            0x6666, 0x6666, 0x6666, 0x6666
        )
    )

    val gf0 = gf()

    val gf1 = gf(longArrayOf(1))

    val D = gf(
        longArrayOf(
            0x78a3, 0x1359, 0x4dca, 0x75eb,
            0xd8ab, 0x4141, 0x0a4d, 0x0070,
            0xe898, 0x7779, 0x4079, 0x8cc7,
            0xfe73, 0x2b6f, 0x6cee, 0x5203
        )
    )

    val D2 = gf(
        longArrayOf(
            0xf159, 0x26b2, 0x9b94, 0xebd6,
            0xb156, 0x8283, 0x149a, 0x00e0,
            0xd130, 0xeef3, 0x80f2, 0x198e,
            0xfce7, 0x56df, 0xd9dc, 0x2406
        )
    )

    val I = gf(
        longArrayOf(
            0xa0b0, 0x4a0e, 0x1b27, 0xc4ee,
            0xe478, 0xad2f, 0x1806, 0x2f43,
            0xd7a7, 0x3dfb, 0x0099, 0x2b4d,
            0xdf0b, 0x4fc1, 0x2480, 0x2b83
        )
    )

    val _121665 = gf(longArrayOf(0xdb41, 1))

    private fun gf(init: LongArray = LongArray(16) { 0 }): LongArray {
        return init.copyOf(16)
    }

    override fun sign(sm: IntArray, m: IntArray, n: Int, sk: IntArray, opt_rnd: IntArray?): Int {
        // If opt_rnd is provided, sm must have n + 128,
        // otherwise it must have n + 64 bytes.

        // Convert Curve25519 secret key into Ed25519 secret key (includes pub key).
        val edsk = IntArray(64)
        val p = arrayOf(gf(), gf(), gf(), gf())

        for (i in 0..31) {
            edsk[i] = sk[i]
        }

        // Ensure private key is in the correct format.
        edsk[0] = edsk[0] and 248
        edsk[31] = edsk[31] and 127
        edsk[31] = edsk[31] or 64

        scalarbase(p, edsk)

        val tmp = edsk.copyOfRange(32, edsk.size)
        pack(tmp, p)
        for (i in 0..tmp.size - 1) {
            edsk[32 + i] = tmp[i]
        }

        // Remember sign bit.
        val signBit = edsk[63] and 128
        val smlen: Int

        if (opt_rnd != null) {
            smlen = crypto_sign_direct_rnd(sm, m, n, edsk, opt_rnd)
        } else {
            smlen = crypto_sign_direct(sm, m, n, edsk)
        }

        // Copy sign bit from public key into signature.
        sm[63] = sm[63] or signBit
        return smlen
    }

    override fun sign_open(m: IntArray, sm: IntArray, n: Int, pk: IntArray): Int {
        // Convert Curve25519 public key into Ed25519 public key.
        val edpk = convertPublicKey(pk)

        // Restore sign bit from signature.
        edpk[31] = edpk[31] or (sm[63] and 128)

        // Remove sign bit from signature.
        sm[63] = sm[63] and 127

        // Verify signed message.
        return crypto_sign_open(m, sm, n, edpk)
    }

    override fun crypto_scalarmult(q: IntArray, n: IntArray, p: IntArray): Int {
        val z = IntArray(32)
        val x = LongArray(80)
        var r: Int

        val a = gf()
        val b = gf()
        val c = gf()
        val d = gf()
        val e = gf()
        val f = gf()

        for (i in 0..30) {
            z[i] = n[i]
        }
        z[31] = (n[31] and 127) or 64
        z[0] = z[0] and 248

        unpack25519(x, p)

        for (i in 0..15) {
            b[i] = x[i]
            d[i] = 0
            a[i] = 0
            c[i] = 0
        }
        a[0] = 1
        d[0] = 1

        for (i in 254 downTo 0) {

            r = (z[i ushr 3] shr (i and 7)) and 1

            sel25519(a, b, r)
            sel25519(c, d, r)
            A(e, a, c)
            Z(a, a, c)
            A(c, b, d)
            Z(b, b, d)
            S(d, e)
            S(f, a)
            M(a, c, a)
            M(c, b, e)
            A(e, a, c)
            Z(a, a, c)
            S(b, a)
            Z(c, d, f)
            M(a, c, _121665)
            A(a, a, d)
            M(c, c, a)
            M(a, d, f)
            M(d, b, x)
            S(b, e)
            sel25519(a, b, r)
            sel25519(c, d, r)

        }

        for (i in 0..15) {
            x[i + 16] = a[i]
            x[i + 32] = c[i]
            x[i + 48] = b[i]
            x[i + 64] = d[i]
        }

        val x32 = x.copyOfRange(32, x.size) // from 32
        val x16 = x.copyOfRange(16, x.size) // from 16

        inv25519(x32, x32)

        M(x16, x16, x32)

        pack25519(q, x16)

        return 0
    }


    // Converts Curve25519 public key back to Ed25519 public key.
    // edwardsY = (montgomeryX - 1) / (montgomeryX + 1)
    private fun convertPublicKey(pk: IntArray): IntArray {
        val z = IntArray(32)
        val x = gf()
        val a = gf()
        val b = gf()

        unpack25519(x, pk)

        A(a, x, gf1)
        Z(b, x, gf1)
        inv25519(a, a)
        M(a, a, b)

        pack25519(z, a)
        return z
    }

    private fun crypto_sign_open(m: IntArray, sm: IntArray, _n: Int, pk: IntArray): Int {
        val t = IntArray(32)
        val h = IntArray(64)
        val p = arrayOf(gf(), gf(), gf(), gf())
        val q = arrayOf(gf(), gf(), gf(), gf())
        var n = _n

        var mlen = -1
        if (n < 64) {
            return mlen
        }

        if (unpackneg(q, pk) != 0) {
            return mlen
        }

        for (i in 0 until n) {
            m[i] = sm[i]
        }

        for (i in 0..31) {
            m[i + 32] = pk[i]
        }

        crypto_hash(h, m, n)
        reduce(h)
        scalarmult(p, q, h)

        scalarbase(q, sm.copyOfRange(32, sm.size))
        add(p, q)
        pack(t, p)

        n -= 64
        if (crypto_verify_32(sm, 0, t, 0) != 0) {
            for (i in 0 until n) {
                m[i] = 0
            }
            return -1
        }

        for (i in 0 until n) {
            m[i] = sm[i + 64]
        }

        mlen = n
        return mlen

    }

    private fun unpackneg(r: Array<LongArray>, p: IntArray): Int {
        val t = gf()
        val chk = gf()
        val num = gf()
        val den = gf()
        val den2 = gf()
        val den4 = gf()
        val den6 = gf()

        set25519(r[2], gf1)
        unpack25519(r[1], p)

        S(num, r[1])
        M(den, num, D)
        Z(num, num, r[2])
        A(den, r[2], den)

        S(den2, den)
        S(den4, den2)
        M(den6, den4, den2)
        M(t, den6, num)
        M(t, t, den)

        pow2523(t, t)
        M(t, t, num)
        M(t, t, den)
        M(t, t, den)
        M(r[0], t, den)

        S(chk, r[0])
        M(chk, chk, den)

        if (neq25519(chk, num) != 0) {
            M(r[0], r[0], I)
        }

        S(chk, r[0])
        M(chk, chk, den)

        if (neq25519(chk, num) != 0) {
            return -1
        }

        if (par25519(r[0]) == (p[31] shr 7)) {
            Z(r[0], gf0, r[0])
        }

        M(r[3], r[0], r[1])

        return 0
    }

    private fun pow2523(o: LongArray, i: LongArray) {
        val c = gf()
        for (a in 0..15) {
            c[a] = i[a]
        }
        for (a in 250 downTo 0) {
            S(c, c)
            if (a != 1) {
                M(c, c, i)
            }
        }
        for (a in 0..15) {
            o[a] = c[a]
        }
    }

    private fun neq25519(a: LongArray, b: LongArray): Int {
        val c = IntArray(32)
        val d = IntArray(32)
        pack25519(c, a)
        pack25519(d, b)
        return crypto_verify_32(c, 0, d, 0)
    }

    private fun vn(x: IntArray, xi: Int, y: IntArray, yi: Int, n: Int): Int {
        var d = 0
        for (i in 0..n - 1) {
            d = d or (x[xi + i] xor y[yi + i])
        }
        return (1 and ((d - 1) shr 8)) - 1
    }

    private fun crypto_verify_32(x: IntArray, xi: Int, y: IntArray, yi: Int): Int {
        return vn(x, xi, y, yi, 32)
    }


    private fun unpack25519(o: LongArray, n: IntArray) {
        for (i in 0..15) {
            val value: Int = n[2 * i] + (n[2 * i + 1] shl 8)
            o[i] = value.toLong()
        }
        o[15] = o[15] and 0x7fff
    }

    private fun scalarbase(p: Array<LongArray>, s: IntArray) {
        val q = arrayOf(gf(), gf(), gf(), gf())
        set25519(q[0], X)
        set25519(q[1], Y)
        set25519(q[2], gf1)
        M(q[3], X, Y)
        scalarmult(p, q, s)
    }

    private fun set25519(r: LongArray, a: LongArray) {
        for (i in 0..15) {
            r[i] = a[i] or 0
        }
    }


    // optimized by Miguel
    private fun M(o: LongArray, a: LongArray, b: LongArray) {
        val at = LongArray(32)
        val ab = LongArray(16)

        for (i in 0..15) {
            ab[i] = b[i]
        }

        var v: Long
        for (i in 0..15) {
            v = a[i]
            for (j in 0..15) {
                at[j + i] += v * ab[j]
            }
        }

        for (i in 0..14) {
            at[i] += 38 * at[i + 16]
        }
        // t15 left as is

        // first car
        var c: Long = 1
        for (i in 0..15) {
            v = at[i] + c + 65535
            c = floor(v / 65536.0).toLong()
            at[i] = v - c * 65536
        }
        at[0] += c - 1 + 37 * (c - 1)

        // second car
        c = 1
        for (i in 0..15) {
            v = at[i] + c + 65535
            c = floor(v / 65536.0).toLong()
            at[i] = v - c * 65536
        }
        at[0] += c - 1 + 37 * (c - 1)

        for (i in 0..15) {
            o[i] = at[i]
        }

    }

    private fun scalarmult(p: Array<LongArray>, q: Array<LongArray>, s: IntArray) {
        var b: Int

        set25519(p[0], gf0)
        set25519(p[1], gf1)
        set25519(p[2], gf1)
        set25519(p[3], gf0)

        for (i in 255 downTo 0) {
            b = (s[(i / 8) or 0] shr (i and 7)) and 1
            cswap(p, q, b)
            add(q, p)
            add(p, p)
            cswap(p, q, b)
        }
    }


    private fun cswap(p: Array<LongArray>, q: Array<LongArray>, b: Int) {
        for (i in 0..3) {
            sel25519(p[i], q[i], b)
        }
    }

    private fun sel25519(p: LongArray, q: LongArray, b: Int) {
        var t: Long
        val invb = (b - 1).inv()
        val c: Long = invb.toLong()
        for (i in 0..15) {
            t = c and (p[i] xor q[i])
            p[i] = p[i] xor t
            q[i] = q[i] xor t
        }
    }


    private fun add(p: Array<LongArray>, q: Array<LongArray>) {
        val a = gf()
        val b = gf()
        val c = gf()
        val d = gf()
        val e = gf()
        val f = gf()
        val g = gf()
        val h = gf()
        val t = gf()

        Z(a, p[1], p[0])
        Z(t, q[1], q[0])
        M(a, a, t)
        A(b, p[0], p[1])
        A(t, q[0], q[1])
        M(b, b, t)
        M(c, p[3], q[3])
        M(c, c, D2)
        M(d, p[2], q[2])
        A(d, d, d)
        Z(e, b, a)
        Z(f, d, c)
        A(g, d, c)
        A(h, b, a)

        M(p[0], e, f)
        M(p[1], h, g)
        M(p[2], g, f)
        M(p[3], e, h)
    }

    private fun Z(o: LongArray, a: LongArray, b: LongArray) {
        for (i in 0..15) {
            o[i] = a[i] - b[i]
        }
    }

    private fun A(o: LongArray, a: LongArray, b: LongArray) {
        for (i in 0..15) {
            o[i] = a[i] + b[i]
        }
    }

    private fun pack(r: IntArray, p: Array<LongArray>) {
        val tx = gf()
        val ty = gf()
        val zi = gf()

        inv25519(zi, p[2])

        M(tx, p[0], zi)
        M(ty, p[1], zi)

        pack25519(r, ty)

        r[31] = r[31] xor (par25519(tx) shl 7)

    }

    private fun inv25519(o: LongArray, i: LongArray) {
        val c = gf()
        for (a in 0..15) {
            c[a] = i[a]
        }

        for (a in 253 downTo 0) {
            S(c, c)
            if (a != 2 && a != 4) {
                M(c, c, i)
            }
        }
        for (a in 0..15) {
            o[a] = c[a]
        }
    }

    private fun S(o: LongArray, a: LongArray) {
        M(o, a, a)
    }

    private fun pack25519(o: IntArray, n: LongArray) {
        var b: Long
        val m = gf()
        val t = gf()

        n.copyInto(t, 0, 0, 16)

        car25519(t)
        car25519(t)
        car25519(t)

        for (j in 0..1) {
            m[0] = t[0] - 0xffed
            for (i in 1..14) {
                m[i] = t[i] - 0xffff - ((m[i - 1] shr 16) and 1)
                m[i - 1] = m[i - 1] and 0xffff
            }
            m[15] = t[15] - 0x7fff - ((m[14] shr 16) and 1)
            b = (m[15] shr 16) and 1
            m[14] = m[14] and 0xffff
            sel25519(t, m, 1 - b.toInt())
        }
        for (i in 0..15) {
            o[2 * i] = (t[i]).toInt() and 0xff
            o[2 * i + 1] = (t[i]).toInt() shr 8
        }
    }

    private fun car25519(o: LongArray) {
        var v: Long
        var c = 1L
        for (i in 0..15) {
            v = o[i] + c + 65535
            c = floor(v / 65536.0).toLong()
            o[i] = v - c * 65536
        }
        o[0] += c - 1 + 37 * (c - 1)
    }

    private fun par25519(a: LongArray): Int {
        val d = IntArray(32)
        pack25519(d, a)
        return d[0] and 1
    }


    // Note: sm must be n+128.
    fun crypto_sign_direct_rnd(sm: IntArray, m: IntArray, n: Int, sk: IntArray, rnd: IntArray): Int {
        val h = IntArray(64)
        val r = IntArray(64)
        val x = IntArray(64)
        val p = arrayOf(gf(), gf(), gf(), gf())

        // Hash separation.
        sm[0] = 0xfe
        for (i in 1..31) {
            sm[i] = 0xff
        }

        // Secret key.
        sk.copyInto(sm, 32, 0, 32)

        // Message.
        m.copyInto(sm, 64, 0, n)

        // Random suffix.
        rnd.copyInto(sm, n + 64, 0, 64)

        crypto_hash(r, sm, n + 128)
        reduce(r)
        scalarbase(p, r)
        pack(sm, p)

        sk.copyInto(sm, 32, 32, 64)

        crypto_hash(h, sm, n + 64)
        reduce(h)

        // Wipe out random suffix.
        for (i in 0..63) {
            sm[n + 64 + i] = 0
        }

        for (i in 0..63) {
            x[i] = 0
        }

        r.copyInto(x, 0, 0, 32)

        for (i in 0..31) {
            for (j in 0..31) {
                x[i + j] += (h[i] * sk[j]).toInt()
            }
        }

        val tmp = sm.copyOfRange(32, n + 64)
        modL(tmp, x)
        tmp.copyInto(sm, 32)

        return n + 64
    }


    // Like crypto_sign, but uses secret key directly in hash.
    private fun crypto_sign_direct(sm: IntArray, m: IntArray, n: Int, sk: IntArray): Int {
        val h = IntArray(64)
        val r = IntArray(64)
        val x = IntArray(64)
        val p = arrayOf(gf(), gf(), gf(), gf())

        m.copyInto(sm, 64, 0, n)

        sk.copyInto(sm, 32, 0, 32)

        crypto_hash(r, sm.copyOfRange(32, sm.size), n + 32)

        reduce(r)

        scalarbase(p, r)

        pack(sm, p)

        sk.copyInto(sm, 32, 32, 64)

        crypto_hash(h, sm, n + 64)
        reduce(h)

        for (i in 0..63) {
            x[i] = 0
        }

        r.copyInto(x, 0, 0, 32)

        for (i in 0..31) {
            for (j in 0..31) {
                x[i + j] += (h[i] * sk[j]).toInt()
            }
        }

        val tmp = sm.copyOfRange(32, sm.size)
        modL(tmp, x)
        tmp.copyInto(sm, 32)

        return n + 64

    }

}