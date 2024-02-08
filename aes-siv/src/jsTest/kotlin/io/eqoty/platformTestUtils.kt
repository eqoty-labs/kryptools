package io.eqoty

import io.eqoty.kryptools.aessiv.createWindowBroadcaster
import io.eqoty.kryptools.aessiv.destroyWindowBroadcaster
import io.eqoty.kryptools.aessiv.windowIsSetup
import jslibs.happydom.GlobalRegistrator
import jslibs.tsstdlib.Crypto
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


// https://github.com/whyoleg/cryptography-kotlin/blob/d524143a0719e6926b0ae190977a7341673fa718/cryptography-random/src/jsMain/kotlin/CryptographyRandom.js.kt
//language=JavaScript
private fun getCrypto(): Crypto {
    return js(
        code = """
    
        var isNodeJs = typeof process !== 'undefined' && process.versions != null && process.versions.node != null
        if (isNodeJs) {
            return (eval('require')('node:crypto').webcrypto);
        } else {
            return (window ? (window.crypto ? window.crypto : window.msCrypto) : self.crypto);
        }
    
               """
    ).unsafeCast<Crypto>()
}


fun setupFakeWindow() {
    GlobalRegistrator.register()
    window.asDynamic().crypto = getCrypto()
}

fun destroyFakeWindow() {
    window.asDynamic().crypto = null
    GlobalRegistrator.unregister()
}


var isFirstRunAndNoWindow = !windowIsSetup.value

actual suspend fun platformBeforeEach() {
    if (isFirstRunAndNoWindow) {
        isFirstRunAndNoWindow = false
        MainScope().launch {
            createWindowBroadcaster.collect {
                setupFakeWindow()
                windowIsSetup.emit(true)
            }
        }

        MainScope().launch {
            destroyWindowBroadcaster.collect {
                destroyFakeWindow()
                windowIsSetup.value = false
            }
        }
    }
}
