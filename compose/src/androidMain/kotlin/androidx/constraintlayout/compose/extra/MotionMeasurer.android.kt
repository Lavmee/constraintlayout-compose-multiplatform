package androidx.constraintlayout.compose.extra

import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.constraintlayout.compose.MotionRenderDebug
import androidx.constraintlayout.core.motion.Motion
import androidx.constraintlayout.core.state.Transition
import androidx.constraintlayout.core.state.WidgetFrame

internal actual fun DrawScope.drawPathsPlatform(
    parentWidth: Float,
    parentHeight: Float,
    startFrame: WidgetFrame,
    drawPath: Boolean,
    drawKeyPositions: Boolean,
    transition: Transition
) {
    val debugRender = MotionRenderDebug(23f)
    debugRender.basicDraw(
        drawContext.canvas.nativeCanvas,
        transition.getMotion(startFrame.widget?.stringId),
        1000,
        parentWidth.toInt(),
        parentHeight.toInt(),
        drawPath,
        drawKeyPositions
    )
}

internal actual fun DrawScope.drawFrameDebugPlatform(
    parentWidth: Float,
    parentHeight: Float,
    startFrame: WidgetFrame,
    endFrame: WidgetFrame,
    pathEffect: PathEffect,
    color: Color,
    transition: Transition
) {
    drawFramePlatform(startFrame, pathEffect, color)
    drawFramePlatform(endFrame, pathEffect, color)
    val numKeyPositions = transition.getNumberKeyPositions(startFrame)
    val debugRender = MotionRenderDebug(23f)

    debugRender.draw(
        drawContext.canvas.nativeCanvas,
        transition.getMotion(startFrame.widget?.stringId),
        1000,
        Motion.DRAW_PATH_BASIC,
        parentWidth.toInt(),
        parentHeight.toInt()
    )
    if (numKeyPositions == 0) {
        //            drawLine(
        //                start = Offset(startFrame.centerX(), startFrame.centerY()),
        //                end = Offset(endFrame.centerX(), endFrame.centerY()),
        //                color = color,
        //                strokeWidth = 3f,
        //                pathEffect = pathEffect
        //            )
    } else {
        val x = FloatArray(numKeyPositions)
        val y = FloatArray(numKeyPositions)
        val pos = FloatArray(numKeyPositions)
        transition.fillKeyPositions(startFrame, x, y, pos)

        for (i in 0..numKeyPositions - 1) {
            val keyFrameProgress = pos[i] / 100f
            val frameWidth =
                ((1 - keyFrameProgress) * startFrame.width()) +
                        (keyFrameProgress * endFrame.width())
            val frameHeight =
                ((1 - keyFrameProgress) * startFrame.height()) +
                        (keyFrameProgress * endFrame.height())
            val curX = x[i] * parentWidth + frameWidth / 2f
            val curY = y[i] * parentHeight + frameHeight / 2f
            //                drawLine(
            //                    start = Offset(prex, prey),
            //                    end = Offset(curX, curY),
            //                    color = color,
            //                    strokeWidth = 3f,
            //                    pathEffect = pathEffect
            //                )
            val path = Path()
            val pathSize = 20f
            path.moveTo(curX - pathSize, curY)
            path.lineTo(curX, curY + pathSize)
            path.lineTo(curX + pathSize, curY)
            path.lineTo(curX, curY - pathSize)
            path.close()

            val stroke = Stroke(width = 3f)
            drawPath(path, color, 1f, stroke)
        }
        //            drawLine(
        //                start = Offset(prex, prey),
        //                end = Offset(endFrame.centerX(), endFrame.centerY()),
        //                color = color,
        //                strokeWidth = 3f,
        //                pathEffect = pathEffect
        //            )
    }
}

internal actual fun DrawScope.drawFramePlatform(
    frame: WidgetFrame,
    pathEffect: PathEffect,
    color: Color
) {
    if (frame.isDefaultTransform) {
        val drawStyle = Stroke(width = 3f, pathEffect = pathEffect)
        drawRect(
            color,
            Offset(frame.left.toFloat(), frame.top.toFloat()),
            Size(frame.width().toFloat(), frame.height().toFloat()),
            style = drawStyle
        )
    } else {
        val matrix = Matrix()
        if (!frame.rotationZ.isNaN()) {
            matrix.preRotate(frame.rotationZ, frame.centerX(), frame.centerY())
        }
        val scaleX = if (frame.scaleX.isNaN()) 1f else frame.scaleX
        val scaleY = if (frame.scaleY.isNaN()) 1f else frame.scaleY
        matrix.preScale(scaleX, scaleY, frame.centerX(), frame.centerY())
        val points =
            floatArrayOf(
                frame.left.toFloat(),
                frame.top.toFloat(),
                frame.right.toFloat(),
                frame.top.toFloat(),
                frame.right.toFloat(),
                frame.bottom.toFloat(),
                frame.left.toFloat(),
                frame.bottom.toFloat()
            )
        matrix.mapPoints(points)
        drawLine(
            start = Offset(points[0], points[1]),
            end = Offset(points[2], points[3]),
            color = color,
            strokeWidth = 3f,
            pathEffect = pathEffect
        )
        drawLine(
            start = Offset(points[2], points[3]),
            end = Offset(points[4], points[5]),
            color = color,
            strokeWidth = 3f,
            pathEffect = pathEffect
        )
        drawLine(
            start = Offset(points[4], points[5]),
            end = Offset(points[6], points[7]),
            color = color,
            strokeWidth = 3f,
            pathEffect = pathEffect
        )
        drawLine(
            start = Offset(points[6], points[7]),
            end = Offset(points[0], points[1]),
            color = color,
            strokeWidth = 3f,
            pathEffect = pathEffect
        )
    }
}
