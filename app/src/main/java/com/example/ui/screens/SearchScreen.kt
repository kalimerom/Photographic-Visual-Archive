package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.ExifSummaryRow
import com.example.ui.theme.ArchiveAccentNeutral
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveDivider
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveSurfaceElevated
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import com.example.ui.viewmodel.SearchViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onPhotoClick: (Long) -> Unit,
    onSeriesClick: (Long) -> Unit,
    onMixClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()

    val quickFilterKeywords = listOf("35mm", "ISO 800", "f/1.8", "street", "monochrome", "Saul Leiter", "Leica")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ArchiveBackground),
        containerColor = ArchiveBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArchiveBackground)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp).testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ArchiveTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.setQuery(it) },
                        placeholder = { Text("Search photos, gear, tags, series, mix...", color = ArchiveTextMuted, fontSize = 13.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearQuery() }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = ArchiveTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder,
                            focusedContainerColor = ArchiveSurface,
                            unfocusedContainerColor = ArchiveSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("search_input_field")
                    )
                }

                // Quick keywords row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(quickFilterKeywords) { kw ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(ArchiveSurface)
                                .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(3.dp))
                                .clickable { viewModel.setQuery(kw) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = kw,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = ArchiveTextSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ArchiveBackground)
        ) {
            if (query.length < 2) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "LOCAL ARCHIVE SEARCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Search filenames, cameras, lenses, focal lengths, aperture, ISO, tags, series, and inspiration studies.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveTextMuted
                    )
                }
            } else if (results.isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found for \"$query\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchiveTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("search_results_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Photos matches
                    if (results.photos.isNotEmpty()) {
                        item {
                            Text(
                                text = "PHOTOS (${results.photos.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ArchiveTextSecondary
                            )
                        }
                        items(results.photos) { photo ->
                            val thumbFile = File(photo.thumbnailPath)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ArchiveSurface)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                    .clickable { onPhotoClick(photo.id) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ArchiveSurfaceElevated)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(if (thumbFile.exists()) thumbFile else File(photo.localPath))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = photo.originalFileName, style = MaterialTheme.typography.titleSmall, color = ArchiveTextPrimary)
                                    ExifSummaryRow(photo = photo)
                                }
                            }
                        }
                    }

                    // Series matches
                    if (results.series.isNotEmpty()) {
                        item {
                            Text(
                                text = "SERIES (${results.series.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ArchiveTextSecondary
                            )
                        }
                        items(results.series) { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ArchiveSurface)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                    .clickable { onSeriesClick(s.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Collections, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = s.title, style = MaterialTheme.typography.titleSmall, color = ArchiveTextPrimary)
                                    s.description?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted) }
                                }
                            }
                        }
                    }

                    // Mix matches
                    if (results.mixes.isNotEmpty()) {
                        item {
                            Text(
                                text = "MIXES (${results.mixes.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ArchiveTextSecondary
                            )
                        }
                        items(results.mixes) { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ArchiveSurface)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                    .clickable { onMixClick(m.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Compare, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = m.title, style = MaterialTheme.typography.titleSmall, color = ArchiveTextPrimary)
                                    m.note?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted) }
                                }
                            }
                        }
                    }

                    // Inspiration matches
                    if (results.inspiration.isNotEmpty()) {
                        item {
                            Text(
                                text = "INSPIRATION (${results.inspiration.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ArchiveTextSecondary
                            )
                        }
                        items(results.inspiration) { insp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ArchiveSurface)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val thumbFile = File(insp.thumbnailPath)
                                if (thumbFile.exists()) {
                                    AsyncImage(
                                        model = thumbFile,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column {
                                    Text(text = insp.photographerName ?: "Artist", style = MaterialTheme.typography.titleSmall, color = ArchiveTextPrimary)
                                    insp.projectName?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = ArchiveTextSecondary) }
                                    insp.note?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = ArchiveTextMuted) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
