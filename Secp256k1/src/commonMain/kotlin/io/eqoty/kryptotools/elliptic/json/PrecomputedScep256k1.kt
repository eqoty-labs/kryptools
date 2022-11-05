package io.eqoty.kryptotools.elliptic.json

import io.eqoty.kryptotools.elliptic.json.Doubles
import io.eqoty.kryptotools.elliptic.json.Naf

@kotlinx.serialization.Serializable
data class PrecomputedScep256k1(
    val doubles: Doubles,
    val naf: Naf
)
