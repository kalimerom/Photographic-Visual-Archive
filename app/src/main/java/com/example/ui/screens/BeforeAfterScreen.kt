package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PreciseSplitSlider
import com.example.ui.components.SideBySideComparison
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

enum class ComparisonMode {
    SPLIT_SLIDER,
    SIDE_BY_SIDE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforeAfterScreen(
    viewModel: PhotoDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photo by viewModel.photo.collectAsStateWithLifecycle()
    var comparisonMode by remember { mutableStateOf(ComparisonMode.SPLIT_SLIDER) }
    var isZoomLocked by remember { mutableStateOf(true) }

    // Edited version image picker
    val pickEditedLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachEditedVersion(uri)
        }
    }

    if (photo == null) {
        Box(modifier = Modifier.fillMaxSize().background(ArchiveBackground))
        return
    }

    val currentPhoto = photo!!
    val originalPath = currentPhoto.localPath
    val editedPath = currentPhoto.editedVersionPath

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
                    modifier = Modifier.size(40.dp).testTag("before_after_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "BEFORE / AFTER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ArchiveTextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Lock toggle
                    IconButton(
                        onClick = { isZoomLocked = !isZoomLocked },
                        modifier = Modifier.size(36.dp).testTag("zoom_lock_toggle")
                    ) {
                        Icon(
                            imageVector = if (isZoomLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (isZoomLocked) "Zoom Lock Enabled" else "Zoom Lock Disabled",
                            tint = if (isZoomLocked) ArchiveAccentNeutral else ArchiveTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Mode toggle (Split Slider vs Side by Side)
                    IconButton(
                        onClick = {
                            comparisonMode = if (comparisonMode == ComparisonMode.SPLIT_SLIDER) {
                                ComparisonMode.SIDE_BY_SIDE
                            } else {
                                ComparisonMode.SPLIT_SLIDER
                            }
                        },
                        modifier = Modifier.size(36.dp).testTag("comparison_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (comparisonMode == ComparisonMode.SPLIT_SLIDER) Icons.Default.ViewColumn else Icons.Default.ViewStream,
                            contentDescription = "Toggle comparison mode",
                            tint = ArchiveTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                    // Controls info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (comparisonMode == ComparisonMode.SPLIT_SLIDER) "MODE: SPLIT SLIDER" else "MODE: SIDE-BY-SIDE",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = ArchiveTextMuted
                        )
                        if (isZoomLocked) {
                            Text(
                                text = "· ZOOM LOCK",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = ArchiveTextSecondary
                            )
                        }
                    }

                    // Attach / Replace / Remove edited version
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (editedPath != null) {
                            TextButton(
                                onClick = { viewModel.removeEditedVersion() },
                                modifier = Modifier.testTag("remove_edited_button")
                            ) {
                                Text("Remove", color = ArchiveHeroRed, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        TextButton(
                            onClick = {
                                pickEditedLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                            modifier = Modifier.testTag("attach_edited_button")
                        ) {
                            Text(
                                text = if (editedPath == null) "Attach Edited" else "Replace",
                                color = ArchiveTextPrimary,
                                style = MaterialTheme.typography.labelSmall
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
            if (editedPath == null) {
                // Prompt to select an edited version
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "NO EDITED VERSION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Attach an edited file to compare against the original.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchiveTextMuted
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(
                        onClick = {
                            pickEditedLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                            .testTag("select_edited_version_prompt")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ArchiveTextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Edited Version", color = ArchiveTextPrimary)
                    }
                }
            } else {
                when (comparisonMode) {
                    ComparisonMode.SPLIT_SLIDER -> {
                        PreciseSplitSlider(
                            originalPath = originalPath,
                            editedPath = editedPath,
                            isZoomLocked = isZoomLocked,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ComparisonMode.SIDE_BY_SIDE -> {
                        SideBySideComparison(
                            imageAPath = originalPath,
                            imageBPath = editedPath,
                            labelA = "ORIGINAL",
                            labelB = "EDITED",
                            isZoomLocked = isZoomLocked,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
