package `in`.procyk.chrd.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.chrd.ChrdFonts
import `in`.procyk.chrd.component.Screen
import `in`.procyk.chrd.component.liquid.LiquidBottomTabsSpacer
import `in`.procyk.chrd.model.*
import `in`.procyk.chrd.model.LinePart.*
import `in`.procyk.chrd.viewmodel.SongViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Stable
internal class LoggingScrollState(
    val scrollState: ScrollState,
    private val tag: String = "SongScreen",
) {
    fun formatValues(): String = Snapshot.withoutReadObservation {
        "ScrollState(value=${scrollState.value}, maxValue=${scrollState.maxValue}, isScrollInProgress=${scrollState.isScrollInProgress}, canScrollForward=${scrollState.canScrollForward}, canScrollBackward=${scrollState.canScrollBackward})"
    }

    val value: Int
        get() {
            val v = scrollState.value
            println("[$tag] Access state.value -> $v (${formatValues()})")
            return v
        }

    val maxValue: Int
        get() {
            val mv = scrollState.maxValue
            println("[$tag] Access state.maxValue -> $mv (${formatValues()})")
            return mv
        }

    val isScrollInProgress: Boolean
        get() {
            val inProgress = scrollState.isScrollInProgress
            println("[$tag] Access state.isScrollInProgress -> $inProgress (${formatValues()})")
            return inProgress
        }

    suspend fun animateScrollTo(
        value: Int,
        animationSpec: AnimationSpec<Float> = spring(),
    ) {
        println("[$tag] Call state.animateScrollTo(value=$value) starting: ${formatValues()}")
        try {
            scrollState.animateScrollTo(value, animationSpec)
            println("[$tag] Completed state.animateScrollTo(value=$value): ${formatValues()}")
        } catch (e: Throwable) {
            println("[$tag] Failed/Cancelled state.animateScrollTo(value=$value): ${e::class.simpleName}: ${e.message}, ${formatValues()}")
            throw e
        }
    }

    override fun toString(): String = formatValues()

    override fun equals(other: Any?): Boolean =
        if (other is LoggingScrollState) scrollState == other.scrollState else scrollState == other

    override fun hashCode(): Int = scrollState.hashCode()
}

private fun Modifier.verticalScroll(
    state: LoggingScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
): Modifier {
    println("[SongScreen] Modifier.verticalScroll applied with state: ${state.formatValues()}")
    return this.verticalScroll(state.scrollState, enabled, flingBehavior, reverseScrolling)
}

@Composable
internal fun SongScreen(
    viewModel: SongViewModel,
    isFullScreen: Boolean,
    onAutoScrollingChanged: (Boolean) -> Unit,
) {
    val song by viewModel.song.collectAsState()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (val song = song) {
            null -> CircularProgressIndicator()
            else -> AutoScrollableSongView(
                song = song,
                viewModel = viewModel,
                isAutoScroll = isFullScreen,
                onAutoScrollingChanged = onAutoScrollingChanged,
            )
        }
    }
}

