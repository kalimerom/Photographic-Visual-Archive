package com.example.data.repository

import android.net.Uri
import com.example.data.dao.ContactSheetDao
import com.example.data.dao.PhotoDao
import com.example.data.dao.SelectionEventDao
import com.example.data.dao.SeriesDao
import com.example.data.dao.TagDao
import com.example.data.model.ContactSheetEntity
import com.example.data.model.ContactSheetPhotoEntity
import com.example.data.model.PhotoEntity
import com.example.data.model.PhotoTagCrossRef
import com.example.data.model.SelectionEventEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.SeriesPhotoCrossRef
import com.example.data.model.TagEntity
import com.example.util.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class ArchiveRepository(
    private val photoDao: PhotoDao,
    private val tagDao: TagDao,
    private val seriesDao: SeriesDao,
    private val contactSheetDao: ContactSheetDao,
    private val selectionEventDao: SelectionEventDao,
    private val storageManager: ImageStorageManager
) {
    val allPhotos: Flow<List<PhotoEntity>> = photoDao.getAllPhotos()
    val allSeries: Flow<List<SeriesEntity>> = seriesDao.getAllSeries()
    val allContactSheets: Flow<List<ContactSheetEntity>> = contactSheetDao.getAllContactSheets()
    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()

    fun getPhotoById(id: Long): Flow<PhotoEntity?> = photoDao.getPhotoById(id)
    suspend fun getPhotoByIdSync(id: Long): PhotoEntity? = photoDao.getPhotoByIdSync(id)

    fun getPhotosByIds(ids: List<Long>): Flow<List<PhotoEntity>> = photoDao.getPhotosByIds(ids)
    suspend fun getPhotosByIdsSync(ids: List<Long>): List<PhotoEntity> = photoDao.getPhotosByIdsSync(ids)

    suspend fun importPhotos(uris: List<Pair<Uri, String?>>, onProgress: (Int, Int) -> Unit = { _, _ -> }) = withContext(Dispatchers.IO) {
        val total = uris.size
        uris.forEachIndexed { index, (uri, name) ->
            try {
                val stored = storageManager.importPhotoFromUri(uri, name)
                val entity = PhotoEntity(
                    localPath = stored.originalPath,
                    thumbnailPath = stored.thumbnailPath,
                    originalFileName = stored.fileName,
                    importedAt = System.currentTimeMillis(),
                    captureDate = stored.exif.captureDate,
                    width = stored.width,
                    height = stored.height,
                    cameraMake = stored.exif.cameraMake,
                    cameraModel = stored.exif.cameraModel,
                    lensModel = stored.exif.lensModel,
                    focalLength = stored.exif.focalLength,
                    aperture = stored.exif.aperture,
                    shutterSpeed = stored.exif.shutterSpeed,
                    iso = stored.exif.iso,
                    exposureBias = stored.exif.exposureBias,
                    whiteBalance = stored.exif.whiteBalance,
                    flash = stored.exif.flash,
                    orientation = stored.exif.orientation,
                    gpsLatitude = stored.exif.gpsLatitude,
                    gpsLongitude = stored.exif.gpsLongitude,
                    software = stored.exif.software
                )
                photoDao.insertPhoto(entity)
            } catch (_: Exception) {}
            onProgress(index + 1, total)
        }
    }

    suspend fun attachEditedVersion(photoId: Long, uri: Uri) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoByIdSync(photoId) ?: return@withContext
        val editedPath = storageManager.saveEditedVersion(uri)
        photoDao.updatePhoto(photo.copy(editedVersionPath = editedPath))
    }

    suspend fun removeEditedVersion(photoId: Long) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoByIdSync(photoId) ?: return@withContext
        photo.editedVersionPath?.let { storageManager.deleteFile(it) }
        photoDao.updatePhoto(photo.copy(editedVersionPath = null))
    }

    suspend fun attachVoiceMemo(photoId: Long, filePath: String, durationMs: Long) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoByIdSync(photoId) ?: return@withContext
        photoDao.updatePhoto(
            photo.copy(
                voiceMemoPath = filePath,
                voiceMemoDurationMs = durationMs
            )
        )
    }

    suspend fun deleteVoiceMemo(photoId: Long) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoByIdSync(photoId) ?: return@withContext
        photo.voiceMemoPath?.let { storageManager.deleteFile(it) }
        photoDao.updatePhoto(
            photo.copy(
                voiceMemoPath = null,
                voiceMemoDurationMs = null
            )
        )
    }

    suspend fun setPhotoSelectionStatus(photoId: Long, status: String) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoByIdSync(photoId) ?: return@withContext
        photoDao.updatePhoto(photo.copy(selectionStatus = status))
        selectionEventDao.insertEvent(
            SelectionEventEntity(
                photoId = photoId,
                action = status
            )
        )
    }

    suspend fun deletePhoto(photoId: Long) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoByIdSync(photoId) ?: return@withContext
        storageManager.deleteFile(photo.localPath)
        storageManager.deleteFile(photo.thumbnailPath)
        photo.editedVersionPath?.let { storageManager.deleteFile(it) }
        photo.voiceMemoPath?.let { storageManager.deleteFile(it) }
        photoDao.deletePhotoById(photoId)
    }

    // Series
    fun getSeriesById(seriesId: Long): Flow<SeriesEntity?> = seriesDao.getSeriesById(seriesId)
    fun getPhotosForSeries(seriesId: Long): Flow<List<PhotoEntity>> = seriesDao.getPhotosForSeries(seriesId)

    suspend fun createSeries(title: String, description: String?, photoIds: List<Long>): Long = withContext(Dispatchers.IO) {
        val coverId = photoIds.firstOrNull()
        val series = SeriesEntity(
            title = title,
            description = description,
            coverPhotoId = coverId
        )
        val seriesId = seriesDao.insertSeries(series)
        val crossRefs = photoIds.mapIndexed { idx, pId ->
            SeriesPhotoCrossRef(seriesId = seriesId, photoId = pId, orderIndex = idx)
        }
        seriesDao.addPhotosToSeries(crossRefs)
        seriesId
    }

    suspend fun addPhotosToSeries(seriesId: Long, photoIds: List<Long>) = withContext(Dispatchers.IO) {
        val crossRefs = photoIds.mapIndexed { idx, pId ->
            SeriesPhotoCrossRef(seriesId = seriesId, photoId = pId, orderIndex = idx)
        }
        seriesDao.addPhotosToSeries(crossRefs)
    }

    suspend fun removePhotoFromSeries(seriesId: Long, photoId: Long) = withContext(Dispatchers.IO) {
        seriesDao.removePhotoFromSeries(seriesId, photoId)
    }

    suspend fun deleteSeries(seriesId: Long) = withContext(Dispatchers.IO) {
        seriesDao.deleteSeries(seriesId)
    }

    // Contact Sheets
    fun getContactSheetById(id: Long): Flow<ContactSheetEntity?> = contactSheetDao.getContactSheetById(id)
    fun getContactSheetPhotos(contactSheetId: Long): Flow<List<ContactSheetPhotoEntity>> =
        contactSheetDao.getContactSheetPhotos(contactSheetId)

    suspend fun createContactSheet(title: String, photoIds: List<Long>): Long = withContext(Dispatchers.IO) {
        val sheetId = contactSheetDao.insertContactSheet(
            ContactSheetEntity(title = title)
        )
        val items = photoIds.mapIndexed { index, pId ->
            ContactSheetPhotoEntity(
                contactSheetId = sheetId,
                photoId = pId,
                orderIndex = index,
                status = "normal"
            )
        }
        contactSheetDao.insertContactSheetPhotos(items)
        sheetId
    }

    suspend fun updateContactSheetPhotoStatus(contactSheetPhotoId: Long, status: String) = withContext(Dispatchers.IO) {
        contactSheetDao.updatePhotoStatus(contactSheetPhotoId, status)
    }

    suspend fun deleteContactSheet(id: Long) = withContext(Dispatchers.IO) {
        contactSheetDao.deletePhotosForSheet(id)
        contactSheetDao.deleteContactSheet(id)
    }

    // Tags
    fun getTagsForPhoto(photoId: Long): Flow<List<TagEntity>> = tagDao.getTagsForPhoto(photoId)

    suspend fun addTagToPhoto(photoId: Long, tagName: String) = withContext(Dispatchers.IO) {
        val cleanName = tagName.trim().lowercase()
        if (cleanName.isBlank()) return@withContext
        var tag = tagDao.getTagByName(cleanName)
        val tagId = if (tag == null) {
            tagDao.insertTag(TagEntity(name = cleanName))
        } else {
            tag.id
        }
        tagDao.addTagToPhoto(PhotoTagCrossRef(photoId = photoId, tagId = tagId))
    }

    suspend fun removeTagFromPhoto(photoId: Long, tagId: Long) = withContext(Dispatchers.IO) {
        tagDao.removeTagFromPhoto(photoId, tagId)
    }

    // Global Search
    fun searchPhotos(query: String): Flow<List<PhotoEntity>> = photoDao.searchPhotos(query)
    fun searchSeries(query: String): Flow<List<SeriesEntity>> = seriesDao.searchSeries(query)

    // Demo Data Seeding
    suspend fun seedDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentPhotos = photoDao.getAllPhotos().first()
        if (currentPhotos.isNotEmpty()) return@withContext

        // Create 8 artistic photography samples with realistic EXIF
        val cal = Calendar.getInstance()

        val sampleConfigs = listOf(
            Triple("ARCH-01", "monochrome", Pair("Leica M10-R", "Summicron-M 35mm f/2 ASPH.")),
            Triple("STREET-02", "street", Pair("Fujifilm X-Pro3", "XF 23mm f/1.4 R LM WR")),
            Triple("COLOR-03", "color", Pair("Sony α7 IV", "FE 50mm f/1.2 GM")),
            Triple("SHADOW-04", "monochrome", Pair("Ricoh GR IIIx", "GR Lens 26.1mm f/2.8")),
            Triple("NIGHT-05", "street", Pair("Nikon Zf", "NIKKOR Z 40mm f/2")),
            Triple("SILHOUETTE-06", "monochrome", Pair("Canon EOS R5", "RF 35mm f/1.8 IS Macro STM")),
            Triple("RAIN-07", "color", Pair("Leica Q2", "Summilux 28mm f/1.7 ASPH.")),
            Triple("STUDY-08", "monochrome", Pair("Hasselblad 907X", "XCD 45mm f/4 P"))
        )

        val insertedIds = mutableListOf<Long>()
        sampleConfigs.forEachIndexed { index, (code, style, gear) ->
            cal.add(Calendar.DAY_OF_YEAR, -index)
            val (orig, thumb) = storageManager.createArtisticPlaceholder(code, style)
            val photo = PhotoEntity(
                localPath = orig,
                thumbnailPath = thumb,
                originalFileName = "FRAME_${code.replace("-", "_")}.DNG",
                importedAt = System.currentTimeMillis() - (index * 86400000L),
                captureDate = cal.timeInMillis,
                width = 1200,
                height = 1600,
                cameraMake = gear.first.split(" ").first(),
                cameraModel = gear.first,
                lensModel = gear.second,
                focalLength = when (index % 4) {
                    0 -> "35mm"
                    1 -> "28mm"
                    2 -> "50mm"
                    else -> "40mm"
                },
                aperture = when (index % 4) {
                    0 -> "f/1.8"
                    1 -> "f/2.0"
                    2 -> "f/1.4"
                    else -> "f/2.8"
                },
                shutterSpeed = when (index % 4) {
                    0 -> "1/500s"
                    1 -> "1/250s"
                    2 -> "1/1000s"
                    else -> "1/125s"
                },
                iso = when (index % 4) {
                    0 -> "ISO 400"
                    1 -> "ISO 800"
                    2 -> "ISO 1600"
                    else -> "ISO 200"
                },
                selectionStatus = if (index == 0) "hero" else "none"
            )
            val id = photoDao.insertPhoto(photo)
            insertedIds.add(id)
        }

        // Create a default Series
        if (insertedIds.size >= 4) {
            val seriesId = seriesDao.insertSeries(
                SeriesEntity(
                    title = "Tokyo Monochrome Studies",
                    description = "Night street exploration and high-contrast shadow studies.",
                    coverPhotoId = insertedIds[0]
                )
            )
            seriesDao.addPhotosToSeries(
                insertedIds.take(4).mapIndexed { idx, id ->
                    SeriesPhotoCrossRef(seriesId = seriesId, photoId = id, orderIndex = idx)
                }
            )
        }

        // Create a default Contact Sheet
        if (insertedIds.size >= 6) {
            val sheetId = contactSheetDao.insertContactSheet(
                ContactSheetEntity(title = "Roll #04 - October Walk")
            )
            contactSheetDao.insertContactSheetPhotos(
                insertedIds.take(6).mapIndexed { idx, id ->
                    ContactSheetPhotoEntity(
                        contactSheetId = sheetId,
                        photoId = id,
                        orderIndex = idx,
                        status = if (idx == 0) "hero" else if (idx == 2) "selected" else "normal"
                    )
                }
            )
        }

        // Add some sample tags
        val sampleTags = listOf("street", "reflection", "monochrome", "35mm", "rain", "night")
        sampleTags.forEach { name ->
            val tagId = tagDao.insertTag(TagEntity(name = name))
            if (insertedIds.isNotEmpty()) {
                tagDao.addTagToPhoto(PhotoTagCrossRef(photoId = insertedIds[0], tagId = tagId))
            }
        }
    }
}
