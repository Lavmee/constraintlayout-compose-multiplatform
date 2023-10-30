package androidx.constraintlayout.core.platform

actual object System {
    actual fun nanoTime(): Long = java.lang.System.nanoTime()
}