@Composable
private fun AutoScrollableSongView(
    song: Song,
    viewModel: SongViewModel,
    modifier: Modifier = Modifier,
    isAutoScroll: Boolean,
    onAutoScrollingChanged: (Boolean) -> Unit,
) {
    BoxWithConstraints {
        val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val rawState = rememberScrollState()
        val state = remember(rawState) { LoggingScrollState(rawState) }
        val scope = rememberCoroutineScope()

        fun resetScrollState(caller: String = "unspecified") {
            println("[SongScreen] resetScrollState called (caller: $caller), current state: $state")
            onAutoScrollingChanged(false)
            scope.launch {
                println("[SongScreen] resetScrollState: launching animateScrollTo(0)")
                state.animateScrollTo(0)
            }
        }

        var speedMultiplier by remember { mutableFloatStateOf(1f) }

        LaunchedEffect(state) {
            snapshotFlow { state.maxValue }.collect { maxVal ->
                println("[SongScreen] state.maxValue changed to $maxVal (state: $state)")
            }
        }

        LaunchedEffect(isAutoScroll, speedMultiplier, state, maxHeightPx) {
            println("[SongScreen] LaunchedEffect auto-scroll: isAutoScroll=$isAutoScroll, speedMultiplier=$speedMultiplier, maxHeightPx=$maxHeightPx, state: $state")
            if (!isAutoScroll) {
                println("[SongScreen] LaunchedEffect auto-scroll: isAutoScroll is false, returning")
                return@LaunchedEffect
            }

            val leftPixels = state.run { maxValue - value }
            val durationMillis = (12_000 * leftPixels / (maxHeightPx * speedMultiplier)).roundToInt()
            println("[SongScreen] LaunchedEffect auto-scroll starting: leftPixels=$leftPixels, durationMillis=$durationMillis, target=${state.maxValue}")
            try {
                state.animateScrollTo(
                    value = state.maxValue,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = LinearEasing,
                    )
                )
                println("[SongScreen] LaunchedEffect auto-scroll: animateScrollTo completed, state: $state")
            } catch (e: Throwable) {
                println("[SongScreen] LaunchedEffect auto-scroll: animateScrollTo cancelled/failed: ${e::class.simpleName}: ${e.message}, state: $state")
            }
        }

        if (isAutoScroll) LaunchedEffect(state) {
            println("[SongScreen] LaunchedEffect(state) started for isAutoScroll=true, state: $state")
            snapshotFlow { state.isScrollInProgress }.collect { inProgress ->
                println("[SongScreen] snapshotFlow isScrollInProgress emitted: $inProgress, state: $state")
                if (!inProgress) {
                    println("[SongScreen] isScrollInProgress became false while isAutoScroll=true -> calling resetScrollState")
                    resetScrollState(caller = "snapshotFlow(!isScrollInProgress)")
                }
            }
        }

        var clickedChord by remember { mutableStateOf<Chord?>(null) }

        Screen(
            modifier = modifier.keepScreenOn(),
            topBar = {
                TopAppBar(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    title = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "by ${song.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::halfToneDown) {
                            Icon(
                                imageVector = Icons.Default.ArrowCircleDown,
                                contentDescription = "Half tone down",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = viewModel::halfToneUp) {
                            Icon(
                                imageVector = Icons.Default.ArrowCircleUp,
                                contentDescription = "Half tone up",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        val isFavorite by viewModel.isFavorite.collectAsState()
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                            )
                        }
                    },
                )
            },
            topBarVisible = !isAutoScroll,
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AnimatedVisibility(
                        visible = isAutoScroll && state.maxValue > 0f,
                        enter =
                            if (state.value <= 0) fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                            else fadeIn(tween(DefaultDurationMillis, DefaultDurationMillis)) + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(DefaultDurationMillis, DefaultDurationMillis),
                            ),
                        exit =
                            if (state.value <= 0) fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                            else fadeOut(tween(DefaultDurationMillis)) + slideOutVertically(
                                targetOffsetY = { it / 2 },
                                animationSpec = tween(DefaultDurationMillis),
                            ),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    speedMultiplier *= 1.5f
                                    println("[SongScreen] Speed multiplier increased to $speedMultiplier, current state: $state")
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Scroll Faster",
                                )
                            }

                            SmallFloatingActionButton(
                                // Prevent speed from dropping to 0
                                onClick = {
                                    speedMultiplier = maxOf(0.1f, speedMultiplier / 1.5f)
                                    println("[SongScreen] Speed multiplier decreased to $speedMultiplier, current state: $state")
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Scroll Slower",
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !isAutoScroll && state.value > 0 && state.maxValue > 0,
                        enter = fadeIn(tween(DefaultDurationMillis, DefaultDurationMillis)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(DefaultDurationMillis, DefaultDurationMillis),
                        ),
                        exit = fadeOut(tween(DefaultDurationMillis)) + slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(DefaultDurationMillis),
                        ),
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                println("[SongScreen] Reset scroll FAB clicked")
                                resetScrollState(caller = "Reset scroll FAB")
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Reset Scroll",
                            )
                        }
                    }


                    AnimatedVisibility(
                        visible = state.maxValue > 0f,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        FloatingActionButton(
                            onClick = {
                                println("[SongScreen] Auto-scroll Play/Pause FAB clicked: toggling isAutoScroll from $isAutoScroll to ${!isAutoScroll}, current state: $state")
                                onAutoScrollingChanged(!isAutoScroll)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                imageVector = if (isAutoScroll) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isAutoScroll) "Pause Auto-scroll" else "Start Auto-scroll",
                            )
                        }
                    }
                    val useLiquidNavigation by viewModel.useLiquidNavigation.collectAsState()
                    LiquidBottomTabsSpacer(useLiquidNavigation && !isAutoScroll)
                }

            },
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .verticalScroll(state)
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                SongChordsView(song)

                song.sections.forEach { section ->
                    SongSectionView(section, onChordClick = { clickedChord = it })
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (clickedChord != null) {
            AlertDialog(
                onDismissRequest = { clickedChord = null },
                confirmButton = {
                    TextButton(onClick = { clickedChord = null }) {
                        Text("Close")
                    }
                },
                title = {
                    Text(
                        text = "How to play ${clickedChord!!.value} chord?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ChordDiagram(clickedChord!!)
                    }
                },
            )
        }
    }
}

