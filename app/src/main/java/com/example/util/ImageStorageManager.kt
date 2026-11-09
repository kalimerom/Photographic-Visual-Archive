package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class StoredImageResult(
    val originalPath: String,
    val thumbnailPath: String,
    val fileName: String,
    val width: Int,
    val height: Int,
    val exif: ExtractedExif
)

class ImageStorageManager(private val context: Context) {

    private val photosDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
    private val thumbnailsDir = File(context.filesDir, "thumbnails").apply { if (!exists()) mkdirs() }
    private val editsDir = File(context.filesDir, "edits").apply { if (!exists()) mkdirs() }
    private val inspirationDir = File(context.filesDir, "inspiration").apply { if (!exists()) mkdirs() }
    private val audioDir = File(context.filesDir, "audio").apply { if (!exists()) mkdirs() }

    fun getAudioDir(): File = audioDir

    suspend fun importPhotoFromUri(uri: Uri, displayName: String? = null): StoredImageResult = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val originalFile = File(photosDir, "$id.jpg")

        // Copy input stream to originalFile
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(originalFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open input stream for $uri")

        val fileName = displayName ?: originalFile.name
        val exif = ExifUtil.extractExif(originalFile)

        // Generate downscaled thumbnail
        val thumbnailFile = File(thumbnailsDir, "${id}_thumb.jpg")
        generateThumbnail(originalFile, thumbnailFile, maxDimension = 480, orientation = exif.orientation)

        // Decode bounds if exif had 0 width/height
        val finalWidth = if (exif.width > 0) exif.width else decodeBounds(originalFile).first
        val finalHeight = if (exif.height > 0) exif.height else decodeBounds(originalFile).second

        StoredImageResult(
            originalPath = originalFile.absolutePath,
            thumbnailPath = thumbnailFile.absolutePath,
            fileName = fileName,
            width = finalWidth,
            height = finalHeight,
            exif = exif.copy(width = finalWidth, height = finalHeight)
        )
    }

