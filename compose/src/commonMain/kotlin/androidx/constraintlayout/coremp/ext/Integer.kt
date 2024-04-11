package androidx.constraintlayout.coremp.ext

import kotlin.math.roundToInt

internal object Integer {
    fun compare(x: Int, y: Int): Int {
        return if (x < y) {
            -1
        } else if (x == y) {
            0
        } else {
            1
        }
    }
}

internal fun Float.roundToIntOrZero() = if (this.isNaN()) 0 else roundToInt()
