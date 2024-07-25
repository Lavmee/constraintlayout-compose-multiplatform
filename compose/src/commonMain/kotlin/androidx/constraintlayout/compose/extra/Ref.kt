package androidx.constraintlayout.compose.extra

internal class Ref private constructor() {

    class FloatRef {
        var element: Float = 0f

        override fun toString(): String = element.toString()
    }
}
