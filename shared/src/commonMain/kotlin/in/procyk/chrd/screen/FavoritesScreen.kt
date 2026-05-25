package `in`.procyk.chrd.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.procyk.chrd.component.Screen
import `in`.procyk.chrd.db.SongRepository
import `in`.procyk.chrd.model.SongListing
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    repository: SongRepository,
    onSongSelected: (SongListing) -> Unit,
) {
    val favorites by repository.favorites.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Screen(title = "Favorites") { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("No favorites yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(favorites) { listing ->
                    Card(
                        onClick = { onSongSelected(listing) },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = listing.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = listing.author,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            IconButton(onClick = {
                                scope.launch { repository.removeFavorite(listing) }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove from favorites"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
