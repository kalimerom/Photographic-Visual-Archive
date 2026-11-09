package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.PhotoEntity
import com.example.ui.components.WaxPencilHeroMarker
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
import com.example.ui.viewmodel.ArchiveViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    viewModel: ArchiveViewModel,
    onPhotoClick: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToSeries: (Long) -> Unit,
    onNavigateToContactSheet: (Long) -> Unit,
    onNavigateToMixCreate: (Long, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val groupedPhotos by viewModel.groupedPhotos.collectAsStateWithLifecycle()
    val allPhotos by viewModel.allPhotos.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedPhotoIds by viewModel.selectedPhotoIds.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()

    var showSeriesDialog by remember { mutableStateOf(false) }
    var showContactSheetDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Multi-photo picker using standard Android Photo Picker (zero-permission, Play Store compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val list = uris.map { uri ->
                val fileName = queryDisplayName(context, uri)
                Pair(uri, fileName)
            }
            viewModel.importPhotos(list)
        }
    }

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
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.clearSelection() },
                                modifier = Modifier.size(36.dp).testTag("close_selection_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Selection",
                                    tint = ArchiveTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${selectedPhotoIds.size} selected",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArchiveTextPrimary
                            )
                        }
                        TextButton(
                            onClick = { viewModel.selectAll() },
                            modifier = Modifier.testTag("select_all_button")
                        ) {
                            Text(
                                text = "Select All",
                                style = MaterialTheme.typography.labelMedium,
                                color = ArchiveTextSecondary
                            )
                        }
                    } else {
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
                                text = "Photos",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ArchiveTextPrimary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onOpenSearch,
                                modifier = Modifier.size(40.dp).testTag("search_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = ArchiveTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.size(40.dp).testTag("settings_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = ArchiveTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Import progress indicator
                if (importProgress.isImporting) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Importing & indexing EXIF (${importProgress.current}/${importProgress.total})...",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArchiveTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (importProgress.total > 0) importProgress.current.toFloat() / importProgress.total.toFloat() else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = ArchiveAccentNeutral,
                            trackColor = ArchiveSurfaceBorder
                        )
                    }
                }

                // Date Filter Chips (Subtle horizontal row)
                if (!isSelectionMode && allPhotos.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            DateFilterChip(
                                label = "ALL",
                                selected = dateFilter == "ALL",
                                onClick = { viewModel.setDateFilter("ALL") }
                            )
                        }
                        item {
                            DateFilterChip(
                                label = "THIS MONTH",
                                selected = dateFilter == "THIS_MONTH",
                                onClick = { viewModel.setDateFilter("THIS_MONTH") }
                            )
                        }
                        item {
                            DateFilterChip(
                                label = "THIS YEAR",
                                selected = dateFilter == "THIS_YEAR",
                                onClick = { viewModel.setDateFilter("THIS_YEAR") }
                            )
                        }
                        items(availableYears) { year ->
                            DateFilterChip(
                                label = year,
                                selected = dateFilter == year,
                                onClick = { viewModel.setDateFilter(year) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    containerColor = ArchiveSurfaceElevated,
                    contentColor = ArchiveTextPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .border(1.dp, ArchiveSurfaceBorder, CircleShape)
                        .testTag("import_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import Photos",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArchiveSurfaceBorder),
                    color = ArchiveSurface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionActionButton(
                            icon = Icons.Default.Collections,
                            label = "Series",
                            onClick = { showSeriesDialog = true },
                            testTag = "selection_series_action"
                        )
                        SelectionActionButton(
                            icon = Icons.Default.GridView,
                            label = "Contact Sheet",
                            onClick = { showContactSheetDialog = true },
                            testTag = "selection_contact_sheet_action"
                        )
                        SelectionActionButton(
                            icon = Icons.Default.Compare,
                            label = "Mix",
                            onClick = {
                                val list = selectedPhotoIds.toList()
                                if (list.isNotEmpty()) {
                                    val idA = list[0]
                                    val idB = if (list.size > 1) list[1] else null
                                    viewModel.clearSelection()
                                    onNavigateToMixCreate(idA, idB)
                                }
                            },
                            testTag = "selection_mix_action"
                        )
                        SelectionActionButton(
                            icon = Icons.Default.Delete,
                            label = "Delete",
                            tint = ArchiveHeroRed,
                            onClick = { showDeleteConfirmDialog = true },
                            testTag = "selection_delete_action"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val screenWidth = maxWidth
            val columns = if (screenWidth > 600.dp) 4 else 3

            if (allPhotos.isEmpty()) {
                // Minimal Empty State exactly per prompt
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "PHOTOS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No photos yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArchiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Import your first photographs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchiveTextMuted
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = ArchiveSurfaceElevated,
                            contentColor = ArchiveTextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp)
                            .testTag("empty_state_import_button")
                    ) {
                        Text(
                            text = "[ IMPORT ]",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("photos_grid"),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    groupedPhotos.forEach { group ->
                        item(
                            span = { GridItemSpan(columns) },
                            key = "header_${group.title}"
                        ) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = ArchiveTextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        items(
                            items = group.photos,
                            key = { it.id }
                        ) { photo ->
                            val isSelected = selectedPhotoIds.contains(photo.id)
                            PhotoGridCell(
                                photo = photo,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleSelection(photo.id)
                                    } else {
                                        onPhotoClick(photo.id)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        viewModel.startSelection(photo.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create Series
    if (showSeriesDialog) {
        var seriesTitle by remember { mutableStateOf("") }
        var seriesNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSeriesDialog = false },
            containerColor = ArchiveSurface,
            title = {
                Text(
                    text = "New Series",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchiveTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Adding ${selectedPhotoIds.size} photos to a new collection. Original files remain in the main archive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveTextMuted
                    )
                    OutlinedTextField(
                        value = seriesTitle,
                        onValueChange = { seriesTitle = it },
                        placeholder = { Text("Series Title (e.g. Night Walk)", color = ArchiveTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("series_title_input")
                    )
                    OutlinedTextField(
                        value = seriesNote,
                        onValueChange = { seriesNote = it },
                        placeholder = { Text("Optional notes", color = ArchiveTextMuted) },
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
                        if (seriesTitle.isNotBlank()) {
                            viewModel.createSeriesFromSelection(seriesTitle, seriesNote) { newId ->
                                showSeriesDialog = false
                                onNavigateToSeries(newId)
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_series")
                ) {
                    Text("Create Series", color = ArchiveTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeriesDialog = false }) {
                    Text("Cancel", color = ArchiveTextMuted)
                }
            }
        )
    }

    // Dialog: Create Contact Sheet
    if (showContactSheetDialog) {
        var sheetTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showContactSheetDialog = false },
            containerColor = ArchiveSurface,
            title = {
                Text(
                    text = "Create Contact Sheet",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchiveTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Creating a manual contact sheet workspace with ${selectedPhotoIds.size} frames for selection & hero ranking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveTextMuted
                    )
                    OutlinedTextField(
                        value = sheetTitle,
                        onValueChange = { sheetTitle = it },
                        placeholder = { Text("Sheet Title (e.g. Roll #05)", color = ArchiveTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("contact_sheet_title_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = if (sheetTitle.isNotBlank()) sheetTitle else "Contact Sheet"
                        viewModel.createContactSheetFromSelection(title) { sheetId ->
                            showContactSheetDialog = false
                            onNavigateToContactSheet(sheetId)
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_contact_sheet")
                ) {
                    Text("Create Sheet", color = ArchiveTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactSheetDialog = false }) {
                    Text("Cancel", color = ArchiveTextMuted)
                }
            }
        )
    }

    // Dialog: Delete confirm
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = ArchiveSurface,
            title = {
                Text("Delete Photos", style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${selectedPhotoIds.size} selected photos from the archive? This removes the local files.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedPhotos()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete", color = ArchiveHeroRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = ArchiveTextMuted)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGridCell(
    photo: PhotoEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Use lightweight downscaled thumbnail for high performance and smooth scrolling
    val thumbnailFile = remember(photo.thumbnailPath) { File(photo.thumbnailPath) }
    val displayFile = if (thumbnailFile.exists()) thumbnailFile else File(photo.localPath)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(ArchiveSurfaceElevated)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("photo_cell_${photo.id}")
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(displayFile)
                .crossfade(150)
                .build(),
            contentDescription = photo.originalFileName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Hero Wax-pencil marker if marked as hero
        if (photo.selectionStatus == "hero") {
            WaxPencilHeroMarker()
        }

        // Selection overlay
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ArchiveTextPrimary else Color.Black.copy(alpha = 0.5f))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ArchiveBackground))
                }
            }
        }

        // Badges for Edited or Voice Memo
        if (!isSelectionMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (photo.editedVersionPath != null) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EDIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                if (photo.voiceMemoPath != null) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                            .padding(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Memo",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    fontSize = 11.sp
                )
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ArchiveSurface,
            labelColor = ArchiveTextSecondary,
            selectedContainerColor = ArchiveSurfaceElevated,
            selectedLabelColor = ArchiveTextPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = ArchiveSurfaceBorder,
            selectedBorderColor = ArchiveAccentNeutral,
            enabled = true,
            selected = selected
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(28.dp)
    )
}

@Composable
private fun SelectionActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = ArchiveTextPrimary,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tint
        )
    }
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}
