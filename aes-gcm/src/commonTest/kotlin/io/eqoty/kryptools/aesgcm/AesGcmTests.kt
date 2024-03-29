package io.eqoty.kryptools.aesgcm

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse


@ExperimentalCoroutinesApi
class AesGcmTests {

    @Test
    fun encryptsAndDecryptsOk() = runTest {
        val rand = Random(0)
        val aesGcm = AesGcm()
        val key = rand.nextUBytes(32)
        val plainText = rand.nextUBytes(256)
        val result = aesGcm.encrypt(key, plainText)
        val decryptedPlainText = aesGcm.decrypt(result.iv, key, result.cyphertext)
        val tagBytesSize = 16
        assertEquals(plainText.size, result.cyphertext.size - tagBytesSize)
        assertContentEquals(plainText, decryptedPlainText)
    }

    @Test
    fun encryptsAndDecrypts3BlocksPartiallyOk() = runTest {
        val rand = Random(0)
        val aesGcm = AesGcm()
        val key = rand.nextUBytes(32)
        val blocks = 3
        val plainText = rand.nextUBytes(16 * blocks)
        val encryptResult = aesGcm.encrypt(key, plainText)
        assertFalse(plainText.contentEquals(encryptResult.cyphertext), "plainText is the same as cypherText")

        val firstBlockCypherText = encryptResult.cyphertext.sliceArray(0 until 16)
        var decryptedPlainText = aesGcm.decryptAtIndexUnauthenticated(encryptResult.iv, key, firstBlockCypherText, 0)
        assertContentEquals(plainText.sliceArray(0 until 16), decryptedPlainText)

        val secondBlockCypherText = encryptResult.cyphertext.sliceArray(16 until 32)
        decryptedPlainText = aesGcm.decryptAtIndexUnauthenticated(encryptResult.iv, key, secondBlockCypherText, 1)
        assertContentEquals(plainText.sliceArray(16 until 32), decryptedPlainText)

        val thirdBlockCypherText = encryptResult.cyphertext.sliceArray(32 until 48)
        decryptedPlainText = aesGcm.decryptAtIndexUnauthenticated(encryptResult.iv, key, thirdBlockCypherText, 2)
        assertContentEquals(plainText.sliceArray(32 until 48), decryptedPlainText)
    }

    /***
     * tests when counter bytes can't be represented by one byte
     */
    @Test
    fun encryptsAndDecrypts257BlocksPartiallyOk() = runTest {
        val rand = Random(0)
        val aesGcm = AesGcm()
        val key = rand.nextUBytes(32)
        val blocks = 257
        val plainText = rand.nextUBytes(16 * blocks)
        val encryptResult = aesGcm.encrypt(key, plainText)
        assertFalse(plainText.contentEquals(encryptResult.cyphertext), "plainText is the same as cypherText")

        for (i in 0 until blocks) {
            val blockOfCypherText = encryptResult.cyphertext.sliceArray(i * 16 until (i + 1) * 16)
            val decryptedPlainText =
                aesGcm.decryptAtIndexUnauthenticated(encryptResult.iv, key, blockOfCypherText, i)
            assertContentEquals(plainText.sliceArray(i * 16 until (i + 1) * 16), decryptedPlainText)
        }
    }

    /***
     * tests when counter bytes can't be represented by three bytes
     */
    @Test
    fun encryptsAndDecrypts65536BlocksPartiallyOk() = runTest {
        val rand = Random(0)
        val aesGcm = AesGcm()
        val key = rand.nextUBytes(32)
        val blocks = 65536
        val plainText = rand.nextUBytes(16 * blocks)
        val encryptResult = aesGcm.encrypt(key, plainText)
        assertFalse(plainText.contentEquals(encryptResult.cyphertext), "plainText is the same as cypherText")

        for (i in 0 until blocks) {
            val blockOfCypherText = encryptResult.cyphertext.sliceArray(i * 16 until (i + 1) * 16)
            val decryptedPlainText =
                aesGcm.decryptAtIndexUnauthenticated(encryptResult.iv, key, blockOfCypherText, i)
            assertContentEquals(plainText.sliceArray(i * 16 until (i + 1) * 16), decryptedPlainText)
        }
    }


}