package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.ExifSummaryRow
import com.example.ui.components.FullExifBottomSheet
import com.example.ui.components.WaxPencilHeroMarker
import com.example.ui.theme.ArchiveAccentNeutral
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveHeroRed
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveSurfaceElevated
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import com.example.ui.viewmodel.PhotoDetailViewModel
import com.example.util.AudioPlaybackState
import com.example.util.AudioRecordState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PhotoDetailScreen(
    viewModel: PhotoDetailViewModel,
    onBack: () -> Unit,
    onNavigateToBeforeAfter: (Long) -> Unit,
    onNavigateToMix: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photo by viewModel.photo.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val audioRecordState by viewModel.audioRecordState.collectAsStateWithLifecycle()
    val audioPlaybackState by viewModel.audioPlaybackState.collectAsStateWithLifecycle()
    val isRecordingVoiceMemo by viewModel.isRecordingVoiceMemo.collectAsStateWithLifecycle()

    var showFullExifSheet by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }

    // Audio permission launcher for recording voice memo
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceMemoRecording()
        }
    }

    // Zoom and pan state for focused image viewer
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    if (photo == null) {
        Box(modifier = Modifier.fillMaxSize().background(ArchiveBackground))
        return
    }

    val currentPhoto = photo!!

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
                    modifier = Modifier.size(40.dp).testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = currentPhoto.originalFileName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    ),
                    color = ArchiveTextSecondary
                )

                // Selection status chip (Keep, Reject, Hero)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        label = "HERO",
                        isActive = currentPhoto.selectionStatus == "hero",
                        activeColor = ArchiveHeroRed,
                        onClick = {
                            viewModel.setSelectionStatus(
                                if (currentPhoto.selectionStatus == "hero") "none" else "hero"
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Focused Photo Viewer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clipToBounds()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(currentPhoto.localPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = currentPhoto.originalFileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )

                if (currentPhoto.selectionStatus == "hero") {
                    WaxPencilHeroMarker()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Essential EXIF row: f/1.8   1/500   ISO 800   35mm
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExifSummaryRow(photo = currentPhoto)

                val dateStr = SimpleDateFormat("MMMM d, yyyy · HH:mm", Locale.US).format(Date(currentPhoto.captureDate))
                val cameraModel = listOfNotNull(currentPhoto.cameraModel, currentPhoto.lensModel).joinToString(" · ")
                Text(
                    text = if (cameraModel.isNotBlank()) "$dateStr  |  $cameraModel" else dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveTextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChip(
                    icon = Icons.Default.Compare,
                    label = "Before / After",
                    onClick = { onNavigateToBeforeAfter(currentPhoto.id) },
                    testTag = "detail_action_before_after"
                )
                ActionChip(
                    icon = Icons.Default.Info,
                    label = "Full EXIF",
                    onClick = { showFullExifSheet = true },
                    testTag = "detail_action_full_exif"
                )
                ActionChip(
                    icon = Icons.Default.Compare,
                    label = "Add to Mix",
                    onClick = { onNavigateToMix(currentPhoto.id) },
                    testTag = "detail_action_add_to_mix"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Memo Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArchiveSurface)
                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOICE MEMO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )

                    if (currentPhoto.voiceMemoPath != null) {
                        IconButton(
                            onClick = { viewModel.deleteVoiceMemo() },
                            modifier = Modifier.size(28.dp).testTag("delete_voice_memo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Voice Memo",
                                tint = ArchiveTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isRecordingVoiceMemo) {
                    // Recording in progress
                    val recordingDurationMs = when (val state = audioRecordState) {
                        is AudioRecordState.Recording -> state.durationMs
                        else -> 0L
                    }
                    val secs = (recordingDurationMs / 1000) % 60
                    val mins = (recordingDurationMs / 60000)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ArchiveHeroRed))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.US, "🎙 %02d:%02d", mins, secs),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                color = ArchiveHeroRed
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { viewModel.cancelVoiceMemoRecording() }
                            ) {
                                Text("Cancel", color = ArchiveTextMuted)
                            }
                            TextButton(
                                onClick = { viewModel.stopVoiceMemoRecording() },
                                colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                                modifier = Modifier.testTag("stop_recording_button")
                            ) {
                                Text("Save", color = ArchiveTextPrimary)
                            }
                        }
                    }
                } else if (currentPhoto.voiceMemoPath != null) {
                    // Recorded memo playback
                    val durationMs = currentPhoto.voiceMemoDurationMs ?: 0L
                    val isPlaying = audioPlaybackState is AudioPlaybackState.Playing

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) viewModel.pauseVoiceMemo() else viewModel.playVoiceMemo()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ArchiveSurfaceElevated, CircleShape)
                                    .testTag("play_pause_voice_memo_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = ArchiveTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            val durSec = (durationMs / 1000) % 60
                            val durMin = durationMs / 60000
                            Text(
                                text = String.format(Locale.US, "🎙 Audio Note (%02d:%02d)", durMin, durSec),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = ArchiveTextPrimary
                            )
                        }
                    }
                } else {
                    // No memo yet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No voice memo attached",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArchiveTextMuted
                        )
                        TextButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.startVoiceMemoRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.testTag("start_recording_button")
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Memo", color = ArchiveTextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )
                    IconButton(
                        onClick = { showTagDialog = true },
                        modifier = Modifier.size(28.dp).testTag("add_tag_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tag",
                            tint = ArchiveTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (tags.isEmpty()) {
                        Text(
                            text = "No tags",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArchiveTextMuted
                        )
                    } else {
                        tags.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ArchiveSurfaceElevated)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${tag.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = ArchiveTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    tint = ArchiveTextMuted,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.removeTag(tag.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Full EXIF Sheet
    if (showFullExifSheet) {
        FullExifBottomSheet(
            photo = currentPhoto,
            onDismiss = { showFullExifSheet = false }
        )
    }

    // Add Tag Dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            containerColor = ArchiveSurface,
            title = {
                Text("Add Tag", style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary)
            },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    placeholder = { Text("e.g. street, 35mm, rain", color = ArchiveTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArchiveTextPrimary,
                        unfocusedTextColor = ArchiveTextPrimary,
                        focusedBorderColor = ArchiveAccentNeutral,
                        unfocusedBorderColor = ArchiveSurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("tag_input_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            viewModel.addTag(newTagInput)
                            newTagInput = ""
                            showTagDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_tag_button")
                ) {
                    Text("Add", color = ArchiveTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel", color = ArchiveTextMuted)
                }
            }
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) activeColor else ArchiveSurfaceElevated)
            .border(1.dp, if (isActive) activeColor else ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("status_pill_$label")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = if (isActive) Color.White else ArchiveTextSecondary
        )
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ArchiveSurfaceElevated)
            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(14.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ArchiveTextPrimary)
    }
}
