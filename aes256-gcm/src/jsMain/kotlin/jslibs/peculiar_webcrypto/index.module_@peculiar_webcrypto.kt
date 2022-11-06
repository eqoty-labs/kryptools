@file:JsModule("@peculiar/webcrypto")
@file:JsNonModule

import jslibs.tsstdlib.KeyAlgorithm
import jslibs.tsstdlib.SubtleCrypto
import org.khronos.webgl.ArrayBufferView

external class Crypto : jslibs.tsstdlib.Crypto {
    override var subtle: SubtleCrypto
    override fun <T : ArrayBufferView> getRandomValues(array: T): T
    fun randomUUID(): String
}

external class CryptoKey : jslibs.tsstdlib.CryptoKey {
    override var algorithm: KeyAlgorithm
    override var extractable: Boolean
    override var type: String /* "private" | "public" | "secret" */
    override var usages: Array<String /* "decrypt" | "deriveBits" | "deriveKey" | "encrypt" | "sign" | "unwrapKey" | "verify" | "wrapKey" */>
}