package androidx.constraintlayout.coremp.ext

import kotlin.math.pow

internal object Math {
    private const val DEGREES_TO_RADIANS = 0.017453292519943295
    private const val RADIANS_TO_DEGREES = 57.29577951308232
    fun toRadians(angdeg: Double): Double = angdeg * DEGREES_TO_RADIANS
    fun toDegrees(angrad: Double): Double = angrad * RADIANS_TO_DEGREES
    fun pow(a: Double, b: Double): Double {
        return a.pow(b)
    }
}
