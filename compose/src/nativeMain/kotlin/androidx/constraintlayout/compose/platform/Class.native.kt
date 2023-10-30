package androidx.constraintlayout.compose.platform

import kotlin.reflect.KClass

internal actual val Any.javaKlass: KClass<out Any>
    get() = this::class
