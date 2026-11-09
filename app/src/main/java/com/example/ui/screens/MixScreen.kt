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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.MixEntity
import com.example.data.repository.ResolvedMix
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
import com.example.ui.viewmodel.MixViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixScreen(
    viewModel: MixViewModel,
    onMixClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allMixes by viewModel.allMixes.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ArchiveBackground),
        containerColor = ArchiveBackground,
        topBar = {
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
                        text = "Mix",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArchiveTextPrimary
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.size(36.dp).testTag("create_mix_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Mix",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ArchiveSurfaceElevated,
                contentColor = ArchiveTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .border(1.dp, ArchiveSurfaceBorder, CircleShape)
                    .testTag("mix_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Mix", tint = ArchiveTextPrimary)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ArchiveBackground)
        ) {
            if (allMixes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MIX",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ArchiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No pairings yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArchiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pair ANY two images inside the archive — personal photos, inspiration studies, or technical comparisons.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchiveTextMuted
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                            .testTag("empty_state_create_mix")
                    ) {
                        Text("[ CREATE MIX ]", color = ArchiveTextPrimary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("mixes_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(allMixes, key = { it.id }) { mix ->
                        MixCard(
                            mix = mix,
                            viewModel = viewModel,
                            onClick = { onMixClick(mix.id) },
                            onDelete = { viewModel.deleteMix(mix.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateMixSheet(
            viewModel = viewModel,
            preselectedPhotoId = null,
            onDismiss = { showCreateDialog = false },
            onCreated = { mixId ->
                showCreateDialog = false
                onMixClick(mixId)
            }
        )
    }
}

@Composable
private fun MixCard(
    mix: MixEntity,
    viewModel: MixViewModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var resolved by remember { mutableStateOf<ResolvedMix?>(null) }

    LaunchedEffect(mix) {
        resolved = viewModel.resolveMixPreview(mix)
    }

    val res = resolved
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(mix.createdAt))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ArchiveSurface)
            .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("mix_card_${mix.id}")
    ) {
        // Paired visual dual-thumbnail: Image A × Image B
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Image A
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ArchiveSurfaceElevated)
            ) {
                if (res?.itemA != null) {
                    val thumbFile = File(res.itemA.thumbnailPath)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (thumbFile.exists()) thumbFile else File(res.itemA.localPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = res.itemA.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = if (res.itemA.type == "photo") "ARCHIVE" else "INSPIRATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 8.sp),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Central pairing marker
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(ArchiveSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    color = ArchiveTextSecondary
                )
            }

            // Image B
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ArchiveSurfaceElevated)
            ) {
                if (res?.itemB != null) {
                    val thumbFile = File(res.itemB.thumbnailPath)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (thumbFile.exists()) thumbFile else File(res.itemB.localPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = res.itemB.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = if (res.itemB.type == "photo") "ARCHIVE" else "INSPIRATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 8.sp),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mix.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchiveTextPrimary
                )
                mix.note?.let { note ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveTextMuted,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Created $dateStr",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = ArchiveTextMuted
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Mix",
                    tint = ArchiveTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMixSheet(
    viewModel: MixViewModel,
    preselectedPhotoId: Long? = null,
    onDismiss: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val archivePhotos by viewModel.archivePhotos.collectAsStateWithLifecycle()
    val inspirationImages by viewModel.inspirationImages.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var selectedImageAId by remember { mutableStateOf(preselectedPhotoId ?: archivePhotos.firstOrNull()?.id ?: 0L) }
    var selectedImageAType by remember { mutableStateOf("photo") }

    var selectedImageBId by remember {
        mutableStateOf(
            inspirationImages.firstOrNull()?.id ?: (archivePhotos.getOrNull(1)?.id ?: 0L)
        )
    }
    var selectedImageBType by remember {
        mutableStateOf(if (inspirationImages.isNotEmpty()) "inspiration" else "photo")
    }

    var pickerActiveForSlot by remember { mutableStateOf<String?>(null) } // "A" or "B" or null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ArchiveSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "PAIR IMAGES INTO A MIX",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = ArchiveTextSecondary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Mix Title (e.g. My Street Study × Saul Leiter)", color = ArchiveTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ArchiveTextPrimary,
                    unfocusedTextColor = ArchiveTextPrimary,
                    focusedBorderColor = ArchiveAccentNeutral,
                    unfocusedBorderColor = ArchiveSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("mix_title_input")
            )

            // Slot A and Slot B Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Slot A
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArchiveSurfaceElevated)
                        .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                        .clickable { pickerActiveForSlot = "A" }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "IMAGE A",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ArchiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedImageAType == "photo") "Archive #${selectedImageAId}" else "Insp #${selectedImageAId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveTextPrimary
                    )
                    Text(
                        text = "Tap to change",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ArchiveTextMuted
                    )
                }

                Box(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                        color = ArchiveTextSecondary
                    )
                }

                // Slot B
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArchiveSurfaceElevated)
                        .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                        .clickable { pickerActiveForSlot = "B" }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "IMAGE B",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ArchiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedImageBType == "photo") "Archive #${selectedImageBId}" else "Insp #${selectedImageBId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveTextPrimary
                    )
                    Text(
                        text = "Tap to change",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ArchiveTextMuted
                    )
                }
            }

            // Quick thumbnail picker strip when user tapped Slot A or B
            pickerActiveForSlot?.let { slot ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArchiveBackground)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Choose for Slot $slot:",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ArchiveTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "Personal Photos:", style = MaterialTheme.typography.labelSmall, color = ArchiveTextSecondary)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(archivePhotos) { p ->
                            val thumbFile = File(p.thumbnailPath)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .clickable {
                                        if (slot == "A") {
                                            selectedImageAId = p.id
                                            selectedImageAType = "photo"
                                        } else {
                                            selectedImageBId = p.id
                                            selectedImageBType = "photo"
                                        }
                                        pickerActiveForSlot = null
                                    }
                            ) {
                                AsyncImage(
                                    model = if (thumbFile.exists()) thumbFile else File(p.localPath),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    if (inspirationImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Inspiration References:", style = MaterialTheme.typography.labelSmall, color = ArchiveTextSecondary)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(inspirationImages) { insp ->
                                val thumbFile = File(insp.thumbnailPath)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .clickable {
                                            if (slot == "A") {
                                                selectedImageAId = insp.id
                                                selectedImageAType = "inspiration"
                                            } else {
                                                selectedImageBId = insp.id
                                                selectedImageBType = "inspiration"
                                            }
                                            pickerActiveForSlot = null
                                        }
                                ) {
                                    AsyncImage(
                                        model = if (thumbFile.exists()) thumbFile else File(insp.localPath),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Comparison notes / Composition analysis", color = ArchiveTextMuted) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ArchiveTextPrimary,
                    unfocusedTextColor = ArchiveTextPrimary,
                    focusedBorderColor = ArchiveAccentNeutral,
                    unfocusedBorderColor = ArchiveSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = ArchiveTextMuted) }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val finalTitle = if (title.isNotBlank()) title else "Visual Mix"
                        viewModel.createMix(
                            title = finalTitle,
                            note = note,
                            imageAId = selectedImageAId,
                            imageAType = selectedImageAType,
                            imageBId = selectedImageBId,
                            imageBType = selectedImageBType
                        ) { newMixId ->
                            onCreated(newMixId)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                    modifier = Modifier.testTag("confirm_create_mix_button")
                ) {
                    Text("Save Mix", color = ArchiveTextPrimary)
                }
            }
        }
    }
}
