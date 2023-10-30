package androidx.constraintlayout.compose.core.ext

import androidx.constraintlayout.compose.core.parser.CLElement

internal interface Cloneable {
    fun clone(): CLElement
}
