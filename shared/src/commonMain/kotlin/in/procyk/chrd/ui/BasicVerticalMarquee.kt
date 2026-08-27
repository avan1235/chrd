package `in`.procyk.chrd.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeAnimationMode.Companion.Immediately
import androidx.compose.foundation.MarqueeAnimationMode.Companion.WhileFocused
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.*
import androidx.compose.ui.node.*
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToInt

object VerticalMarqueeDefaults {
    val Iterations: Int = 3
    val RepeatDelayMillis: Int = 1_200
    val Spacing: VerticalMarqueeSpacing = VerticalMarqueeSpacing.fractionOfContainer(1f / 3f)
    val Velocity: Dp = 30.dp
}

/**
 * Applies an animated marquee effect to the modified content if it's too tall to fit in the
 * available space. This modifier has no effect if the content fits in the max constraints. The
 * content will be measured with unbounded height.
 *
 * @param iterations The number of times to repeat the animation. `Int.MAX_VALUE` will repeat
 *   forever, and 0 will disable animation.
 * @param animationMode Whether the marquee should start animating [Immediately] or only
 *   [WhileFocused].
 * @param repeatDelayMillis The duration to wait before starting each subsequent iteration, in millis.
 * @param initialDelayMillis The duration to wait before starting the first iteration.
 * @param spacing A [VerticalMarqueeSpacing] that specifies how much space to leave at the end of the
 *   content before showing the beginning again.
 * @param velocity The speed of the animation in dps / second.
 */
@Stable
fun Modifier.basicVerticalMarquee(
    iterations: Int = VerticalMarqueeDefaults.Iterations,
    animationMode: MarqueeAnimationMode = Immediately,
    repeatDelayMillis: Int = VerticalMarqueeDefaults.RepeatDelayMillis,
    initialDelayMillis: Int = if (animationMode == Immediately) repeatDelayMillis else 0,
    spacing: VerticalMarqueeSpacing = VerticalMarqueeDefaults.Spacing,
    velocity: Dp = VerticalMarqueeDefaults.Velocity,
): Modifier =
    this then
            VerticalMarqueeModifierElement(
                iterations = iterations,
                animationMode = animationMode,
                delayMillis = repeatDelayMillis,
                initialDelayMillis = initialDelayMillis,
                spacing = spacing,
                velocity = velocity,
            )

