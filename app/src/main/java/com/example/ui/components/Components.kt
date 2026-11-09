package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PhotoEntity
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveDivider
import com.example.ui.theme.ArchiveHeroRed
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveSurfaceElevated
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Analog Wax-Pencil Hero Marker
 * Implements an organic, slightly imperfect red framing line that replicates
 * the red grease-pencil/wax-pencil markings photographers hand-draw on contact sheets.
 */
@Composable
fun WaxPencilHeroMarker(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val red = ArchiveHeroRed

        // Outer grease pencil line with slight organic hand-drawn deviation
        val path = Path().apply {
            moveTo(6f, 5f)
            lineTo(w - 7f, 6f)
            lineTo(w - 5f, h - 6f)
            lineTo(7f, h - 5f)
            close()
        }

        // Slight second stroke pass for wax thickness
        val secondaryPath = Path().apply {
            moveTo(8f, 7f)
            lineTo(w - 9f, 8f)
            lineTo(w - 7f, h - 8f)
            lineTo(9f, h - 7f)
            close()
        }

        drawPath(
            path = path,
            color = red,
            style = Stroke(
                width = 3.5f,
                pathEffect = PathEffect.cornerPathEffect(4f)
            )
        )
        drawPath(
            path = secondaryPath,
            color = red.copy(alpha = 0.5f),
            style = Stroke(
                width = 1.5f,
                pathEffect = PathEffect.cornerPathEffect(6f)
            )
        )

        // Hand-drawn corner emphasis brackets
        val cornerLen = minOf(w, h) * 0.15f
        drawLine(red, Offset(4f, 5f), Offset(4f + cornerLen, 5f), strokeWidth = 4f)
        drawLine(red, Offset(5f, 4f), Offset(5f, 4f + cornerLen), strokeWidth = 4f)

        drawLine(red, Offset(w - 5f, 5f), Offset(w - 5f - cornerLen, 5f), strokeWidth = 4f)
        drawLine(red, Offset(w - 5f, 4f), Offset(w - 5f, 4f + cornerLen), strokeWidth = 4f)

        drawLine(red, Offset(4f, h - 5f), Offset(4f + cornerLen, h - 5f), strokeWidth = 4f)
        drawLine(red, Offset(5f, h - 4f), Offset(5f, h - 4f - cornerLen), strokeWidth = 4f)

        drawLine(red, Offset(w - 5f, h - 5f), Offset(w - 5f - cornerLen, h - 5f), strokeWidth = 4f)
        drawLine(red, Offset(w - 5f, h - 4f), Offset(w - 5f, h - 4f - cornerLen), strokeWidth = 4f)
    }
}

/**
 * Minimal EXIF summary row: f/1.8   1/500   ISO 800   35mm
 */
@Composable
fun ExifSummaryRow(
    photo: PhotoEntity,
    modifier: Modifier = Modifier
) {
    val items = mutableListOf<String>()
    photo.aperture?.let { items.add(it) }
    photo.shutterSpeed?.let { items.add(it) }
    photo.iso?.let { items.add(it) }
    photo.focalLength?.let { items.add(it) }

    if (items.isNotEmpty()) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = ArchiveTextPrimary
                )
            }
        }
    }
}

/**
 * Full EXIF Bottom Sheet
 * Displays all available extracted EXIF attributes in an editorial, compact layout.
 * Does not show empty fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullExifBottomSheet(
    photo: PhotoEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val exifPairs = remember(photo) {
        val list = mutableListOf<Pair<String, String>>()
        list.add("File Name" to photo.originalFileName)
        if (photo.width > 0 && photo.height > 0) {
            list.add("Dimensions" to "${photo.width} × ${photo.height} px")
        }
        val dateStr = SimpleDateFormat("MMM d, yyyy · HH:mm:ss", Locale.US).format(Date(photo.captureDate))
        list.add("Capture Date" to dateStr)

        photo.cameraMake?.let { list.add("Camera Make" to it) }
        photo.cameraModel?.let { list.add("Camera Model" to it) }
        photo.lensModel?.let { list.add("Lens" to it) }
        photo.focalLength?.let { list.add("Focal Length" to it) }
        photo.aperture?.let { list.add("Aperture" to it) }
        photo.shutterSpeed?.let { list.add("Shutter Speed" to it) }
        photo.iso?.let { list.add("ISO" to it) }
        photo.exposureBias?.let { list.add("Exposure Bias" to it) }
        photo.whiteBalance?.let { list.add("White Balance" to it) }
        photo.flash?.let { list.add("Flash" to it) }
        if (photo.gpsLatitude != null && photo.gpsLongitude != null) {
            list.add("GPS Coordinates" to String.format(Locale.US, "%.5f, %.5f", photo.gpsLatitude, photo.gpsLongitude))
        }
        photo.software?.let { list.add("Software" to it) }
        list
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArchiveSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .background(ArchiveSurfaceBorder, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXIF METADATA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ArchiveTextSecondary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_exif_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ArchiveTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArchiveSurfaceElevated)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                exifPairs.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArchiveTextMuted,
                            modifier = Modifier.width(110.dp)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            ),
                            color = ArchiveTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

