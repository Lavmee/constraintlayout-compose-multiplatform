package androidx.constraintlayout.compose.platform

import androidx.constraintlayout.core.platform.System

internal actual object System {
    actual fun nanoTime(): Long = System.nanoTime()
    actual val err: PrintStream
        get() = object : PrintStream {
            override fun println(value: String) {
                println(value)
            }
        }
}
