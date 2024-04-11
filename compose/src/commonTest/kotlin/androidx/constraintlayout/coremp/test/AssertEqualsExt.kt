package androidx.constraintlayout.coremp.test

import kotlin.test.assertEquals

internal fun assertEquals(
    expected: Int,
    actual: Int,
    absoluteTolerance: Int,
    message: String? = null,
) {
    assertEquals(expected.toDouble(), actual.toDouble(), absoluteTolerance.toDouble(), message)
}

internal fun assertEquals(
    expected: Int,
    actual: Int,
    absoluteTolerance: Double,
    message: String? = null,
) {
    assertEquals(expected.toDouble(), actual.toDouble(), absoluteTolerance, message)
}

internal fun assertEquals(
    expected: Int,
    actual: Float,
    absoluteTolerance: Float,
    message: String? = null,
) {
    assertEquals(expected.toFloat(), actual, absoluteTolerance, message)
}
