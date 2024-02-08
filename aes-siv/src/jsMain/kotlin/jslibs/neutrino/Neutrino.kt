@file:JsModule("@solar-republic/neutrino") @file:JsNonModule

package jslibs.neutrino

import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

/**
 * Encrypt a given plaintext using AES-128 SIV with a properly-formatted key
 * @param atu8_key - an AES-128 SIV key
 * @param atu8_plaintext - the plaintext input
 * @param a_ad - optional associated data (defaults to `[new Uint8Array(0)]` for Secret Network)
 * @returns ciphertext output
 */
external fun aes_128_siv_encrypt(
    atu8_key: Uint8Array, atu8_plaintext: Uint8Array, a_ad: Array<Uint8Array> = definedExternally
): Promise<Uint8Array>


/**
 * Decrypt a given ciphertext using AES-128 SIV with a properly-formatted key
 * @param atu8_key - an AES-128 SIV key
 * @param atu8_payload - the input payload
 * @param a_ad - optional associated data (defaults to `[new Uint8Array(0)]` for Secret Network)
 * @returns plaintext output
 */
// eslint-disable-next-line @typescript-eslint/naming-convention
external fun aes_128_siv_decrypt(
    atu8_key: Uint8Array, atu8_payload: Uint8Array, a_ad: Array<Uint8Array> = definedExternally
): Promise<Uint8Array>