package io.eqoty.kryptools.axlsign

abstract class Curve25519 {
    abstract fun sign(sm: IntArray, m: IntArray, n: Int, sk: IntArray, opt_rnd: IntArray?): Int
    abstract fun sign_open(m: IntArray, sm: IntArray, n: Int, pk: IntArray): Int

    abstract fun crypto_scalarmult(q: IntArray, n: IntArray, p: IntArray): Int
    fun crypto_scalarmult_base(q: IntArray, n: IntArray): Int {
        return crypto_scalarmult(q, n, _9)
    }


    protected fun reduce(r: IntArray) {
        val x = IntArray(64)
        r.copyInto(x, 0, 0, 64)
        for (i in 0..63) {
            r[i] = 0
        }
        modL(r, x)
    }

    protected fun modL(r: IntArray, x: IntArray) {

        var carry: Int

        for (i in 63 downTo 32) {
            carry = 0
            var j = i - 32
            val k = i - 12
            while (j < k) {
                x[j] += carry - 16 * x[i] * L[j - (i - 32)]
                carry = (x[j] + 128) shr 8
                x[j] -= carry * 256
                ++j
            }
            x[j] += carry
            x[i] = 0
        }

        carry = 0
        for (j in 0..31) {
            x[j] += carry - (x[31] shr 4) * L[j]
            carry = x[j] shr 8
            x[j] = x[j] and 255
        }

        for (j in 0..31) {
            x[j] -= carry * L[j]
        }

        for (i in 0..31) {
            x[i + 1] += x[i] shr 8
            r[i] = x[i] and 255
        }

    }

}
