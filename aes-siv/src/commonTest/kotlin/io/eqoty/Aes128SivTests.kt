package io.eqoty

import io.eqoty.kryptools.aessiv.AesSIV
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Aes128SivTests {

    private val testVectors = listOf(
        TestVector(
            name = "Deterministic Authenticated Encryption Example",
            key = "fffefdfcfbfaf9f8f7f6f5f4f3f2f1f0f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff".hexToBytes(),
            ad = listOf("101112131415161718191a1b1c1d1e1f2021222324252627".hexToBytes()),
            plaintext = "112233445566778899aabbccddee".hexToBytes(),
            ciphertext = "85632d07c6e8f37f950acd320a2ecc9340c02b9690c4dc04daef7f6afe5c".hexToBytes()
        ),
        TestVector(
            name = "Nonce-Based Authenticated Encryption Example",
            key = "7f7e7d7c7b7a79787776757473727170404142434445464748494a4b4c4d4e4f".hexToBytes(),
            ad = listOf(
                "00112233445566778899aabbccddeeffdeaddadadeaddadaffeeddccbbaa99887766554433221100".hexToBytes(),
                "102030405060708090a0".hexToBytes(),
                "09f911029d74e35bd84156c5635688c0".hexToBytes()
            ),
            plaintext = "7468697320697320736f6d6520706c61696e7465787420746f20656e6372797074207573696e67205349562d414553".hexToBytes(),
            ciphertext = "7bdb6e3b432667eb06f4d14bff2fbd0fcb900f2fddbe404326601965c889bf17dba77ceb094fa663b7a3f748ba8af829ea64ad544a272e9c485b62a3fd5c0d".hexToBytes()
        ),
        TestVector(
            name = "Empty Authenticated Data And Plaintext Example",
            key = "fffefdfcfbfaf9f8f7f6f5f4f3f2f1f0f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff".hexToBytes(),
            ad = listOf(),
            plaintext = ubyteArrayOf(),
            ciphertext = "f2007a5beb2b8900c588a7adf599f172".hexToBytes()
        ),
        TestVector(
            name = "Empty Authenticated Data And Block-Size Plaintext Example",
            key = "fffefdfcfbfaf9f8f7f6f5f4f3f2f1f0f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff".hexToBytes(),
            ad = listOf(),
            plaintext = "00112233445566778899aabbccddeeff".hexToBytes(),
            ciphertext = "f304f912863e303d5b540e5057c7010c942ffaf45b0e5ca5fb9a56a5263bb065".hexToBytes()
        )
    )

    @Test
    fun runTestVectors() = runTest {
        val aesSIV = AesSIV()
        testVectors.forEachIndexed { index, vector ->
            println("Testing Vector #${index + 1}: ${vector.name}")
            val decrypted = aesSIV.decrypt(vector.key, vector.ciphertext, vector.ad)
            assertContentEquals(vector.plaintext, decrypted, "Decryption failed for vector: ${vector.name}")
            println("decrypted: ${decrypted.joinToString("") { it.toString(16).padStart(2, '0') }}")
            val encrypted = aesSIV.encrypt(vector.key, vector.plaintext, vector.ad)
            println("encrypted: ${encrypted.joinToString("") { it.toString(16).padStart(2, '0') }}")
            assertContentEquals(vector.ciphertext, encrypted, "Encryption failed for vector: ${vector.name}")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.hexToBytes() = this.hexToUByteArray()
}

data class TestVector(
    val name: String,
    val key: UByteArray,
    val ad: List<UByteArray>,
    val plaintext: UByteArray,
    val ciphertext: UByteArray
)
