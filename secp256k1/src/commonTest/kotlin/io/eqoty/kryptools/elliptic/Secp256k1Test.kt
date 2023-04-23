package io.eqoty.kryptools.elliptic

import io.eqoty.kryptools.Secp256k1
import io.eqoty.kryptools.toUByteArray
import okio.ByteString.Companion.decodeHex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse


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
        val expectedCompressed =
            "03000d331e7ac60da03d489bedb76523a29998261a82d863f3892ff00642886c8b".decodeHex().toUByteArray()
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
        val expectedCompressed =
            "026a52ad443378103708bb89e828c972f58060c690d2aa65480eedb6f3f56b2057".decodeHex().toUByteArray()
        assertContentEquals(expectedCompressed, pubkeyCompressed)
    }

    @Test
    fun test_valid_failing_key() {
        // todo: issue #2
        val privkey = "08ff5e5dceb88bc8524b7847e38f7154dd3085b0f193f7b33b05cb589c0f758b".decodeHex().toUByteArray()
        val uncompressed = Secp256k1.makeKeypair(privkey).pubkey
        val expectedUncompressed =
            "04100cc44d0e2ad046571c07e046b75f499eb66cf096e3d4e08e1847c447378b797329f352a553620250bae405c26c87e4ee331e41aec890446083356eedb48df0"
                .decodeHex().toUByteArray()

        assertContentEquals(expectedUncompressed, uncompressed)
        val pubkeyCompressed = Secp256k1.compressPubkey(uncompressed)
        val expectedCompressed =
            "02100cc44d0e2ad046571c07e046b75f499eb66cf096e3d4e08e1847c447378b79".decodeHex().toUByteArray()
        assertContentEquals(expectedCompressed, pubkeyCompressed)
    }


    @Test
    fun testInvalidPublicKey() {
        // An invalid secp256k1 public key with wrong length
        val invalidKey = "441dc75d1789d6c1b6e962ae83114c759a315b2c2dce6d8f6ed75c4d3e4e4c"

        // Decode the key from hex
        val publicKey = invalidKey.decodeHex().toUByteArray()

        // Check if the public key is valid
        assertFalse(Secp256k1.validate(publicKey).isSuccess)
    }

    @Test
    fun testPublicKeyUnderflow() {
        // A secp256k1 public key with a y-coordinate equal to zero
        val publicKey = ("04" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "a5f4e7a7724d01c5574eb3e99a80b72df2951c9dcde7a38f2e2ba611cd440a77"
                ).decodeHex().toUByteArray()

        // Check if the public key is valid
        assertFalse(Secp256k1.validate(publicKey).isSuccess)
    }

    @Test
    fun testPublicKeyOverflow() {
        // A secp256k1 public key with a y-coordinate greater than the order of the curve
        val publicKey = ("04" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "d1f865a4918e919e98640a697b9c23d7f372c14775d96a247d2c45b01d7ee8ce"
                ).decodeHex().toUByteArray()

        // Check if the public key is valid
        assertFalse(Secp256k1.validate(publicKey).isSuccess)
    }


    @Test
    fun testPrivateKeyOverflow() {
        // A secp256k1 private key greater than the order of the curve
        val privateKey = "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141".decodeHex().toUByteArray()

        // Check if the key is valid
        assertFalse(Secp256k1.validatePrivateKey(privateKey).isSuccess)
    }

    @Test
    fun testPrivateKeyUnderflow() {
        // A secp256k1 private key equal to zero
        val privateKey = "0000000000000000000000000000000000000000000000000000000000000000".decodeHex().toUByteArray()

        // Check if the key is valid
        assertFalse(Secp256k1.validatePrivateKey(privateKey).isSuccess)
    }



}