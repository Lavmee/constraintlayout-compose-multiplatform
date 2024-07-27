package androidx.constraintlayout.compose.extra

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.constraintlayout.core.state.Transition
import androidx.constraintlayout.core.state.WidgetFrame

internal actual fun DrawScope.drawPathsPlatform(
    parentWidth: Float,
    parentHeight: Float,
    startFrame: WidgetFrame,
    drawPath: Boolean,
    drawKeyPositions: Boolean,
    transition: Transition,
) {
}

internal actual fun DrawScope.drawFrameDebugPlatform(
    parentWidth: Float,
    parentHeight: Float,
    startFrame: WidgetFrame,
    endFrame: WidgetFrame,
    pathEffect: PathEffect,
    color: Color,
    transition: Transition,
) {
}

internal actual fun DrawScope.drawFramePlatform(
    frame: WidgetFrame,
    pathEffect: PathEffect,
    color: Color,
) {
}