private data class VerticalMarqueeModifierElement(
    private val iterations: Int,
    private val animationMode: MarqueeAnimationMode,
    private val delayMillis: Int,
    private val initialDelayMillis: Int,
    private val spacing: VerticalMarqueeSpacing,
    private val velocity: Dp,
) : ModifierNodeElement<VerticalMarqueeModifierNode>() {
    override fun create(): VerticalMarqueeModifierNode =
        VerticalMarqueeModifierNode(
            iterations = iterations,
            animationMode = animationMode,
            delayMillis = delayMillis,
            initialDelayMillis = initialDelayMillis,
            spacing = spacing,
            velocity = velocity,
        )

    override fun update(node: VerticalMarqueeModifierNode) {
        node.update(
            iterations = iterations,
            animationMode = animationMode,
            delayMillis = delayMillis,
            initialDelayMillis = initialDelayMillis,
            spacing = spacing,
            velocity = velocity,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "basicVerticalMarquee"
        properties["iterations"] = iterations
        properties["animationMode"] = animationMode
        properties["delayMillis"] = delayMillis
        properties["initialDelayMillis"] = initialDelayMillis
        properties["spacing"] = spacing
        properties["velocity"] = velocity
    }
}

private class VerticalMarqueeModifierNode(
    private var iterations: Int,
    animationMode: MarqueeAnimationMode,
    private var delayMillis: Int,
    private var initialDelayMillis: Int,
    spacing: VerticalMarqueeSpacing,
    private var velocity: Dp,
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode, FocusEventModifierNode {

    private var contentHeight by mutableIntStateOf(0)
    private var containerHeight by mutableIntStateOf(0)
    private var hasFocus by mutableStateOf(false)
    private var animationJob: Job? = null
    private var marqueeLayer: GraphicsLayer? = null
    var spacing: VerticalMarqueeSpacing by mutableStateOf(spacing)
    var animationMode: MarqueeAnimationMode by mutableStateOf(animationMode)

    /**
     * The animation of the marquee content - this is always in the range
     * [0, contentHeight + spacingHeight].
     */
    private val offset = Animatable(0f)

    private val spacingPx by derivedStateOf {
        with(spacing) { requireDensity().calculateSpacing(contentHeight, containerHeight) }
    }

    override fun onAttach() {
        val layer = marqueeLayer
        val graphicsContext = requireGraphicsContext()
        if (layer != null) {
            graphicsContext.releaseGraphicsLayer(layer)
        }

        marqueeLayer = graphicsContext.createGraphicsLayer()
        restartAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null

        val layer = marqueeLayer
        if (layer != null) {
            requireGraphicsContext().releaseGraphicsLayer(layer)
            marqueeLayer = null
        }
    }

    fun update(
        iterations: Int,
        animationMode: MarqueeAnimationMode,
        delayMillis: Int,
        initialDelayMillis: Int,
        spacing: VerticalMarqueeSpacing,
        velocity: Dp,
    ) {
        this.spacing = spacing
        this.animationMode = animationMode

        if (
            this.iterations != iterations ||
            this.delayMillis != delayMillis ||
            this.initialDelayMillis != initialDelayMillis ||
            this.velocity != velocity
        ) {
            this.iterations = iterations
            this.delayMillis = delayMillis
            this.initialDelayMillis = initialDelayMillis
            this.velocity = velocity
            restartAnimation()
        }
    }

    override fun onFocusEvent(focusState: FocusState) {
        hasFocus = focusState.hasFocus
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val childConstraints = constraints.copy(maxHeight = Constraints.Infinity)
        val placeable = measurable.measure(childConstraints)
        containerHeight = constraints.constrainHeight(placeable.height)
        contentHeight = placeable.height
        val containerWidth = constraints.constrainWidth(placeable.width)

        return layout(containerWidth, containerHeight) {
            placeable.placeWithLayer(0, 0)
        }
    }

    /** Ignores height since marquee contents are measured with infinite height. */
    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int = measurable.minIntrinsicWidth(Constraints.Infinity)

    /** Ignores height since marquee contents are measured with infinite height. */
    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int = measurable.maxIntrinsicWidth(Constraints.Infinity)

    /** Always returns zero since the vertical marquee has no minimum height. */
    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int = 0

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int = measurable.maxIntrinsicHeight(width)

    override fun ContentDrawScope.draw() {
        val clipWindowOffset =
            if (velocity > 0.dp) {
                offset.value
            } else {
                -offset.value + contentHeight + spacingPx
            }

        val firstCopyVisible = clipWindowOffset < contentHeight
        val secondCopyVisible = clipWindowOffset + containerHeight > contentHeight + spacingPx
        val secondCopyOffset = (contentHeight + spacingPx).toFloat()

        val drawWidth = size.width
        marqueeLayer?.let { layer ->
            layer.record(size = IntSize(drawWidth.roundToInt(), contentHeight)) {
                this@draw.drawContent()
            }
        }
        clipRect(bottom = containerHeight.toFloat()) {
            translate(top = -clipWindowOffset) {
                val layer = marqueeLayer
                if (layer != null) {
                    if (firstCopyVisible) {
                        drawLayer(layer)
                    }
                    if (secondCopyVisible) {
                        translate(top = secondCopyOffset) { drawLayer(layer) }
                    }
                } else {
                    if (firstCopyVisible) {
                        this@draw.drawContent()
                    }
                    if (secondCopyVisible) {
                        translate(top = secondCopyOffset) { this@draw.drawContent() }
                    }
                }
            }
        }
    }

    private fun restartAnimation() {
        val oldJob = animationJob
        oldJob?.cancel()
        if (isAttached) {
            animationJob =
                coroutineScope.launch {
                    oldJob?.join()
                    runAnimation()
                }
        }
    }

    private suspend fun runAnimation() {
        if (iterations <= 0) return

        withContext(FixedMotionDurationScale) {
            snapshotFlow {
                if (contentHeight <= containerHeight) return@snapshotFlow null
                if (animationMode == WhileFocused && !hasFocus) return@snapshotFlow null
                (contentHeight + spacingPx).toFloat()
            }
                .collectLatest { contentWithSpacingHeight ->
                    if (contentWithSpacingHeight == null) return@collectLatest

                    val spec =
                        createMarqueeAnimationSpec(
                            iterations,
                            contentWithSpacingHeight,
                            initialDelayMillis,
                            delayMillis,
                            velocity,
                            requireDensity(),
                        )

                    offset.snapTo(0f)
                    try {
                        offset.animateTo(contentWithSpacingHeight, spec)
                    } finally {
                        offset.snapTo(0f)
                    }
                }
        }
    }
}

private fun createMarqueeAnimationSpec(
    iterations: Int,
    targetValue: Float,
    initialDelayMillis: Int,
    delayMillis: Int,
    velocity: Dp,
    density: Density,
): AnimationSpec<Float> {
    val pxPerSec = with(density) { velocity.toPx() }
    val singleSpec =
        velocityBasedTween(
            velocity = pxPerSec.absoluteValue,
            targetValue = targetValue,
            delayMillis = delayMillis,
        )
    val startOffset = StartOffset(-delayMillis + initialDelayMillis)
    return if (iterations == Int.MAX_VALUE) {
        infiniteRepeatable(singleSpec, initialStartOffset = startOffset)
    } else {
        repeatable(iterations, singleSpec, initialStartOffset = startOffset)
    }
}

private fun velocityBasedTween(
    velocity: Float,
    targetValue: Float,
    delayMillis: Int,
): TweenSpec<Float> {
    val pxPerMilli = velocity / 1000f
    return tween(
        durationMillis = ceil(targetValue / pxPerMilli).toInt(),
        easing = LinearEasing,
        delayMillis = delayMillis,
    )
}

/** A [VerticalMarqueeSpacing] with a fixed size. */
fun VerticalMarqueeSpacing(spacing: Dp): VerticalMarqueeSpacing =
    VerticalMarqueeSpacing { _, _ -> spacing.roundToPx() }

/**
 * Defines a [calculateSpacing] method that determines the space after the end of [basicVerticalMarquee]
 * content before drawing the content again.
 */
@Stable
fun interface VerticalMarqueeSpacing {
    /**
     * Calculates the space after the end of [basicVerticalMarquee] content before drawing the content
     * again.
     *
     * @param contentHeight The height of the content inside the marquee, in pixels.
     * @param containerHeight The height of the marquee itself, in pixels.
     * @return The space in pixels between the end of the content and the beginning of the content.
     */
    fun Density.calculateSpacing(contentHeight: Int, containerHeight: Int): Int

    companion object {
        /** A [VerticalMarqueeSpacing] that is a fraction of the container's height. */
        fun fractionOfContainer(fraction: Float): VerticalMarqueeSpacing =
            VerticalMarqueeSpacing { _, height ->
                (fraction * height).roundToInt()
            }
    }
}

private object FixedMotionDurationScale : MotionDurationScale {
    override val scaleFactor: Float
        get() = 1f
}
