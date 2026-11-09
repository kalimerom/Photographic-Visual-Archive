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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.ExifSummaryRow
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
import com.example.ui.viewmodel.ContactSheetFrame
import com.example.ui.viewmodel.ContactSheetMode
import com.example.ui.viewmodel.ContactSheetViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSheetScreen(
    viewModel: ContactSheetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contactSheet by viewModel.contactSheet.collectAsStateWithLifecycle()
    val frames by viewModel.frames.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val selectedFrameId by viewModel.selectedFrameId.collectAsStateWithLifecycle()

    val activeFrame = frames.find { it.frameEntity.id == selectedFrameId } ?: frames.firstOrNull()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ArchiveBackground),
        containerColor = ArchiveBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).testTag("contact_sheet_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CONTACT SHEET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )
                    Text(
                        text = contactSheet?.title ?: "Contact Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArchiveTextPrimary
                    )
                }

                // View mode toggle: Grid vs Film Strip
                IconButton(
                    onClick = {
                        viewModel.setViewMode(
                            if (viewMode == ContactSheetMode.GRID) ContactSheetMode.FILM_STRIP
                            else ContactSheetMode.GRID
                        )
                    },
                    modifier = Modifier.size(36.dp).testTag("toggle_contact_sheet_view_mode")
                ) {
                    Icon(
                        imageVector = if (viewMode == ContactSheetMode.GRID) Icons.Default.ViewStream else Icons.Default.GridView,
                        contentDescription = "Toggle View Mode",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArchiveSurfaceBorder),
                color = ArchiveSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heroCount = frames.count { it.frameEntity.status == "hero" }
                    val selectedCount = frames.count { it.frameEntity.status == "selected" }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${frames.size} FRAMES",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = ArchiveTextMuted
                        )
                        if (heroCount > 0) {
                            Text(
                                text = "· $heroCount HERO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ArchiveHeroRed
                            )
                        }
                    }

                    if (activeFrame != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STATUS:",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = ArchiveTextMuted
                            )
                            StatusCycleButton(
                                status = activeFrame.frameEntity.status,
                                onClick = { viewModel.cycleFrameStatus(activeFrame.frameEntity.id) }
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
            when (viewMode) {
                ContactSheetMode.GRID -> {
                    // Contact Sheet Grid: 01, 02, 03, 04...
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .testTag("contact_sheet_grid"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(
                            items = frames,
                            key = { _, item -> item.frameEntity.id }
                        ) { index, frame ->
                            ContactSheetFrameCell(
                                index = index + 1,
                                frame = frame,
                                isCurrentActive = frame.frameEntity.id == selectedFrameId,
                                onClick = {
                                    viewModel.selectFrame(frame.frameEntity.id)
                                    viewModel.cycleFrameStatus(frame.frameEntity.id)
                                }
                            )
                        }
                    }
                }
                ContactSheetMode.FILM_STRIP -> {
                    // Contact Sheet Film Strip: Large Image above horizontal filmstrip negative
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Large Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeFrame?.photo != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(activeFrame.photo.localPath))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Active Frame",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (activeFrame.frameEntity.status == "hero") {
                                    WaxPencilHeroMarker()
                                }
                            }
                        }

                        // Frame info bar
                        if (activeFrame?.photo != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ArchiveSurface)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val frameNum = frames.indexOfFirst { it.frameEntity.id == activeFrame.frameEntity.id } + 1
                                Text(
                                    text = String.format(Locale.US, "FRAME #%02d", frameNum),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ArchiveTextPrimary
                                )
                                ExifSummaryRow(photo = activeFrame.photo)
                            }
                        }

                        // Horizontal Film Strip Negatives Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(ArchiveSurfaceElevated)
                                .border(1.dp, ArchiveSurfaceBorder)
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                itemsIndexed(frames) { index, frame ->
                                    FilmStripThumbnail(
                                        index = index + 1,
                                        frame = frame,
                                        isActive = frame.frameEntity.id == activeFrame?.frameEntity?.id,
                                        onClick = { viewModel.selectFrame(frame.frameEntity.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactSheetFrameCell(
    index: Int,
    frame: ContactSheetFrame,
    isCurrentActive: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val photo = frame.photo
    val status = frame.frameEntity.status

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(ArchiveSurface)
            .border(
                1.dp,
                if (status == "hero") ArchiveHeroRed else if (status == "selected") ArchiveAccentNeutral else ArchiveSurfaceBorder
            )
            .clickable(onClick = onClick)
            .padding(2.dp)
            .testTag("frame_cell_$index")
    ) {
        // Frame Index Label: 01, 02...
        Text(
            text = String.format(Locale.US, "%02d", index),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            ),
            color = if (status == "hero") ArchiveHeroRed else ArchiveTextMuted,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(ArchiveSurfaceElevated)
        ) {
            if (photo != null) {
                val thumbFile = File(photo.thumbnailPath)
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (thumbFile.exists()) thumbFile else File(photo.localPath))
                        .crossfade(100)
                        .build(),
                    contentDescription = "Frame $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Hero Wax-pencil marker
            if (status == "hero") {
                WaxPencilHeroMarker()
            } else if (status == "selected") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color.White.copy(alpha = 0.8f))
                )
            }
        }
    }
}

@Composable
private fun FilmStripThumbnail(
    index: Int,
    frame: ContactSheetFrame,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val photo = frame.photo
    val status = frame.frameEntity.status

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(68.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(ArchiveSurface)
            .border(
                1.5.dp,
                if (isActive) Color.White else if (status == "hero") ArchiveHeroRed else ArchiveSurfaceBorder,
                RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format(Locale.US, "%02d", index),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = if (status == "hero") FontWeight.Bold else FontWeight.Normal
            ),
            color = if (status == "hero") ArchiveHeroRed else ArchiveTextMuted
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ArchiveSurfaceElevated)
        ) {
            if (photo != null) {
                val thumbFile = File(photo.thumbnailPath)
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (thumbFile.exists()) thumbFile else File(photo.localPath))
                        .crossfade(100)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (status == "hero") {
                WaxPencilHeroMarker()
            }
        }
    }
}

@Composable
private fun StatusCycleButton(
    status: String,
    onClick: () -> Unit
) {
    val (label, bg, fg) = when (status) {
        "hero" -> Triple("HERO", ArchiveHeroRed, Color.White)
        "selected" -> Triple("SELECTED", ArchiveAccentNeutral, ArchiveBackground)
        else -> Triple("NORMAL", ArchiveSurfaceElevated, ArchiveTextSecondary)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("status_cycle_button")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = fg
        )
    }
}
