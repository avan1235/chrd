package `in`.procyk.chrd.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.chrd.model.LinePart
import `in`.procyk.chrd.model.LinePart.ChordInText
import `in`.procyk.chrd.model.LinePart.ChordOverWhitespace
import `in`.procyk.chrd.model.LinePart.ChordedLyric
import `in`.procyk.chrd.model.LinePart.Lyric
import `in`.procyk.chrd.model.SectionType
import `in`.procyk.chrd.model.Song
import `in`.procyk.chrd.model.SongLine
import `in`.procyk.chrd.model.SongSection
import `in`.procyk.chrd.viewmodel.SongViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@Composable
internal fun SongScreen(
    viewModel: SongViewModel,
) {
    val song by viewModel.song.collectAsState()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
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

@Composable
private fun AutoScrollableSongView(
    song: Song,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var isAutoScrolling by remember { mutableStateOf(false) }
    var scrollSpeedMillis by remember { mutableLongStateOf(36L) }
    var isResetAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(isAutoScrolling) {
        if (!isAutoScrolling) return@LaunchedEffect

        while (isActive && scrollState.value < scrollState.maxValue) {
            scrollState.scrollBy(1f)
            delay(scrollSpeedMillis)
        }
        isResetAvailable = true
        isAutoScrolling = false
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = isAutoScrolling,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> fullHeight / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                val prev = scrollSpeedMillis
                                val updated = prev / 2
                                scrollSpeedMillis = if (updated > 0) updated else prev
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Scroll Faster"
                            )
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                scrollSpeedMillis *= 2
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Scroll Slower"
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Auto-scroll"
                            )
                        }
                    }

                    else -> FloatingActionButton(
                        onClick = { isAutoScrolling = !isAutoScrolling },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoScrolling) "Pause Auto-scroll" else "Start Auto-scroll"
                        )
                    }
                }
            }
        }) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "by ${song.author}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            song.sections.forEach { section ->
                SongSectionView(section)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SongSectionView(section: SongSection) {
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
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        val isChorus = section.type == SectionType.CHORUS
        Column(
            modifier = Modifier.padding(start = if (isChorus) 16.dp else 0.dp) // Indent chorus
        ) {
            section.lines.forEach { line ->
                SongLineView(line, isChorus)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SongLineView(line: SongLine, isChorus: Boolean) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {
        line.parts.forEach { part ->
            LinePartView(part = part, isChorus = isChorus)
        }
    }
}

@Composable
private fun LinePartView(part: LinePart, isChorus: Boolean) {
    val chordColor = MaterialTheme.colorScheme.primary
    val lyricColor =
        if (isChorus) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val lyricFontWeight = if (isChorus) FontWeight.Normal else FontWeight.Thin

    val spacerText = " "

    Column(
        modifier = Modifier.padding(end = 2.dp) // Slight spacing between words
    ) {
        val chordText = when (part) {
            is ChordOverWhitespace -> part.chord.value
            is ChordedLyric -> part.chord.value
            is Lyric -> spacerText
            is ChordInText -> spacerText
        }

        Text(
            text = chordText, color = chordColor, fontWeight = FontWeight.Bold, fontSize = 16.sp
        )

        Text(
            text = when (part) {
                is Lyric -> part.text
                is ChordedLyric -> part.text
                is ChordOverWhitespace -> spacerText
                is ChordInText -> part.chord.value
            },
            color = when (part) {
                is Lyric, is ChordedLyric, is ChordOverWhitespace -> lyricColor
                is ChordInText -> chordColor
            },
            fontWeight = when (part) {
                is Lyric, is ChordedLyric, is ChordOverWhitespace -> lyricFontWeight
                is ChordInText -> FontWeight.Bold
            },
            fontSize = 16.sp,
        )
    }
}