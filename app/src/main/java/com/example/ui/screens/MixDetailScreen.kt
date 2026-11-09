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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SideBySideComparison
import com.example.ui.theme.ArchiveAccentNeutral
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import com.example.ui.viewmodel.MixViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixDetailScreen(
    mixId: Long,
    viewModel: MixViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(mixId) {
        viewModel.loadMixDetail(mixId)
    }

    val resolvedMix by viewModel.currentResolvedMix.collectAsStateWithLifecycle()
    var isZoomLocked by remember { mutableStateOf(true) }

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
                    modifier = Modifier.size(40.dp).testTag("mix_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArchiveTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = resolvedMix?.mix?.title ?: "MIX STUDY",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchiveTextPrimary,
                    maxLines = 1
                )

                IconButton(
                    onClick = { isZoomLocked = !isZoomLocked },
                    modifier = Modifier.size(36.dp).testTag("mix_zoom_lock_toggle")
                ) {
                    Icon(
                        imageVector = if (isZoomLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Zoom Lock",
                        tint = if (isZoomLocked) ArchiveAccentNeutral else ArchiveTextMuted,
                        modifier = Modifier.size(18.dp)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    resolvedMix?.mix?.let { mix ->
                        Text(
                            text = "COMPARISON NOTE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ArchiveTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mix.note ?: "No notes attached to this Mix.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArchiveTextPrimary
                        )
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
            val res = resolvedMix
            if (res?.itemA != null && res.itemB != null) {
                SideBySideComparison(
                    imageAPath = res.itemA.localPath,
                    imageBPath = res.itemB.localPath,
                    labelA = res.itemA.title.uppercase(),
                    labelB = res.itemB.title.uppercase(),
                    isZoomLocked = isZoomLocked,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading paired images...", style = MaterialTheme.typography.bodySmall, color = ArchiveTextMuted)
                }
            }
        }
    }
}
