package `in`.procyk.chrd.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.chrd.model.*
import `in`.procyk.chrd.model.LinePart.*
import `in`.procyk.chrd.viewmodel.SongViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


@Composable
internal fun SongScreen(
    viewModel: SongViewModel,
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
            else -> AutoScrollableSongView(song)
        }
    }
}

private val SCROLL_REFRESH_RATE = 1.seconds / 120

@Composable
private fun AutoScrollableSongView(
    song: Song,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var isAutoScrolling by remember { mutableStateOf(false) }
    var scrollBy by remember { mutableFloatStateOf(1f) }
    var isResetAvailable by remember { mutableStateOf(false) }
    var clickedChord by remember { mutableStateOf<Chord?>(null) }

    LaunchedEffect(isAutoScrolling, scrollBy) {
        if (!isAutoScrolling) return@LaunchedEffect

        try {
            while (isActive && scrollState.value < scrollState.maxValue) {
                scrollState.scrollBy(scrollBy)
                delay(SCROLL_REFRESH_RATE)
            }
        } finally {
            if (scrollState.value >= scrollState.maxValue) {
                isResetAvailable = true
                isAutoScrolling = false
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedVisibility(
                    visible = isAutoScrolling,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> fullHeight / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight / 2 }),
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                scrollBy *= 2
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Scroll Faster",
                            )
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                val prev = scrollBy
                                val updated = prev / 2
                                scrollBy = if (updated > 0.1f) updated else prev

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

                when {
                    isResetAvailable -> {
                        val scope = rememberCoroutineScope()
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    scrollState.animateScrollTo(0)
                                    isResetAvailable = false
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Auto-scroll",
                            )
                        }
                    }

                    else -> FloatingActionButton(
                        onClick = { isAutoScrolling = !isAutoScrolling },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoScrolling) "Pause Auto-scroll" else "Start Auto-scroll",
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState)
                .padding(16.dp),
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "by ${song.author}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )

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