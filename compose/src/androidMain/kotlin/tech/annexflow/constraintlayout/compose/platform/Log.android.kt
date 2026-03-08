package tech.annexflow.constraintlayout.compose.platform

import android.util.Log

internal actual object Log {
    actual fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    actual fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    actual fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }
}
