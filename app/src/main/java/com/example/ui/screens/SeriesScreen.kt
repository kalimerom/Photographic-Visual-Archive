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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ContactSheetEntity
import com.example.data.model.SeriesEntity
import com.example.ui.theme.ArchiveAccentNeutral
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveDivider
import com.example.ui.theme.ArchiveHeroRed
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveSurfaceElevated
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import com.example.ui.viewmodel.SeriesViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    viewModel: SeriesViewModel,
    onSeriesClick: (Long) -> Unit,
    onContactSheetClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val allContactSheets by viewModel.allContactSheets.collectAsStateWithLifecycle()
    val allPhotos by viewModel.allPhotos.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Series, 1 = Contact Sheets
    var showCreateSeriesDialog by remember { mutableStateOf(false) }

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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VISUAL ARCHIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ArchiveTextSecondary
                        )
                        Text(
                            text = "Collections",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArchiveTextPrimary
                        )
                    }

                    if (selectedTabIndex == 0) {
                        IconButton(
                            onClick = { showCreateSeriesDialog = true },
                            modifier = Modifier.size(36.dp).testTag("create_series_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Series",
                                tint = ArchiveTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Sub-tabs: SERIES vs CONTACT SHEETS
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = ArchiveBackground,
                    contentColor = ArchiveTextPrimary,
                    divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ArchiveDivider)) },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ArchiveAccentNeutral,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = "SERIES (${allSeries.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = "CONTACT SHEETS (${allContactSheets.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    )
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
            if (selectedTabIndex == 0) {
                // SERIES LIST
                if (allSeries.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SERIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ArchiveTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No series yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArchiveTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select photos from the Photos archive to create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArchiveTextMuted
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TextButton(
                            onClick = { showCreateSeriesDialog = true },
                            colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                .testTag("empty_state_create_series")
                        ) {
                            Text("[ CREATE SERIES ]", color = ArchiveTextPrimary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("series_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allSeries, key = { it.id }) { series ->
                            val coverPhoto = allPhotos.find { it.id == series.coverPhotoId }
                            SeriesListItem(
                                series = series,
                                coverPath = coverPhoto?.thumbnailPath ?: coverPhoto?.localPath,
                                onClick = { onSeriesClick(series.id) },
                                onDelete = { viewModel.deleteSeries(series.id) }
                            )
                        }
                    }
                }
            } else {
                // CONTACT SHEETS LIST
                if (allContactSheets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CONTACT SHEETS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ArchiveTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No contact sheets yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArchiveTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select photos in the Photos tab and tap 'Contact Sheet' to create a manual workspace.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArchiveTextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("contact_sheets_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allContactSheets, key = { it.id }) { sheet ->
                            ContactSheetListItem(
                                sheet = sheet,
                                onClick = { onContactSheetClick(sheet.id) },
                                onDelete = { viewModel.deleteContactSheet(sheet.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateSeriesDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateSeriesDialog = false },
            containerColor = ArchiveSurface,
            title = {
                Text("New Series", style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Series Title (e.g. Night Walk)", color = ArchiveTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_series_title_field")
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Description / Statement (optional)", color = ArchiveTextMuted) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createSeries(title, description, emptyList()) { newId ->
                                showCreateSeriesDialog = false
                                onSeriesClick(newId)
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_new_series_button")
                ) {
                    Text("Create", color = ArchiveTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSeriesDialog = false }) {
                    Text("Cancel", color = ArchiveTextMuted)
                }
            }
        )
    }
}

@Composable
private fun SeriesListItem(
    series: SeriesEntity,
    coverPath: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("MMM yyyy", Locale.US).format(Date(series.createdAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ArchiveSurface)
            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("series_item_${series.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover thumbnail
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ArchiveSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (coverPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(coverPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = null,
                    tint = ArchiveTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = series.title,
                style = MaterialTheme.typography.titleMedium,
                color = ArchiveTextPrimary
            )
            series.description?.let { desc ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveTextMuted,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created $dateStr",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = ArchiveTextMuted
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Series",
                tint = ArchiveTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ContactSheetListItem(
    sheet: ContactSheetEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(sheet.createdAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ArchiveSurface)
            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("contact_sheet_item_${sheet.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ArchiveSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = ArchiveTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sheet.title,
                style = MaterialTheme.typography.titleMedium,
                color = ArchiveTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Created $dateStr",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = ArchiveTextMuted
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Contact Sheet",
                tint = ArchiveTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
