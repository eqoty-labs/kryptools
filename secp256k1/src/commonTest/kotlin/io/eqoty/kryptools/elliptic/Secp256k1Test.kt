package io.eqoty.kryptools.elliptic

import io.eqoty.kryptools.Secp256k1
import io.eqoty.kryptools.toUByteArray
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


class Secp256k1Test {
    @Test
    fun generates_secp256k1_pubkey_when_x_coordinate_has_empty_first_byte() {
        val privkey = "57abd2261fd3f6a3dbe755f0994ee309f7cc6767d6e35113c41699ffc110e599".decodeHex().toUByteArray()
        val uncompressed = Secp256k1.makeKeypair(privkey).pubkey
        val expectedUncompressed =
            "04000d331e7ac60da03d489bedb76523a29998261a82d863f3892ff00642886c8b218407acba7ae1f69eb30fa554f957bd216debb9ed1c049989b6f9a31cde1e3f"
                .decodeHex().toUByteArray()

        assertContentEquals(expectedUncompressed, uncompressed)
        val pubkeyCompressed = Secp256k1.compressPubkey(uncompressed)
        val expectedCompressed = "03000d331e7ac60da03d489bedb76523a29998261a82d863f3892ff00642886c8b".decodeHex().toUByteArray()
        assertContentEquals(expectedCompressed, pubkeyCompressed)
    }

    @Test
    fun generates_secp256k1_pubkey_when_y_coordinate_has_empty_first_byte() {
        val privkey = "cecd5536b8a0594afa199b77f8702a80eb7af2bbf86f79a6a6c9886470572e0d".decodeHex().toUByteArray()
        val uncompressed = Secp256k1.makeKeypair(privkey).pubkey
        val expectedUncompressed =
            "046a52ad443378103708bb89e828c972f58060c690d2aa65480eedb6f3f56b205700b8cf8cf24a0808b23dd10cca8f90ba00cef691bd67681ab23f585f8bbac844"
                .decodeHex().toUByteArray()

        assertContentEquals(expectedUncompressed, uncompressed)
        val pubkeyCompressed = Secp256k1.compressPubkey(uncompressed)
        val expectedCompressed = "026a52ad443378103708bb89e828c972f58060c690d2aa65480eedb6f3f56b2057".decodeHex().toUByteArray()
        assertContentEquals(expectedCompressed, pubkeyCompressed)
    }


}