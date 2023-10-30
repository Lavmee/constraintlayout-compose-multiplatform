package androidx.constraintlayout.core.platform

actual object System {
    actual fun nanoTime(): Long = kotlin.system.getTimeNanos()
}
