package io.eqoty.kryptools.bech32


fun addressToBytes(address: String): ByteArray {
    return Bech32.decode(address).data
}

fun addressToUBytes(address: String): UByteArray {
    return Bech32.decode(address).data.asUByteArray()
}
