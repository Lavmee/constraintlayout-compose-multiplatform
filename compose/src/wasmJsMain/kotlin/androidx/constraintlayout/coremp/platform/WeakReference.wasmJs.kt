package androidx.constraintlayout.coremp.platform

actual class WeakReference<T : Any>(private var ref: WeakRef?) {
    actual constructor(referred: T) : this(WeakRef(referred.toJsReference()))

    actual fun get(): T? = ref?.deref()?.unsafeCast<JsReference<T>>()?.get()

    actual fun clear() {
        ref = null
    }
}

external class WeakRef(target: JsAny) {
    fun deref(): JsAny
}
