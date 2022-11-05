package io.eqoty

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class Tests {

    @BeforeTest
    fun beforeEach() = runTest {
        platformBeforeEach()
    }

}