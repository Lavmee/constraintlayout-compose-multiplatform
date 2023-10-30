package androidx.constraintlayout.compose.core.platform

expect object System {
    fun nanoTime(): Long
}