@Composable
private fun SongSectionView(section: SongSection, onChordClick: (Chord) -> Unit) {
    Column {
        val sectionName = when (section.type) {
            SectionType.VERSE -> "Verse"
            SectionType.CHORUS -> "Chorus"
            SectionType.BRIDGE -> "Bridge"
            SectionType.OTHER -> ""
        }

        if (sectionName.isNotEmpty()) {
            Text(
                text = sectionName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        val isChorus = section.type == SectionType.CHORUS
        Column(
            modifier = Modifier.padding(start = if (isChorus) 16.dp else 0.dp),
        ) {
            section.lines.forEach { line ->
                SongLineView(line, isChorus, onChordClick = onChordClick)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SongLineView(line: SongLine, isChorus: Boolean, onChordClick: (Chord) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start,
    ) {
        line.parts.forEach { part ->
            LinePartView(part = part, isChorus = isChorus, onChordClick = onChordClick)
        }
    }
}

@Composable
private fun LinePartView(part: LinePart, isChorus: Boolean, onChordClick: (Chord) -> Unit) {
    val chordColor = MaterialTheme.colorScheme.primary
    val lyricColor =
        if (isChorus) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val lyricFontWeight = if (isChorus) FontWeight.Normal else FontWeight.Thin

    val spacerText = " "

    Column(
        modifier = Modifier.padding(end = 2.dp), // Slight spacing between words
    ) {
        val chordTop = when (part) {
            is ChordOverWhitespace -> part.chord
            is ChordedLyric -> part.chord
            else -> null
        }
        val chordTextTop = chordTop?.value ?: spacerText


        Text(
            text = chordTextTop,
            color = chordColor,
            fontFamily = ChrdFonts.mono,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = if (chordTop != null) {
                val interactionSource = remember { MutableInteractionSource() }
                Modifier.clickable(interactionSource, indication = null) { onChordClick(chordTop) }
            } else Modifier,
        )

        val chordBottom = when (part) {
            is ChordInText -> part.chord
            else -> null
        }
        val textBottom = when (part) {
            is Lyric -> part.text
            is ChordedLyric -> part.text
            is ChordOverWhitespace -> spacerText
            is ChordInText -> part.chord.value
        }

        Text(
            text = textBottom,
            color = if (chordBottom != null) chordColor else lyricColor,
            fontFamily = ChrdFonts.mono,
            fontWeight = if (chordBottom != null) FontWeight.Bold else lyricFontWeight,
            fontSize = 16.sp,
            modifier = if (chordBottom != null) {
                val interactionSource = remember { MutableInteractionSource() }
                Modifier.clickable(interactionSource, indication = null) { onChordClick(chordBottom) }
            } else Modifier,
        )
    }
}

@Composable
private fun SongChordsView(song: Song) {
    val chords = remember(song) {
        song.sections.asSequence()
            .flatMap { it.lines }
            .flatMap { it.parts }
            .mapNotNull {
                when (it) {
                    is ChordedLyric -> it.chord
                    is ChordOverWhitespace -> it.chord
                    is ChordInText -> it.chord
                    is Lyric -> null
                }
            }
            .distinct()
            .sortedWith(compareBy({ it.root }, { it.quality })).toList()
    }

    if (chords.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            chords.forEach { chord ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = chord.value,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = ChrdFonts.mono,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ChordDiagram(chord)
                }
            }
        }
    }
}

@Composable
private fun ChordDiagram(
    chord: Chord,
    stringInterval: Dp = 16.dp,
    fretInterval: Dp = 20.dp,
    topPadding: Dp = 16.dp,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val representation = chord.representation

    val maxFret = representation.mapNotNull { it.digitToIntOrNull() }.maxOrNull() ?: 0
    val displayFrets = maxOf(5, maxFret)

    val width = stringInterval * 5
    val height = fretInterval * displayFrets + topPadding

    Canvas(modifier = Modifier.size(width, height)) {
        val sSpace = size.width / 5
        val fSpace = (size.height - topPadding.toPx()) / displayFrets
        val nutY = topPadding.toPx()

        for (i in 0 until 6) {
            val x = i * sSpace
            drawLine(
                color = onSurface,
                start = Offset(x, nutY),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }

        for (i in 0..displayFrets) {
            val y = nutY + i * fSpace
            drawLine(
                color = onSurface,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = if (i == 0) 3.dp.toPx() else 1.dp.toPx(),
            )
        }

        representation.forEachIndexed { sIndex, char ->
            val x = sIndex * sSpace
            when (char) {
                'x' -> {
                    val crossSize = 2.dp.toPx()
                    val centerY = nutY / 2
                    drawLine(
                        color = onSurface,
                        start = Offset(x - crossSize, centerY - crossSize),
                        end = Offset(x + crossSize, centerY + crossSize),
                        strokeWidth = 1.dp.toPx(),
                    )
                    drawLine(
                        color = onSurface,
                        start = Offset(x + crossSize, centerY - crossSize),
                        end = Offset(x - crossSize, centerY + crossSize),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                '0' -> {}

                else -> {
                    val fret = char.digitToIntOrNull()
                    if (fret != null && fret > 0) {
                        val y = nutY + (fret - 0.5f) * fSpace
                        drawCircle(
                            color = onSurface,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y),
                        )
                    }
                }
            }
        }
    }
}