package io.eqoty

import io.eqoty.kryptools.aessiv.AesSIV
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class Tests {

    @Test
    fun test() = runTest {
        val aesSIV = AesSIV()
        val txEncryptionKey = ubyteArrayOf(
            0x01u, 0x23u, 0x45u, 0x67u, 0x89u, 0xABu, 0xCDu, 0xEFu,
            0x01u, 0x23u, 0x45u, 0x67u, 0x89u, 0xABu, 0xCDu, 0xEFu,
            0x01u, 0x23u, 0x45u, 0x67u, 0x89u, 0xABu, 0xCDu, 0xEFu,
            0x01u, 0x23u, 0x45u, 0x67u, 0x89u, 0xABu, 0xCDu, 0xEFu
        )
        val plaintext = "The cake is a lie".encodeToByteArray().asUByteArray()
        val associatedData = ubyteArrayOf(1u, 2u, 3u, 4u, 5u, 6u, 7u, 8u, 9u, 10u, 11u, 12u, 13u, 14u, 15u, 16u)
        val ciphertext = aesSIV.encrypt(txEncryptionKey, plaintext, associatedData)
        println("ciphertext: $ciphertext")
        val decrypted = aesSIV.decrypt(txEncryptionKey, ciphertext, associatedData)
        println("decrypted: $decrypted")
        assertContentEquals(plaintext, decrypted)
    }

}