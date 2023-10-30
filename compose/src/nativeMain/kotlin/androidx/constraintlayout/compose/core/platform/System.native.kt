package androidx.constraintlayout.compose.core.platform

actual object System {
    actual fun nanoTime(): Long = kotlin.system.getTimeNanos()
}
