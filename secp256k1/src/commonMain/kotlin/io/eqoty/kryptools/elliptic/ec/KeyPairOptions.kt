package io.eqoty.kryptools.elliptic.ec

data class KeyPairOptions(
    val priv: UByteArray? = null,
    val pub: UByteArray? = null,
    val privEnc: String? = null,
    val pubEnc: String? = null
)
