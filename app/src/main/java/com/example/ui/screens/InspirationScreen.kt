package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.InspirationImageEntity
import com.example.data.model.InspirationPhotographerEntity
import com.example.data.model.InspirationProjectEntity
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
import com.example.ui.viewmodel.InspirationTab
import com.example.ui.viewmodel.InspirationViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationScreen(
    viewModel: InspirationViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val images by viewModel.filteredImages.collectAsStateWithLifecycle()
    val allImages by viewModel.allImages.collectAsStateWithLifecycle()
    val photographers by viewModel.allPhotographers.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val photographerFilter by viewModel.photographerFilter.collectAsStateWithLifecycle()

    var showAddInspirationSheet by remember { mutableStateOf(false) }
    var showAddPhotographerDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var selectedDetailImage by remember { mutableStateOf<InspirationImageEntity?>(null) }

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
                            text = "Inspiration",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArchiveTextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showAddInspirationSheet = true },
                            modifier = Modifier.size(36.dp).testTag("add_inspiration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Reference",
                                tint = ArchiveTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Filter chip if photographer filter is active
                if (photographerFilter != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtered by: ${photographerFilter}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = ArchiveTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Filter",
                            tint = ArchiveTextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.setPhotographerFilter(null) }
                        )
                    }
                }

                // Tabs: IMAGES, PHOTOGRAPHERS, PROJECTS
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = ArchiveBackground,
                    contentColor = ArchiveTextPrimary,
                    divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ArchiveDivider)) },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = ArchiveAccentNeutral,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == InspirationTab.IMAGES,
                        onClick = { viewModel.setSelectedTab(InspirationTab.IMAGES) },
                        text = {
                            Text(
                                text = "IMAGES (${allImages.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == InspirationTab.PHOTOGRAPHERS,
                        onClick = { viewModel.setSelectedTab(InspirationTab.PHOTOGRAPHERS) },
                        text = {
                            Text(
                                text = "ARTISTS (${photographers.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == InspirationTab.PROJECTS,
                        onClick = { viewModel.setSelectedTab(InspirationTab.PROJECTS) },
                        text = {
                            Text(
                                text = "PROJECTS (${projects.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        InspirationTab.IMAGES -> showAddInspirationSheet = true
                        InspirationTab.PHOTOGRAPHERS -> showAddPhotographerDialog = true
                        InspirationTab.PROJECTS -> showAddProjectDialog = true
                    }
                },
                containerColor = ArchiveSurfaceElevated,
                contentColor = ArchiveTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .border(1.dp, ArchiveSurfaceBorder, CircleShape)
                    .testTag("inspiration_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = ArchiveTextPrimary)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ArchiveBackground)
        ) {
            when (selectedTab) {
                InspirationTab.IMAGES -> {
                    if (images.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "INSPIRATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = ArchiveTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No references yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArchiveTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Save reference photographs, photographers, and project studies separately from your personal work.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArchiveTextMuted
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            TextButton(
                                onClick = { showAddInspirationSheet = true },
                                colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(4.dp))
                                    .testTag("empty_state_add_inspiration")
                            ) {
                                Text("[ ADD INSPIRATION ]", color = ArchiveTextPrimary)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("inspiration_images_grid"),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(images, key = { it.id }) { item ->
                                InspirationCell(
                                    item = item,
                                    onClick = { selectedDetailImage = item }
                                )
                            }
                        }
                    }
                }
                InspirationTab.PHOTOGRAPHERS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("photographers_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(photographers, key = { it.id }) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArchiveSurface)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setPhotographerFilter(p.name) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ArchiveSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ArchiveTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = p.name, style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary)
                                    p.notes?.let { n ->
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = n, style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
                InspirationTab.PROJECTS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("projects_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(projects, key = { it.id }) { pr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArchiveSurface)
                                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ArchiveSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = ArchiveTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = pr.title, style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary)
                                    pr.notes?.let { n ->
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = n, style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Add Inspiration
    if (showAddInspirationSheet) {
        AddInspirationBottomSheet(
            onDismiss = { showAddInspirationSheet = false },
            onSave = { uri, pName, prName, url, note, tags ->
                viewModel.addInspiration(uri, pName, prName, url, note, tags) {
                    showAddInspirationSheet = false
                }
            }
        )
    }

    // Detail Dialog for Inspiration Image
    selectedDetailImage?.let { insp ->
        InspirationDetailDialog(
            item = insp,
            onDismiss = { selectedDetailImage = null },
            onDelete = {
                viewModel.deleteInspiration(insp.id)
                selectedDetailImage = null
            }
        )
    }

    // Add Photographer Dialog
    if (showAddPhotographerDialog) {
        var pName by remember { mutableStateOf("") }
        var pNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPhotographerDialog = false },
            containerColor = ArchiveSurface,
            title = { Text("Add Photographer / Artist", style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        placeholder = { Text("Name (e.g. Saul Leiter)", color = ArchiveTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("photographer_name_input")
                    )
                    OutlinedTextField(
                        value = pNotes,
                        onValueChange = { pNotes = it },
                        placeholder = { Text("Notes / Style bio (optional)", color = ArchiveTextMuted) },
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
                        if (pName.isNotBlank()) {
                            viewModel.addPhotographer(pName, pNotes)
                            showAddPhotographerDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_photographer")
                ) {
                    Text("Save", color = ArchiveTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPhotographerDialog = false }) { Text("Cancel", color = ArchiveTextMuted) }
            }
        )
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        var prTitle by remember { mutableStateOf("") }
        var prNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddProjectDialog = false },
            containerColor = ArchiveSurface,
            title = { Text("Add Project / Monograph", style = MaterialTheme.typography.titleMedium, color = ArchiveTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prTitle,
                        onValueChange = { prTitle = it },
                        placeholder = { Text("Title (e.g. Early Color)", color = ArchiveTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArchiveTextPrimary,
                            unfocusedTextColor = ArchiveTextPrimary,
                            focusedBorderColor = ArchiveAccentNeutral,
                            unfocusedBorderColor = ArchiveSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("project_title_input")
                    )
                    OutlinedTextField(
                        value = prNotes,
                        onValueChange = { prNotes = it },
                        placeholder = { Text("Notes / Period (optional)", color = ArchiveTextMuted) },
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
                        if (prTitle.isNotBlank()) {
                            viewModel.addProject(prTitle, null, prNotes)
                            showAddProjectDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_project")
                ) {
                    Text("Save", color = ArchiveTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProjectDialog = false }) { Text("Cancel", color = ArchiveTextMuted) }
            }
        )
    }
}

@Composable
private fun InspirationCell(
    item: InspirationImageEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val hasImage = item.thumbnailPath.isNotBlank() && File(item.thumbnailPath).exists()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(ArchiveSurfaceElevated)
            .clickable(onClick = onClick)
            .testTag("inspiration_cell_${item.id}")
    ) {
        if (hasImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(item.thumbnailPath))
                    .crossfade(true)
                    .build(),
                contentDescription = item.photographerName ?: "Inspiration Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // URL reference without image
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = ArchiveTextMuted, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.photographerName ?: "Reference",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArchiveTextSecondary,
                    maxLines = 1
                )
            }
        }

        // Overlay with photographer name
        item.photographerName?.let { name ->
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInspirationBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Uri?, String?, String?, String?, String?, List<String>) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var photographer by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ArchiveSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ADD INSPIRATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = ArchiveTextSecondary
            )

            // Image Picker Button / Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArchiveSurfaceElevated)
                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .testTag("pick_inspiration_image_button"),
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "Selected Inspiration",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Photo, contentDescription = null, tint = ArchiveTextMuted, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to choose reference image (optional)", style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted)
                    }
                }
            }

            OutlinedTextField(
                value = photographer,
                onValueChange = { photographer = it },
                placeholder = { Text("Photographer (e.g. Saul Leiter)", color = ArchiveTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ArchiveTextPrimary,
                    unfocusedTextColor = ArchiveTextPrimary,
                    focusedBorderColor = ArchiveAccentNeutral,
                    unfocusedBorderColor = ArchiveSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("inspiration_photographer_input")
            )

            OutlinedTextField(
                value = project,
                onValueChange = { project = it },
                placeholder = { Text("Project / Monograph (e.g. Early Color)", color = ArchiveTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ArchiveTextPrimary,
                    unfocusedTextColor = ArchiveTextPrimary,
                    focusedBorderColor = ArchiveAccentNeutral,
                    unfocusedBorderColor = ArchiveSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("inspiration_project_input")
            )

            OutlinedTextField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it },
                placeholder = { Text("Source URL (optional)", color = ArchiveTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ArchiveTextPrimary,
                    unfocusedTextColor = ArchiveTextPrimary,
                    focusedBorderColor = ArchiveAccentNeutral,
                    unfocusedBorderColor = ArchiveSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                placeholder = { Text("Tags (comma separated: color, street, rain)", color = ArchiveTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ArchiveTextPrimary,
                    unfocusedTextColor = ArchiveTextPrimary,
                    focusedBorderColor = ArchiveAccentNeutral,
                    unfocusedBorderColor = ArchiveSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("inspiration_tags_input")
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Notes / Visual study comments", color = ArchiveTextMuted) },
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
                        val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(selectedUri, photographer, project, sourceUrl, note, tagsList)
                    },
                    colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                    modifier = Modifier.testTag("save_inspiration_button")
                ) {
                    Text("Save Reference", color = ArchiveTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun InspirationDetailDialog(
    item: InspirationImageEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val hasImage = item.localPath.isNotBlank() && File(item.localPath).exists()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArchiveSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.photographerName ?: "Inspiration",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArchiveTextPrimary
                    )
                    item.projectName?.let { pr ->
                        Text(text = pr, style = MaterialTheme.typography.bodySmall, color = ArchiveTextSecondary)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ArchiveHeroRed, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (hasImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(item.localPath))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                item.note?.let { n ->
                    Text(text = n, style = MaterialTheme.typography.bodySmall, color = ArchiveTextPrimary)
                }

                item.sourceUrl?.let { url ->
                    Text(
                        text = "Source: $url",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ArchiveTextMuted,
                        maxLines = 1
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = ArchiveTextPrimary) }
        }
    )
}
