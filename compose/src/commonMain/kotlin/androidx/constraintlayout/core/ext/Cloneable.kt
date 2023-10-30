package androidx.constraintlayout.core.ext

import androidx.constraintlayout.core.parser.CLElement

internal interface Cloneable {
    fun clone(): CLElement
}
