/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.annexflow.constraintlayout.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import tech.annexflow.constraintlayout.compose.extra.drawFrameDebugPlatform
import tech.annexflow.constraintlayout.compose.extra.drawFramePlatform
import tech.annexflow.constraintlayout.compose.extra.drawPathsPlatform
import tech.annexflow.constraintlayout.compose.platform.annotation.SuppressWarnings
import tech.annexflow.constraintlayout.core.state.Dimension
import tech.annexflow.constraintlayout.core.state.Transition
import tech.annexflow.constraintlayout.core.state.WidgetFrame
import tech.annexflow.constraintlayout.core.widgets.Optimizer

@ExperimentalMotionApi
internal class MotionMeasurer(density: Density) : Measurer(density) {
    private val DEBUG = false
    private var lastProgressInInterpolation = 0f
    val transition = Transition { with(density) { it.dp.toPx() } }

    // TODO: Explicitly declare `getDesignInfo` so that studio tooling can identify the method, also
    //  make sure that the constraints/dimensions returned are for the start/current ConstraintSet

    private fun measureConstraintSet(
        optimizationLevel: Int,
        constraintSet: ConstraintSet,
        measurables: List<Measurable>,
        constraints: Constraints,
    ) {
        state.reset()
        constraintSet.applyTo(state, measurables)
        buildMapping(state, measurables)
        state.apply(root)
        root.children.fastForEach { it.isAnimated = true }
        applyRootSize(constraints)
        root.updateHierarchy()

        if (DEBUG) {
            root.debugName = "ConstraintLayout"
            root.children.fastForEach { child ->
                child.debugName =
                    (child.companionWidget as? Measurable)?.layoutId?.toString() ?: "NOTAG"
            }
        }

        root.optimizationLevel = optimizationLevel
        // No need to set sizes and size modes as we passed them to the state above.
        root.measure(Optimizer.OPTIMIZATION_NONE, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    @Suppress("UnavailableSymbol")
    fun performInterpolationMeasure(
        constraints: Constraints,
        layoutDirection: LayoutDirection,
        constraintSetStart: ConstraintSet,
        constraintSetEnd: ConstraintSet,
        @SuppressWarnings("HiddenTypeParameter") transition: TransitionImpl,
        measurables: List<Measurable>,
        optimizationLevel: Int,
        progress: Float,
        compositionSource: CompositionSource,
        invalidateOnConstraintsCallback: ShouldInvalidateCallback?,
    ): IntSize {
        val needsRemeasure =
            needsRemeasure(
                constraints = constraints,
                source = compositionSource,
                invalidateOnConstraintsCallback = invalidateOnConstraintsCallback,
            )

        if (
            lastProgressInInterpolation != progress ||
            (
                layoutInformationReceiver?.getForcedWidth() != Int.MIN_VALUE &&
                    layoutInformationReceiver?.getForcedHeight() != Int.MIN_VALUE
                ) ||
            needsRemeasure
        ) {
            recalculateInterpolation(
                constraints = constraints,
                layoutDirection = layoutDirection,
                constraintSetStart = constraintSetStart,
                constraintSetEnd = constraintSetEnd,
                transition = transition,
                measurables = measurables,
                optimizationLevel = optimizationLevel,
                progress = progress,
                remeasure = needsRemeasure,
            )
        }
        oldConstraints = constraints
        return IntSize(root.width, root.height)
    }

    /**
     * Nullable reference of [Constraints] used for the `invalidateOnConstraintsCallback`.
     *
     * Helps us to indicate when we can start calling the callback, as we need at least one measure
     * pass to populate this reference.
     */
    private var oldConstraints: Constraints? = null

    /**
     * Indicates if the layout requires measuring before computing the interpolation.
     *
     * This might happen if the size of MotionLayout or any of its children changed.
     *
     * MotionLayout size might change from its parent Layout, and in some cases the children size
     * might change (eg: A Text layout has a longer string appended).
     */
    private fun needsRemeasure(
        constraints: Constraints,
        source: CompositionSource,
        invalidateOnConstraintsCallback: ShouldInvalidateCallback?,
    ): Boolean {
        if (this.transition.isEmpty || frameCache.isEmpty()) {
            // Nothing measured (by MotionMeasurer)
            return true
        }

        if (oldConstraints != null && invalidateOnConstraintsCallback != null) {
            // User is deciding when to invalidate on measuring constraints
            if (invalidateOnConstraintsCallback(oldConstraints!!, constraints)) {
                return true
            }
        } else {
            // Default behavior, only take this path if there's no user logic to invalidate
            if (
                (constraints.hasFixedHeight && !state.sameFixedHeight(constraints.maxHeight)) ||
                (constraints.hasFixedWidth && !state.sameFixedWidth(constraints.maxWidth))
            ) {
                // Layout size changed
                return true
            }
        }

        // Content recomposed. Or marked as such by InvalidationStrategy.onObservedStateChange.
        return source == CompositionSource.Content
    }

    /**
     * Remeasures based on [constraintSetStart] and [constraintSetEnd] if needed.
     *
     * Runs the interpolation for the given [progress].
     *
     * Finally, updates the [Measurable]s dimension if they changed during interpolation.
     */
    private fun recalculateInterpolation(
        constraints: Constraints,
        layoutDirection: LayoutDirection,
        constraintSetStart: ConstraintSet,
        constraintSetEnd: ConstraintSet,
        transition: TransitionImpl?,
        measurables: List<Measurable>,
        optimizationLevel: Int,
        progress: Float,
        remeasure: Boolean,
    ) {
        lastProgressInInterpolation = progress
        if (remeasure) {
            this.transition.clear()
            resetMeasureState()
            // Define the size of the ConstraintLayout.
            state.width(
                if (constraints.hasFixedWidth) {
                    Dimension.createFixed(constraints.maxWidth)
                } else {
                    Dimension.createWrap().min(constraints.minWidth)
                },
            )
            state.height(
                if (constraints.hasFixedHeight) {
                    Dimension.createFixed(constraints.maxHeight)
                } else {
                    Dimension.createWrap().min(constraints.minHeight)
                },
            )
            // Build constraint set and apply it to the state.
            state.rootIncomingConstraints = constraints
            state.isRtl = layoutDirection == LayoutDirection.Rtl

            measureConstraintSet(optimizationLevel, constraintSetStart, measurables, constraints)
            this.transition.updateFrom(root, Transition.START)
            measureConstraintSet(optimizationLevel, constraintSetEnd, measurables, constraints)
            this.transition.updateFrom(root, Transition.END)
            transition?.applyKeyFramesTo(this.transition)
        } else {
            // Have to remap even if there's no reason to remeasure
            buildMapping(state, measurables)
        }
        this.transition.interpolate(root.width, root.height, progress)
        root.width = this.transition.interpolatedWidth
        root.height = this.transition.interpolatedHeight
        // Update measurables to interpolated dimensions
        root.children.fastForEach { child ->
            // Update measurables to the interpolated dimensions
            val measurable = (child.companionWidget as? Measurable) ?: return@fastForEach
            val interpolatedFrame = this.transition.getInterpolated(child) ?: return@fastForEach
            placeables[measurable] =
                measurable.measure(
                    Constraints.fixed(interpolatedFrame.width(), interpolatedFrame.height()),
                )
            frameCache[measurable] = interpolatedFrame
        }

        if (layoutInformationReceiver?.getLayoutInformationMode() == LayoutInfoFlags.BOUNDS) {
            computeLayoutResult()
        }
    }

    private fun encodeKeyFrames(
        json: StringBuilder,
        location: FloatArray,
        types: IntArray,
        progress: IntArray,
        count: Int,
    ) {
        if (count == 0) {
            return
        }
        json.append("keyTypes : [")
        for (i in 0 until count) {
            val m = types[i]
            json.append(" $m,")
        }
        json.append("],\n")

        json.append("keyPos : [")
        for (i in 0 until count * 2) {
            val f = location[i]
            json.append(" $f,")
        }
        json.append("],\n ")

        json.append("keyFrames : [")
        for (i in 0 until count) {
            val f = progress[i]
            json.append(" $f,")
        }
        json.append("],\n ")
    }

    fun encodeRoot(json: StringBuilder) {
        json.append("  root: {")
        json.append("interpolated: { left:  0,")
        json.append("  top:  0,")
        json.append("  right:   ${root.width} ,")
        json.append("  bottom:  ${root.height} ,")
        json.append(" } }")
    }

    override fun computeLayoutResult() {
        val json = StringBuilder()
        json.append("{ ")
        encodeRoot(json)
        val mode = IntArray(50)
        val pos = IntArray(50)
        val key = FloatArray(100)

        root.children.fastForEach { child ->
            val start = transition.getStart(child.stringId)
            val end = transition.getEnd(child.stringId)
            val interpolated = transition.getInterpolated(child.stringId)
            val path = transition.getPath(child.stringId)
            val count = transition.getKeyFrames(child.stringId, key, mode, pos)

            json.append(" ${child.stringId}: {")
            json.append(" interpolated : ")
            interpolated.serialize(json, true)

            json.append(", start : ")
            start.serialize(json)

            json.append(", end : ")
            end.serialize(json)
            encodeKeyFrames(json, key, mode, pos, count)
            json.append(" path : [")
            for (point in path) {
                json.append(" $point ,")
            }
            json.append(" ] ")
            json.append("}, ")
        }
        json.append(" }")
        layoutInformationReceiver?.setLayoutInformation(json.toString())
    }

    /**
     * Draws debug information related to the current Transition.
     *
     * Typically, this means drawing the bounds of each widget at the start/end positions, the path
     * they take and indicators for KeyPositions.
     */
    fun DrawScope.drawDebug(
        drawBounds: Boolean = true,
        drawPaths: Boolean = true,
        drawKeyPositions: Boolean = true,
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        root.children.fastForEach { child ->
            val startFrame = transition.getStart(child)
            val endFrame = transition.getEnd(child)
            if (drawBounds) {
                // Draw widget bounds at the start and end
                drawFrame(frame = startFrame, pathEffect = pathEffect, color = Color.Blue)
                drawFrame(frame = endFrame, pathEffect = pathEffect, color = Color.Blue)
                translate(2f, 2f) {
                    // Do an additional offset draw in case the bounds are not visible/obstructed
                    drawFrame(frame = startFrame, pathEffect = pathEffect, color = Color.White)
                    drawFrame(frame = endFrame, pathEffect = pathEffect, color = Color.White)
                }
            }
            drawPaths(
                parentWidth = size.width,
                parentHeight = size.height,
                startFrame = startFrame,
                drawPath = drawPaths,
                drawKeyPositions = drawKeyPositions,
            )
        }
    }

    private fun DrawScope.drawPaths(
        parentWidth: Float,
        parentHeight: Float,
        startFrame: WidgetFrame,
        drawPath: Boolean,
        drawKeyPositions: Boolean,
    ) {
        drawPathsPlatform(
            parentWidth = parentWidth,
            parentHeight = parentHeight,
            startFrame = startFrame,
            drawPath = drawPath,
            drawKeyPositions = drawKeyPositions,
            transition = transition,
        )
    }

    private fun DrawScope.drawFrameDebug(
        parentWidth: Float,
        parentHeight: Float,
        startFrame: WidgetFrame,
        endFrame: WidgetFrame,
        pathEffect: PathEffect,
        color: Color,
    ) {
        drawFrameDebugPlatform(
            parentWidth = parentWidth,
            parentHeight = parentHeight,
            startFrame = startFrame,
            endFrame = endFrame,
            pathEffect = pathEffect,
            color = color,
            transition = transition,
        )
    }

    private fun DrawScope.drawFrame(frame: WidgetFrame, pathEffect: PathEffect, color: Color) {
        drawFramePlatform(frame = frame, pathEffect = pathEffect, color = color)
    }

    /**
     * Calculates and returns a [Color] value of the custom property given by [name] on the
     * ConstraintWidget corresponding to [id], the value is calculated at the given [progress] value
     * on the current Transition.
     *
     * Returns [Color.Unspecified] if the custom property doesn't exist.
     */
    fun getCustomColor(id: String, name: String, progress: Float): Color {
        if (!transition.contains(id)) {
            return Color.Unspecified
        }
        transition.interpolate(root.width, root.height, progress)

        val interpolatedFrame = transition.getInterpolated(id)

        if (!interpolatedFrame.containsCustom(name)) {
            return Color.Unspecified
        }
        return Color(interpolatedFrame.getCustomColor(name))
    }

    /**
     * Calculates and returns a [Float] value of the custom property given by [name] on the
     * ConstraintWidget corresponding to [id], the value is calculated at the given [progress] value
     * on the current Transition.
     *
     * Returns [Float.NaN] if the custom property doesn't exist.
     */
    fun getCustomFloat(id: String, name: String, progress: Float): Float {
        if (!transition.contains(id)) {
            return Float.NaN
        }
        transition.interpolate(root.width, root.height, progress)

        val interpolatedFrame = transition.getInterpolated(id)
        return interpolatedFrame.getCustomFloat(name)
    }

    fun clearConstraintSets() {
        transition.clear()
        frameCache.clear()
    }

    @Suppress("UnavailableSymbol")
    fun initWith(
        start: ConstraintSet,
        end: ConstraintSet,
        layoutDirection: LayoutDirection,
        @SuppressWarnings("HiddenTypeParameter") transition: TransitionImpl,
        progress: Float,
    ) {
        clearConstraintSets()

        state.isRtl = layoutDirection == LayoutDirection.Rtl
        start.applyTo(state, emptyList())
        start.applyTo(this.transition, Transition.START)
        state.apply(root)
        this.transition.updateFrom(root, Transition.START)

        start.applyTo(state, emptyList())
        end.applyTo(this.transition, Transition.END)
        state.apply(root)
        this.transition.updateFrom(root, Transition.END)

        this.transition.interpolate(0, 0, progress)
        transition.applyAllTo(this.transition)
    }
}

/**
 * Functional interface to represent the callback of type `(old: Constraints, new: Constraints) ->
 * Boolean`
 */
internal fun interface ShouldInvalidateCallback {
    operator fun invoke(old: Constraints, new: Constraints): Boolean
}
