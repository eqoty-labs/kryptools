// Curve25519 signatures (and also key agreement)
// like in the early Axolotl.
//
// Ported to Kotlin by Miguel Sandro Lucero. miguel.sandro@gmail.com. 2017.05.31
// You can use it under MIT or CC0 license.
//
// Curve25519 signatures idea and math by Trevor Perrin
// https://moderncrypto.org/mail-archive/curves/2014/000205.html
//
// Derived from axlsign.js written by Dmitry Chestnykh. https://github.com/wavesplatform/curve25519-js
package io.eqoty.kryptools.axlsign


class AxlSignLong : AxlSign() {

    override val curve25519 = Curve25519Long()

}