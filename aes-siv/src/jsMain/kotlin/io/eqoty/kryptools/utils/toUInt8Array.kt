package io.eqoty.kryptools.utils

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

fun UByteArray.toUInt8Array(): Uint8Array = Uint8Array(toByteArray().toTypedArray())


fun Uint8Array.toUByteArray(): UByteArray {
    if (length.asDynamic() == undefined) {
        println("Error")
    }
    val result = UByteArray(length)
    for (i in 0 until length) {
        result[i] = get(i).toUByte()
    }

    return result
}
