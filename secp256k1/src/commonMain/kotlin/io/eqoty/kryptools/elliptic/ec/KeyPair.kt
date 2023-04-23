package io.eqoty.kryptools.elliptic.ec

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import io.eqoty.kryptools.elliptic.BasePoint
import io.eqoty.kryptools.elliptic.biginteger.BN

data class KeyPairSignOptions(val canonical: Boolean? = null, val k: BN?)
data class Signature(val r: BN, val s: BN, val recoveryParam: Int?)

class KeyPair(val ec: EC, options: KeyPairOptions) {
    var priv: BN? = null
    var pub: BasePoint<*>? = null

    init {
        if (options.priv != null)
            this.importPrivate(options.priv, options.privEnc)
        if (options.pub != null)
            this.importPublic(options.pub, options.pubEnc)

    }

    fun getPrivate() = priv?.number?.toUByteArray()

    private fun importPublic(key: UByteArray, enc: String?) {
//        if (key.x or key.y) {
//            // Montgomery points only have an `x` coordinate.
//            // Weierstrass/Edwards points on the other hand have both `x` and
//            // `y` coordinates.
//            if (this.ec.curve.type === 'mont') {
//                assert(key.x, 'Need x coordinate');
//            } else if (this.ec.curve.type === 'short' ||
//                this.ec.curve.type === 'edwards') {
//                assert(key.x && key.y, 'Need both x and y coordinate');
//            }
//            this.pub = this.ec.curve.point(key.x, key.y);
//            return;
//        }
        pub = ec.curve.decodePoint(key, enc)
    }

    private fun importPrivate(key: UByteArray, enc: String?) {
        priv = BN(BigInteger.fromUByteArray(key, Sign.POSITIVE))

        // Ensure that the priv won't be bigger than n, otherwise we may fail
        // in fixed multiplication method
        this.priv = this.priv!!.mod(this.ec.n!!)
    }

    fun validate(): Result<Unit> {
        val pub = getPublic()

        if (pub.isInfinity())
            return Result.failure(Error("Invalid public key"))
        if (!pub.validate())
            return Result.failure(Error("Public key is not a point"))
        if (!pub.mul(ec.curve.n!!).isInfinity())
            return Result.failure(Error("Public key * N != O"))

        return Result.success(Unit)
    }

    private fun getPublic(): BasePoint<*> {
        if (pub == null)
            pub = ec.g!!.mul(this.priv!!)

        return pub!!
    }

    fun getPublicEncoded(compact: Boolean = false): UByteArray {
        val pub = getPublic()
        return pub.encode(compact)
    }

    fun sign(msg: UByteArray, enc: String? = null, options: KeyPairSignOptions? = null): Signature =
        ec.sign(msg, this, enc, options)

    companion object {
        fun fromPrivate(ec: EC, priv: UByteArray, enc: String? = null): KeyPair =
            KeyPair(ec, KeyPairOptions(priv = priv, privEnc = enc))


        fun fromPublic(ec: EC, pubkey: UByteArray, enc: String? = null): KeyPair =
            KeyPair(ec, KeyPairOptions(pub = pubkey, privEnc = enc))

    }
}