import io.eqoty.kryptools.bech32.Bech32

fun addressToBytes(address: String): ByteArray {
    return Bech32.decode(address).data
}

fun addressToUBytes(address: String): UByteArray {
    return Bech32.decode(address).data.toUByteArray()
}
