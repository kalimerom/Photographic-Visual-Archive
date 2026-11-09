package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.VisualArchiveApplication
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveHeroRed
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveSurfaceElevated
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as VisualArchiveApplication
    val scope = rememberCoroutineScope()

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
                    modifier = Modifier.size(40.dp).testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "SETTINGS & ARCHIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ArchiveTextSecondary
                )

                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Architecture Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArchiveSurface)
                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOCAL-FIRST ARCHITECTURE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ArchiveTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Visual Archive is fully offline. All original files, downscaled browsing thumbnails, audio voice memos, series, and inspiration studies persist locally inside app-managed storage without requiring cloud sync or third-party servers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveTextMuted
                )
            }

            // Prototype Tools
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArchiveSurface)
                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROTOTYPE DEMO DATA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ArchiveTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If testing without an SD card or camera connection, you can seed sample artistic monochrome & street photography, Saul Leiter reference studies, and default contact sheets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        scope.launch {
                            app.archiveRepository.seedDemoDataIfEmpty()
                            app.inspirationRepository.seedDemoInspirationIfEmpty()
                            app.mixRepository.seedDemoMixIfEmpty()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(containerColor = ArchiveSurfaceElevated),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("seed_demo_data_button")
                ) {
                    Text("Seed Sample Photography Data", color = ArchiveTextPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }

            // App Specs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArchiveSurface)
                    .border(1.dp, ArchiveSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ArchiveTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ArchiveTextSecondary
                    )
                }
                Text("Version: 1.0.0 (Local Archive Prototype)", style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted)
                Text("Database: Room SQLite (Persistent)", style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted)
                Text("EXIF Extraction: ExifInterface (Off-main-thread)", style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted)
                Text("Grid Engine: LazyVerticalGrid + Cached Thumbnails", style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted)
            }
        }
    }
}
