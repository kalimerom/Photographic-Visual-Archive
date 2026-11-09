package com.example.util

import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class ExtractedExif(
    val width: Int,
    val height: Int,
    val captureDate: Long,
    val cameraMake: String?,
    val cameraModel: String?,
    val lensModel: String?,
    val focalLength: String?,
    val aperture: String?,
    val shutterSpeed: String?,
    val iso: String?,
    val exposureBias: String?,
    val whiteBalance: String?,
    val flash: String?,
    val orientation: Int,
    val gpsLatitude: Double?,
    val gpsLongitude: Double?,
    val software: String?
)

object ExifUtil {

    fun extractExif(file: File): ExtractedExif {
        val exif = try {
            ExifInterface(file.absolutePath)
        } catch (e: Exception) {
            null
        }

        if (exif == null) {
            return ExtractedExif(
                width = 0,
                height = 0,
                captureDate = file.lastModified(),
                cameraMake = null,
                cameraModel = null,
                lensModel = null,
                focalLength = null,
                aperture = null,
                shutterSpeed = null,
                iso = null,
                exposureBias = null,
                whiteBalance = null,
                flash = null,
                orientation = 0,
                gpsLatitude = null,
                gpsLongitude = null,
                software = null
            )
        }

        val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).let {
            if (it == 0) exif.getAttributeInt(ExifInterface.TAG_PIXEL_X_DIMENSION, 0) else it
        }
        val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).let {
            if (it == 0) exif.getAttributeInt(ExifInterface.TAG_PIXEL_Y_DIMENSION, 0) else it
        }
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim()
        val model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim()
        val lens = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)?.trim()
        val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)?.trim()

        // Focal length
        val focalLengthDouble = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
        val focalLength = if (focalLengthDouble > 0.0) {
            if (focalLengthDouble % 1.0 == 0.0) "${focalLengthDouble.toInt()}mm" else String.format(Locale.US, "%.1fmm", focalLengthDouble)
        } else null

        // F-Number (Aperture)
        val apertureDouble = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)
        val aperture = if (apertureDouble > 0.0) {
            String.format(Locale.US, "f/%.1f", apertureDouble)
        } else null

        // Shutter speed (Exposure time)
        val exposureTimeDouble = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, 0.0)
        val shutterSpeed = if (exposureTimeDouble > 0.0) {
            if (exposureTimeDouble < 1.0) {
                val denom = (1.0 / exposureTimeDouble).toInt()
                "1/${denom}s"
            } else {
                "${exposureTimeDouble.toInt()}s"
            }
        } else null

        // ISO
        val isoVal = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0).let {
            if (it == 0) exif.getAttributeInt(ExifInterface.TAG_ISO_SPEED_RATINGS, 0) else it
        }
        val iso = if (isoVal > 0) "ISO $isoVal" else null

        // Exposure Bias
        val bias = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, 0.0)
        val exposureBias = if (bias != 0.0) String.format(Locale.US, "%+.1f EV", bias) else null

        // White balance
        val wb = exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, -1)
        val whiteBalance = when (wb) {
            ExifInterface.WHITE_BALANCE_AUTO.toInt() -> "Auto"
            ExifInterface.WHITE_BALANCE_MANUAL.toInt() -> "Manual"
            else -> null
        }

        // Flash
        val flashVal = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1)
        val flash = when {
            flashVal == -1 -> null
            flashVal and 1 != 0 -> "Fired"
            else -> "Did not fire"
        }

        // Capture Date
        val dateStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
        var captureDate = file.lastModified()
        if (!dateStr.isNullOrBlank()) {
            val formatters = listOf(
                SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                SimpleDateFormat("yyyy:MM:dd", Locale.US)
            )
            for (sdf in formatters) {
                try {
                    sdf.timeZone = TimeZone.getDefault()
                    val parsed = sdf.parse(dateStr)
                    if (parsed != null) {
                        captureDate = parsed.time
                        break
                    }
                } catch (_: Exception) {}
            }
        }

        // Lat/Lng
        val latLong = FloatArray(2)
        val hasGps = exif.getLatLong(latLong)
        val gpsLat = if (hasGps) latLong[0].toDouble() else null
        val gpsLng = if (hasGps) latLong[1].toDouble() else null

        return ExtractedExif(
            width = width,
            height = height,
            captureDate = captureDate,
            cameraMake = make,
            cameraModel = model,
            lensModel = lens,
            focalLength = focalLength,
            aperture = aperture,
            shutterSpeed = shutterSpeed,
            iso = iso,
            exposureBias = exposureBias,
            whiteBalance = whiteBalance,
            flash = flash,
            orientation = orientation,
            gpsLatitude = gpsLat,
            gpsLongitude = gpsLng,
            software = software
        )
    }
}
