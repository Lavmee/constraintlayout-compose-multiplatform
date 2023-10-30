package androidx.constraintlayout.compose

import kotlin.test.assertEquals

fun assertEquals(expected: Int, actual: Int, absoluteTolerance: Int, message: String? = null) {
    assertEquals(expected.toDouble(), actual.toDouble(), absoluteTolerance.toDouble(), message)
}

fun assertEquals(expected: Int, actual: Int, absoluteTolerance: Double, message: String? = null) {
    assertEquals(expected.toDouble(), actual.toDouble(), absoluteTolerance, message)
}

fun assertEquals(expected: Int, actual: Float, absoluteTolerance: Float, message: String? = null) {
    assertEquals(expected.toFloat(), actual, absoluteTolerance, message)
}
