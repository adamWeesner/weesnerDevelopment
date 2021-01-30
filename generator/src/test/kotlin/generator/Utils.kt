package generator

import kotlin.test.assertEquals

inline infix fun <reified A, reified B> A.shouldBe(expected: B) = assertEquals(expected, this)
