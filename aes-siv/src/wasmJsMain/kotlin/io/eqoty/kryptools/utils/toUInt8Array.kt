package io.eqoty.kryptools.utils

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

fun UByteArray.toUInt8Array(): Uint8Array {
    return with(asByteArray()) {
        val arr = Uint8Array(size)
        repeat(size) { i ->
            arr[i] = this[i]
        }
        arr
    }
}


fun Uint8Array.toUByteArray(): UByteArray {
    val arr = ByteArray(length)
    repeat(length) { i ->
        arr[i] = this[i]
    }
    return arr.asUByteArray()
}
