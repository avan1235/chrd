package `in`.procyk.chrd.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import `in`.procyk.chrd.component.Screen
import `in`.procyk.chrd.component.liquid.LiquidBottomTabsSpacer
import `in`.procyk.chrd.model.SongListing
import `in`.procyk.chrd.viewmodel.SearchViewModel


@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onSongSelected: (SongListing) -> Unit,
    useLiquidNavigation: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.background,
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val isLoadingSongs by viewModel.isLoadingSongs.collectAsState()

    Screen { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            if (!isLoadingSongs) LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                reverseLayout = true,
                contentPadding =
                    if (isLoadingSongs) PaddingValues(all = 16.dp)
                    else PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (useLiquidNavigation) 190.dp else 102.dp,
                    ),
            ) {
                items(results) { song ->
                    Card(
                        onClick = { onSongSelected(song) },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = song.author,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 16.dp),
                                )
                                Text(
                                    text = song.origin.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall.run { copy(color = color.copy(alpha = 0.5f)) },
                                )
                            }
                        }
                    }
                }
            }
            else {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            Box(
                Modifier
                    .padding(16.dp)
                    .padding(bottom = if (useLiquidNavigation) 88.dp else 0.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.widthIn(max = 480.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    OutlinedTextField(
                        shape = Capsule(),
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .defaultMinSize(minHeight = 60.dp),
                        value = query,
                        onValueChange = viewModel::onQueryChanged,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = containerColor,
                            unfocusedContainerColor = containerColor,
                            disabledContainerColor = containerColor,
                            errorContainerColor = containerColor,
                        ),
                        placeholder = {
                            Text(
                                "Search songs by titles or authors…",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingIcon = {
                            FilledIconButton(
                                onClick = viewModel::onRequestSearch,
                                shape = Capsule(),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(60.dp),
                                enabled = !isLoadingSongs,
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
