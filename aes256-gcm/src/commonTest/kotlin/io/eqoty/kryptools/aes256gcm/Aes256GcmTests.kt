package io.eqoty.kryptools.aes256gcm

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse


@ExperimentalCoroutinesApi
class Aes256GcmTests {


    @Test
    fun encryptsAndDecryptsOk() = runTest {
        val rand = Random(0)
        val aes256Gcm = Aes256Gcm()
        val key = rand.nextUBytes(16)
        val plainText = rand.nextUBytes(256)
        val result = aes256Gcm.encrypt(key, plainText)
        val decryptedPlainText = aes256Gcm.decrypt(result.iv, key, result.cyphertext)
        val tagBytesSize = 16
        assertEquals(plainText.size, result.cyphertext.size - tagBytesSize)
        assertContentEquals(plainText, decryptedPlainText)
    }
//
//    @Test
//    fun encryptsAndDecrypts3BlocksPartiallyOk() = runTest {
//        val rand = Random(0)
//        val aes256Gcm = Aes256Gcm()
//        val iv = rand.nextUBytes(12)
//        val key = rand.nextUBytes(16)
//        val blocks = 3
//        val plainText = rand.nextUBytes(16 * blocks)
//        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
//        assertFalse(plainText.contentEquals(cypherText), "plainText is the same as cypherText")
//
//        val firstBlockCypherText = cypherText.sliceArray(0 until 16)
//        var decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, firstBlockCypherText, 0)
//        assertContentEquals(plainText.sliceArray(0 until 16), decryptedPlainText)
//
//        val secondBlockCypherText = cypherText.sliceArray(16 until 32)
//        decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, secondBlockCypherText, 1)
//        assertContentEquals(plainText.sliceArray(16 until 32), decryptedPlainText)
//
//        val thirdBlockCypherText = cypherText.sliceArray(32 until 48)
//        decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, thirdBlockCypherText, 2)
//        assertContentEquals(plainText.sliceArray(32 until 48), decryptedPlainText)
//    }
//
//    /***
//     * tests when counter bytes can't be represented by one byte
//     */
//    @Test
//    fun encryptsAndDecrypts257BlocksPartiallyOk() = runTest {
//        val rand = Random(0)
//        val aes256Gcm = Aes256Gcm()
//        val iv = rand.nextUBytes(12)
//        val key = rand.nextUBytes(16)
//        val blocks = 257
//        val plainText = rand.nextUBytes(16 * blocks)
//        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
//        assertFalse(plainText.contentEquals(cypherText), "plainText is the same as cypherText")
//
//        for (i in 0 until blocks) {
//            val blockOfCypherText = cypherText.sliceArray(i * 16 until (i + 1) * 16)
//            val decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, blockOfCypherText, i)
//            assertContentEquals(plainText.sliceArray(i * 16 until (i + 1) * 16), decryptedPlainText)
//        }
//    }
//
//    /***
//     * tests when counter bytes can't be represented by three bytes
//     */
//    @Test
//    fun encryptsAndDecrypts65536BlocksPartiallyOk() = runTest {
//        val rand = Random(0)
//        val aes256Gcm = Aes256Gcm()
//        val iv = rand.nextUBytes(12)
//        val key = rand.nextUBytes(16)
//        val blocks = 65536
//        val plainText = rand.nextUBytes(16 * blocks)
//        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
//        assertFalse(plainText.contentEquals(cypherText), "plainText is the same as cypherText")
//
//        for (i in 0 until blocks) {
//            val blockOfCypherText = cypherText.sliceArray(i * 16 until (i + 1) * 16)
//            val decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, blockOfCypherText, i)
//            assertContentEquals(plainText.sliceArray(i * 16 until (i + 1) * 16), decryptedPlainText)
//        }
//    }


}