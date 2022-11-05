package io.eqoty.kryptotools.aes256gcm

import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse

class Aes256GcmTests {


    @Test
    fun encryptsAndDecryptsOk() {
        val rand = Random(0)
        val aes256Gcm = Aes256Gcm()
        val iv = rand.nextUBytes(12)
        val key = rand.nextUBytes(16)
        val plainText = rand.nextUBytes(256)
        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
        val decryptedPlainText = aes256Gcm.decrypt(iv, key, cypherText)

        assertContentEquals(plainText, decryptedPlainText)
    }

    @Test
    fun encryptsAndDecrypts3BlocksPartiallyOk() {
        val rand = Random(0)
        val aes256Gcm = Aes256Gcm()
        val iv = rand.nextUBytes(12)
        val key = rand.nextUBytes(16)
        val blocks = 3
        val plainText = rand.nextUBytes(16 * blocks)
        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
        assertFalse(plainText.contentEquals(cypherText), "plainText is the same as cypherText")

        val firstBlockCypherText = cypherText.copyOfRange(0, 16)
        var decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, firstBlockCypherText, 0)
        assertContentEquals(plainText.copyOfRange(0, 16), decryptedPlainText)

        val secondBlockCypherText = cypherText.copyOfRange(16, 32)
        decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, secondBlockCypherText, 1)
        assertContentEquals(plainText.copyOfRange(16, 32), decryptedPlainText)

        val thirdBlockCypherText = cypherText.copyOfRange(32, 48)
        decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, thirdBlockCypherText, 2)
        assertContentEquals(plainText.copyOfRange(32, 48), decryptedPlainText)
    }

    /***
     * tests when counter bytes can't be represented by one byte
     */
    @Test
    fun encryptsAndDecrypts257BlocksPartiallyOk() {
        val rand = Random(0)
        val aes256Gcm = Aes256Gcm()
        val iv = rand.nextUBytes(12)
        val key = rand.nextUBytes(16)
        val blocks = 5000
        val plainText = rand.nextUBytes(16 * blocks)
        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
        assertFalse(plainText.contentEquals(cypherText), "plainText is the same as cypherText")

        for (i in 0 until blocks) {
            val blockOfCypherText = cypherText.copyOfRange(i * 16, (i + 1) * 16)
            val decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, blockOfCypherText, i)
            assertContentEquals(plainText.copyOfRange(i * 16, (i + 1) * 16), decryptedPlainText)
        }
    }

    /***
     * tests when counter bytes can't be represented by three bytes
     */
    @Test
    fun encryptsAndDecrypts65536BlocksPartiallyOk() {
        val rand = Random(0)
        val aes256Gcm = Aes256Gcm()
        val iv = rand.nextUBytes(12)
        val key = rand.nextUBytes(16)
        val blocks = 65536
        val plainText = rand.nextUBytes(16 * blocks)
        val cypherText = aes256Gcm.encrypt(iv, key, plainText)
        assertFalse(plainText.contentEquals(cypherText), "plainText is the same as cypherText")

        for (i in 0 until blocks) {
            val blockOfCypherText = cypherText.copyOfRange(i * 16, (i + 1) * 16)
            val decryptedPlainText = aes256Gcm.decryptAtIndexUnauthenticated(iv, key, blockOfCypherText, i)
            assertContentEquals(plainText.copyOfRange(i * 16, (i + 1) * 16), decryptedPlainText)
        }
    }


}