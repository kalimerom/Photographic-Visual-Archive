package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveDivider
import java.io.File
import kotlin.math.roundToInt

/**
 * True Split Slider with GenericShape clipping.
 * Moving the slider smoothly reveals the Original image on the left
 * and the Edited image on the right.
 * Uses Modifier.offset { IntOffset(...) } to eliminate any negative padding crashes
 * and clamp positions safely across 0f..1f (0% to 100%).
 */
@Composable
fun PreciseSplitSlider(
    originalPath: String,
    editedPath: String,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1f,
    panOffset: Offset = Offset.Zero,
    isZoomLocked: Boolean = true
) {
    val context = LocalContext.current
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    var localScale by remember { mutableFloatStateOf(1f) }
    var localOffset by remember { mutableStateOf(Offset.Zero) }

    val effectiveScale = if (zoomScale != 1f) zoomScale else localScale
    val effectiveOffset = if (panOffset != Offset.Zero) panOffset else localOffset

    val safeFraction = splitFraction.coerceIn(0f, 1f)

    val leftClipShape = remember(safeFraction) {
        GenericShape { size, _ ->
            val w = (size.width * safeFraction).coerceIn(0f, size.width)
            addRect(Rect(0f, 0f, w, size.height))
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(ArchiveBackground)
            .pointerInput(isZoomLocked) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (isZoomLocked) {
                        localScale = (localScale * zoom).coerceIn(1f, 5f)
                        localOffset = if (localScale > 1f) {
                            localOffset + pan
                        } else Offset.Zero
                    }
                }
            }
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val safeTotalWidth = if (totalWidth > 0f) totalWidth else 1f

        // 1. Right Layer (Edited Image)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = effectiveScale
                    scaleY = effectiveScale
                    translationX = effectiveOffset.x
                    translationY = effectiveOffset.y
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(editedPath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Edited Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Left Layer (Original Image clipped to splitFraction)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(leftClipShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = effectiveScale
                        scaleY = effectiveScale
                        translationX = effectiveOffset.x
                        translationY = effectiveOffset.y
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(originalPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Original Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3. Interactive Split Bar (positioned with offset { IntOffset(...) }, NEVER negative padding)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    val dividerXPx = safeTotalWidth * safeFraction
                    val lineXPx = (dividerXPx - 1.dp.toPx()).roundToInt()
                    IntOffset(lineXPx, 0)
                }
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.White)
        )

        // Draggable Handle (positioned with offset { IntOffset(...) }, NEVER negative padding)
        val handleSize = 32.dp
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    val dividerXPx = safeTotalWidth * safeFraction
                    val halfHandlePx = handleSize.toPx() / 2f
                    val clampedHandleX = (dividerXPx - halfHandlePx)
                        .coerceIn(0f, (safeTotalWidth - handleSize.toPx()).coerceAtLeast(0f))
                        .roundToInt()
                    IntOffset(clampedHandleX, 0)
                }
                .size(handleSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, ArchiveBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(1.5.dp, 10.dp).background(ArchiveBackground))
                Box(modifier = Modifier.size(1.5.dp, 10.dp).background(ArchiveBackground))
            }
        }

        // Gesture detector across full area for responsive dragging of slider at 1x
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(safeTotalWidth, effectiveScale) {
                    if (effectiveScale <= 1.05f) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (safeTotalWidth > 0f) {
                                val newFraction = (splitFraction + (dragAmount.x / safeTotalWidth)).coerceIn(0f, 1f)
                                splitFraction = newFraction
                            }
                        }
                    }
                }
        )

        // Dedicated gesture detector on handle / divider column (always allows sliding even when zoomed in)
        val handleTouchWidth = 56.dp
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    val dividerXPx = safeTotalWidth * safeFraction
                    val touchHalf = handleTouchWidth.toPx() / 2f
                    val touchX = (dividerXPx - touchHalf).roundToInt()
                    IntOffset(touchX, 0)
                }
                .width(handleTouchWidth)
                .fillMaxHeight()
                .pointerInput(safeTotalWidth) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (safeTotalWidth > 0f) {
                            val newFraction = (splitFraction + (dragAmount.x / safeTotalWidth)).coerceIn(0f, 1f)
                            splitFraction = newFraction
                        }
                    }
                }
        )

        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ORIGINAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = "EDITED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Side-by-Side Comparison with Synchronized Zoom & Pan (Zoom Lock).
 * When Zoom Lock is active, moving/zooming either side updates both panes identically
 * for micro-detail and sharpness inspection.
 */
@Composable
fun SideBySideComparison(
    imageAPath: String,
    imageBPath: String,
    labelA: String = "ORIGINAL",
    labelB: String = "EDITED",
    isZoomLocked: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sharedScale by remember { mutableFloatStateOf(1f) }
    var sharedOffset by remember { mutableStateOf(Offset.Zero) }

    var scaleB by remember { mutableFloatStateOf(1f) }
    var offsetB by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArchiveBackground)
            .pointerInput(isZoomLocked) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (isZoomLocked) {
                        sharedScale = (sharedScale * zoom).coerceIn(1f, 5f)
                        sharedOffset = if (sharedScale > 1f) {
                            sharedOffset + pan
                        } else Offset.Zero
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left Pane (Image A)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds()
                    .background(ArchiveBackground)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(imageAPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = labelA,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = sharedScale
                            scaleY = sharedScale
                            translationX = sharedOffset.x
                            translationY = sharedOffset.y
                        }
                )
                Text(
                    text = labelA,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Subtle vertical divider line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(ArchiveDivider)
            )

            // Right Pane (Image B)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds()
                    .background(ArchiveBackground)
                    .then(
                        if (!isZoomLocked) {
                            Modifier.pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scaleB = (scaleB * zoom).coerceIn(1f, 5f)
                                    offsetB = if (scaleB > 1f) offsetB + pan else Offset.Zero
                                }
                            }
                        } else Modifier
                    )
            ) {
                val effectiveScale = if (isZoomLocked) sharedScale else scaleB
                val effectiveOffset = if (isZoomLocked) sharedOffset else offsetB

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(imageBPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = labelB,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = effectiveScale
                            scaleY = effectiveScale
                            translationX = effectiveOffset.x
                            translationY = effectiveOffset.y
                        }
                )
                Text(
                    text = labelB,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
