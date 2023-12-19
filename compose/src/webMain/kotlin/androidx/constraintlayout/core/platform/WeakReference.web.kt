package androidx.constraintlayout.core.platform

actual class WeakReference<T : Any> actual constructor(referred: T) {
    private val workaroundReference: T = referred
    actual fun get(): T? = workaroundReference
    actual fun clear() {}
}
