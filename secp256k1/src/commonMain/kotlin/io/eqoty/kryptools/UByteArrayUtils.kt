package io.eqoty.kryptools

import okio.ByteString
import okio.ByteString.Companion.toByteString


fun UByteArray.toIntArray(): IntArray =
    map { it.toInt() }.toIntArray()

fun IntArray.toUByteArray(): UByteArray =
    map { it.toUByte() }.toUByteArray()

fun UByteArray.asByteString(): ByteString =
    asByteArray().toByteString()

fun UByteArray.decodeToString(): String =
    asByteArray().decodeToString()

fun ByteString.toUByteArray(): UByteArray =
    toByteArray().asUByteArray()

fun UByteArray.getPadded(length: Int): UByteArray {
    val paddingLength = length - this.size
    if (paddingLength < 0) {
        throw Error("Length smaller than this.length")
    }
    val padding = UByteArray(paddingLength) { 0u }
    return padding + this
}