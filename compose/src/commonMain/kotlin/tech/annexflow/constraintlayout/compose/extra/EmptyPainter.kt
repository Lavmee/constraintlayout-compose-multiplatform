package tech.annexflow.constraintlayout.compose.extra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

internal class EmptyPainter : Painter() {
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {}
}

@Composable
internal fun rememberEmptyPainter(): Painter = remember { EmptyPainter() }