    suspend fun saveEditedVersion(uri: Uri): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val editedFile = File(editsDir, "${id}_edited.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(editedFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not copy edited image")
        editedFile.absolutePath
    }

    suspend fun saveInspirationImage(uri: Uri): Pair<String, String> = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val originalFile = File(inspirationDir, "${id}_insp.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(originalFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not copy inspiration image")

        val thumbFile = File(thumbnailsDir, "${id}_insp_thumb.jpg")
        generateThumbnail(originalFile, thumbFile, maxDimension = 480, orientation = 0)
        Pair(originalFile.absolutePath, thumbFile.absolutePath)
    }

    private fun decodeBounds(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Pair(options.outWidth, options.outHeight)
    }

    private fun generateThumbnail(sourceFile: File, destFile: File, maxDimension: Int, orientation: Int) {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(sourceFile.absolutePath, boundsOptions)

        val srcWidth = boundsOptions.outWidth
        val srcHeight = boundsOptions.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) return

        var sampleSize = 1
        var halfWidth = srcWidth
        var halfHeight = srcHeight
        while (halfWidth > maxDimension * 2 && halfHeight > maxDimension * 2) {
            sampleSize *= 2
            halfWidth /= 2
            halfHeight /= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565 // Memory-friendly for fast grid browsing
        }

        val sampledBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions) ?: return

        // Scale to max dimension
        val maxSrc = maxOf(sampledBitmap.width, sampledBitmap.height)
        val scale = if (maxSrc > maxDimension) maxDimension.toFloat() / maxSrc else 1f

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        // Apply EXIF orientation if needed
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        }

        val finalThumb = Bitmap.createBitmap(
            sampledBitmap, 0, 0,
            sampledBitmap.width, sampledBitmap.height,
            matrix, true
        )

        FileOutputStream(destFile).use { out ->
            finalThumb.compress(Bitmap.CompressFormat.JPEG, 82, out)
        }

        if (finalThumb != sampledBitmap) {
            finalThumb.recycle()
        }
        sampledBitmap.recycle()
    }

    suspend fun createArtisticPlaceholder(
        title: String,
        style: String, // "monochrome", "street", "noir", "architecture", "color"
        width: Int = 1200,
        height: Int = 1600
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val origFile = File(photosDir, "sample_$id.jpg")
        val thumbFile = File(thumbnailsDir, "sample_${id}_thumb.jpg")

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (style) {
            "monochrome" -> {
                val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(),
                    intArrayOf(AndroidColor.rgb(20, 20, 22), AndroidColor.rgb(55, 58, 62), AndroidColor.rgb(10, 10, 12)),
                    null, Shader.TileMode.CLAMP)
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
                // Draw architectural silhouette
                paint.color = AndroidColor.rgb(15, 15, 17)
                canvas.drawRect(width * 0.15f, height * 0.35f, width * 0.85f, height.toFloat(), paint)
                paint.color = AndroidColor.rgb(220, 222, 225)
                paint.strokeWidth = 2f
                paint.style = Paint.Style.STROKE
                canvas.drawRect(width * 0.25f, height * 0.45f, width * 0.75f, height * 0.85f, paint)
                paint.style = Paint.Style.FILL
            }
            "street" -> {
                val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(AndroidColor.rgb(24, 18, 14), AndroidColor.rgb(65, 48, 38), AndroidColor.rgb(12, 10, 8)),
                    null, Shader.TileMode.CLAMP)
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
                // Street reflection / light streaks
                paint.color = AndroidColor.argb(140, 230, 170, 90)
                canvas.drawCircle(width * 0.45f, height * 0.4f, width * 0.22f, paint)
                paint.color = AndroidColor.argb(160, 210, 80, 50)
                canvas.drawRect(width * 0.2f, height * 0.65f, width * 0.8f, height * 0.68f, paint)
            }
            "color" -> {
                val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(AndroidColor.rgb(18, 28, 38), AndroidColor.rgb(40, 55, 68), AndroidColor.rgb(12, 15, 20)),
                    null, Shader.TileMode.CLAMP)
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
                // Subtle color pop
                paint.color = AndroidColor.argb(180, 220, 70, 60)
                canvas.drawCircle(width * 0.55f, height * 0.52f, width * 0.16f, paint)
                paint.color = AndroidColor.argb(100, 250, 240, 220)
                canvas.drawRect(width * 0.1f, height * 0.2f, width * 0.9f, height * 0.25f, paint)
            }
            else -> {
                val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(),
                    intArrayOf(AndroidColor.rgb(25, 25, 28), AndroidColor.rgb(42, 44, 48), AndroidColor.rgb(18, 18, 20)),
                    null, Shader.TileMode.CLAMP)
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
                paint.color = AndroidColor.argb(120, 200, 200, 200)
                canvas.drawCircle(width * 0.5f, height * 0.5f, width * 0.25f, paint)
            }
        }

        // Minimalist label imprint on photo corner
        paint.color = AndroidColor.argb(120, 240, 240, 240)
        paint.textSize = 36f
        paint.isFakeBoldText = true
        canvas.drawText(title.uppercase(), width * 0.08f, height * 0.92f, paint)

        FileOutputStream(origFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val thumbBitmap = Bitmap.createScaledBitmap(bitmap, 360, 480, true)
        FileOutputStream(thumbFile).use { out ->
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
        }
        thumbBitmap.recycle()
        bitmap.recycle()

        Pair(origFile.absolutePath, thumbFile.absolutePath)
    }

    suspend fun createInspirationPlaceholder(
        photographer: String,
        project: String,
        style: String
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val origFile = File(inspirationDir, "insp_$id.jpg")
        val thumbFile = File(thumbnailsDir, "insp_${id}_thumb.jpg")

        val width = 1200
        val height = 1500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Editorial warm/muted tones characteristic of Saul Leiter / Vivian Maier
        val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(AndroidColor.rgb(38, 28, 22), AndroidColor.rgb(68, 48, 38), AndroidColor.rgb(20, 16, 14)),
            null, Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Composition geometry
        paint.color = AndroidColor.argb(200, 185, 45, 35) // Iconic red reflection
        canvas.drawRect(width * 0.3f, height * 0.25f, width * 0.7f, height * 0.75f, paint)

        paint.color = AndroidColor.argb(140, 245, 230, 200)
        paint.textSize = 36f
        canvas.drawText("$photographer — $project", width * 0.08f, height * 0.88f, paint)

        FileOutputStream(origFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val thumbBitmap = Bitmap.createScaledBitmap(bitmap, 360, 450, true)
        FileOutputStream(thumbFile).use { out ->
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
        }
        thumbBitmap.recycle()
        bitmap.recycle()

        Pair(origFile.absolutePath, thumbFile.absolutePath)
    }

    fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }
}
