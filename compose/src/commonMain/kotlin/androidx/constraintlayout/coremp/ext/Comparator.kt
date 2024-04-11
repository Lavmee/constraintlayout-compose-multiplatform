package androidx.constraintlayout.coremp.ext

internal fun compare(f1: Float, f2: Float): Int {
    if (f1 < f2) return -1

    if (f1 > f2) return 1

    val thisBits: Int = f1.toBits()
    val anotherBits: Int = f2.toBits()

    return (
        if (thisBits == anotherBits) {
            0
        } else if (thisBits < anotherBits) {
            -1
        } else {
            1
        }
        )
}
