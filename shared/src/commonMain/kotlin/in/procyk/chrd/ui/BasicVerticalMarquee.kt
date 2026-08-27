package `in`.procyk.chrd.ui

import androidx.compose.animation.core.Animatable
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
import kotlin.math.roundToInt

object VerticalMarqueeDefaults {
    const val Iterations: Int = 3
    const val RepeatDelayMillis: Int = 1_200
    val Spacing: VerticalMarqueeSpacing = VerticalMarqueeSpacing.fractionOfContainer(1f / 3f)
    val Velocity: Dp = 30.dp
    const val ShowSecondCopy: Boolean = true
}

@Stable
class VerticalMarqueeState(
    initialIsPlaying: Boolean = false,
) {
    var offset: Float by mutableFloatStateOf(0f)
        internal set
    var maxOffset: Float by mutableFloatStateOf(0f)
        internal set
    var isPlaying: Boolean by mutableStateOf(initialIsPlaying)
}

@Composable
fun rememberVerticalMarqueeState(initialIsPlaying: Boolean = false): VerticalMarqueeState {
    return remember { VerticalMarqueeState(initialIsPlaying) }
}

@Stable
fun Modifier.basicVerticalMarquee(
    iterations: Int = VerticalMarqueeDefaults.Iterations,
    animationMode: MarqueeAnimationMode = Immediately,
    repeatDelayMillis: Int = VerticalMarqueeDefaults.RepeatDelayMillis,
    initialDelayMillis: Int = if (animationMode == Immediately) repeatDelayMillis else 0,
    spacing: VerticalMarqueeSpacing = VerticalMarqueeSpacing.fractionOfContainer(1f),
    velocity: Dp = VerticalMarqueeDefaults.Velocity,
    showSecondCopy: Boolean = VerticalMarqueeDefaults.ShowSecondCopy,
    state: VerticalMarqueeState? = null,
): Modifier =
    this then
            VerticalMarqueeModifierElement(
                iterations = iterations,
                animationMode = animationMode,
                delayMillis = repeatDelayMillis,
                initialDelayMillis = initialDelayMillis,
                spacing = spacing,
                velocity = velocity,
                showSecondCopy = showSecondCopy,
                state = state,
            )

private data class VerticalMarqueeModifierElement(
    private val iterations: Int,
    private val animationMode: MarqueeAnimationMode,
    private val delayMillis: Int,
    private val initialDelayMillis: Int,
    private val spacing: VerticalMarqueeSpacing,
    private val velocity: Dp,
    private val showSecondCopy: Boolean,
    private val state: VerticalMarqueeState?,
) : ModifierNodeElement<VerticalMarqueeModifierNode>() {
    override fun create(): VerticalMarqueeModifierNode =
        VerticalMarqueeModifierNode(
            iterations = iterations,
            animationMode = animationMode,
            delayMillis = delayMillis,
            initialDelayMillis = initialDelayMillis,
            spacing = spacing,
            velocity = velocity,
            showSecondCopy = showSecondCopy,
            state = state,
        )

    override fun update(node: VerticalMarqueeModifierNode) {
        node.update(
            iterations = iterations,
            animationMode = animationMode,
            delayMillis = delayMillis,
            initialDelayMillis = initialDelayMillis,
            spacing = spacing,
            velocity = velocity,
            state = state,
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
    iterations: Int,
    animationMode: MarqueeAnimationMode,
    delayMillis: Int,
    initialDelayMillis: Int,
    spacing: VerticalMarqueeSpacing,
    velocity: Dp,
    val showSecondCopy: Boolean,
    state: VerticalMarqueeState?,
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode, FocusEventModifierNode {

    private var contentHeight by mutableIntStateOf(0)
    private var containerHeight by mutableIntStateOf(0)
    private var hasFocus by mutableStateOf(false)
    private var animationJob: Job? = null
    private var marqueeLayer: GraphicsLayer? = null
    var spacing: VerticalMarqueeSpacing by mutableStateOf(spacing)
    var animationMode: MarqueeAnimationMode by mutableStateOf(animationMode)
    var iterations: Int by mutableIntStateOf(iterations)
    var delayMillis: Int by mutableIntStateOf(delayMillis)
    var initialDelayMillis: Int by mutableIntStateOf(initialDelayMillis)
    var velocity: Dp by mutableStateOf(velocity)
    var state: VerticalMarqueeState? by mutableStateOf(state)

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
        state: VerticalMarqueeState?,
    ) {
        this.spacing = spacing
        this.animationMode = animationMode
        this.iterations = iterations
        this.delayMillis = delayMillis
        this.initialDelayMillis = initialDelayMillis
        this.velocity = velocity
        this.state = state
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
                    if (showSecondCopy && secondCopyVisible) {
                        translate(top = secondCopyOffset) { drawLayer(layer) }
                    }
                } else {
                    if (firstCopyVisible) {
                        this@draw.drawContent()
                    }
                    if (showSecondCopy && secondCopyVisible) {
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

        var currentIteration = 0
        var delayWait = true
        var delayMillisRemaining = initialDelayMillis.toFloat()

        withContext(FixedMotionDurationScale) {
            snapshotFlow {
                val ch = contentHeight
                val cntH = containerHeight
                val contentWithSpacingHeight = if (ch <= cntH) null else (ch + spacingPx).toFloat()

                val isPlaying = when {
                    state != null -> state!!.isPlaying
                    animationMode == WhileFocused -> hasFocus
                    else -> true
                }

                val pxPerSec = with(requireDensity()) { velocity.toPx().absoluteValue }

                AnimationConfig(
                    contentWithSpacingHeight = contentWithSpacingHeight,
                    isPlaying = isPlaying,
                    pxPerSec = pxPerSec,
                    iterations = iterations,
                    initialDelayMillis = initialDelayMillis,
                    delayMillis = delayMillis,
                )
            }.collectLatest { config ->
                if (config.contentWithSpacingHeight == null) return@collectLatest
                state?.maxOffset = config.contentWithSpacingHeight

                if (!config.isPlaying || config.iterations <= 0) return@collectLatest

                var lastTimeNanos = withFrameNanos { it }

                while (config.iterations == Int.MAX_VALUE || currentIteration < config.iterations) {
                    val timeNanos = withFrameNanos { it }
                    val deltaMs = (timeNanos - lastTimeNanos) / 1_000_000f
                    lastTimeNanos = timeNanos

                    if (delayWait) {
                        delayMillisRemaining -= deltaMs
                        if (delayMillisRemaining <= 0f) {
                            delayWait = false
                        }
                        continue
                    }

                    val deltaPx = config.pxPerSec * (deltaMs / 1000f)

                    var newOffset = offset.value + deltaPx
                    if (newOffset >= config.contentWithSpacingHeight) {
                        newOffset = 0f
                        currentIteration++
                        if (config.iterations != Int.MAX_VALUE && currentIteration >= config.iterations) {
                            offset.snapTo(newOffset)
                            state?.offset = newOffset
                            break
                        }
                        delayWait = true
                        delayMillisRemaining = config.delayMillis.toFloat()
                    }

                    offset.snapTo(newOffset)
                    state?.offset = newOffset
                }
            }
        }
    }
}

private data class AnimationConfig(
    val contentWithSpacingHeight: Float?,
    val isPlaying: Boolean,
    val pxPerSec: Float,
    val iterations: Int,
    val initialDelayMillis: Int,
    val delayMillis: Int,
)

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
