package `in`.procyk.chrd.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.chrd.component.Screen
import `in`.procyk.chrd.component.liquid.LiquidBottomTabsSpacer
import `in`.procyk.chrd.model.*
import `in`.procyk.chrd.model.LinePart.*
import `in`.procyk.chrd.ui.KeepScreenOn
import `in`.procyk.chrd.viewmodel.SongViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


@Composable
internal fun SongScreen(
    viewModel: SongViewModel,
    isAutoScrolling: Boolean,
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
                isAutoScrolling = isAutoScrolling,
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
    isAutoScrolling: Boolean,
    onAutoScrollingChanged: (Boolean) -> Unit,
) {
    KeepScreenOn()

    val songLines = remember(song) { song.sections.sumOf { it.lines.size } }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var speedMultiplier by remember { mutableFloatStateOf(1f) }
    var clickedChord by remember { mutableStateOf<Chord?>(null) }

    val isAtBottom by remember {
        derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue
        }
    }

    LaunchedEffect(isAutoScrolling, speedMultiplier, scrollState.maxValue) {
        if (!isAutoScrolling || scrollState.maxValue <= 0 || songLines == 0) return@LaunchedEffect

        val remainingPixels = scrollState.maxValue - scrollState.value

        if (remainingPixels > 0) {
            val totalTimeMs = (songLines * 5000f) / speedMultiplier

            val fractionRemaining = remainingPixels.toFloat() / scrollState.maxValue.toFloat()
            val durationMs = (totalTimeMs * fractionRemaining).toInt()

            try {
                scrollState.animateScrollTo(
                    value = scrollState.maxValue,
                    animationSpec = tween(
                        durationMillis = durationMs,
                        easing = LinearEasing,
                    ),
                )
                onAutoScrollingChanged(false)
            } catch (e: CancellationException) {
                if (!scrollState.isScrollInProgress) throw e

                onAutoScrollingChanged(false)
            }
        } else {
            onAutoScrollingChanged(false)
        }
    }

    Screen(
        topBar = {
            TopAppBar(
                title = {
                    Column {
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
        topBarVisible = !isAutoScrolling,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedVisibility(
                    visible = isAutoScrolling && scrollState.maxValue > 0,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        SmallFloatingActionButton(
                            onClick = { speedMultiplier *= 1.5f },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Scroll Faster",
                            )
                        }

                        SmallFloatingActionButton(
                            // Prevent speed from dropping to 0
                            onClick = { speedMultiplier = maxOf(0.1f, speedMultiplier / 1.5f) },
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
                    visible = scrollState.maxValue > 0,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    when {
                        isAtBottom -> {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { scrollState.animateScrollTo(0) }
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Restart Auto-scroll",
                                )
                            }
                        }

                        else -> FloatingActionButton(
                            onClick = { onAutoScrollingChanged(!isAutoScrolling) },
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isAutoScrolling) "Pause Auto-scroll" else "Start Auto-scroll",
                            )
                        }
                    }
                }
                val useLiquidNavigation by viewModel.useLiquidNavigation.collectAsState()
                LiquidBottomTabsSpacer(useLiquidNavigation && !isAutoScrolling)
            }

        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
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
                    text = clickedChord!!.value,
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