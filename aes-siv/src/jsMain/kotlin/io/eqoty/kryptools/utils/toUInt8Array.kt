package io.eqoty.kryptools.utils

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

fun UByteArray.asUInt8Array(): Uint8Array = Uint8Array(asByteArray().unsafeCast<Int8Array>().buffer)


fun Uint8Array.asUByteArray(): UByteArray = Int8Array(this.buffer).unsafeCast<ByteArray>().asUByteArray()
