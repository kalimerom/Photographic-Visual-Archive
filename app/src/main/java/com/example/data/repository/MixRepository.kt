package com.example.data.repository

import com.example.data.dao.InspirationDao
import com.example.data.dao.MixDao
import com.example.data.dao.PhotoDao
import com.example.data.model.MixEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class ResolvedMixItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val localPath: String,
    val thumbnailPath: String,
    val type: String // "photo" or "inspiration"
)

data class ResolvedMix(
    val mix: MixEntity,
    val itemA: ResolvedMixItem?,
    val itemB: ResolvedMixItem?
)

class MixRepository(
    private val mixDao: MixDao,
    private val photoDao: PhotoDao,
    private val inspirationDao: InspirationDao
) {
    val allMixes: Flow<List<MixEntity>> = mixDao.getAllMixes()

    fun getMixById(id: Long): Flow<MixEntity?> = mixDao.getMixById(id)

    suspend fun resolveMix(mix: MixEntity): ResolvedMix = withContext(Dispatchers.IO) {
        val itemA = resolveItem(mix.imageAId, mix.imageAType)
        val itemB = resolveItem(mix.imageBId, mix.imageBType)
        ResolvedMix(mix = mix, itemA = itemA, itemB = itemB)
    }

    private suspend fun resolveItem(id: Long, type: String): ResolvedMixItem? {
        return if (type == "photo") {
            val p = photoDao.getPhotoByIdSync(id)
            if (p != null) {
                ResolvedMixItem(
                    id = p.id,
                    title = p.originalFileName,
                    subtitle = listOfNotNull(p.focalLength, p.aperture, p.cameraModel).joinToString(" · "),
                    localPath = p.localPath,
                    thumbnailPath = p.thumbnailPath,
                    type = "photo"
                )
            } else null
        } else {
            val insp = inspirationDao.getInspirationImageByIdSync(id)
            if (insp != null) {
                ResolvedMixItem(
                    id = insp.id,
                    title = insp.photographerName ?: "Inspiration",
                    subtitle = insp.projectName ?: "Reference",
                    localPath = insp.localPath,
                    thumbnailPath = insp.thumbnailPath,
                    type = "inspiration"
                )
            } else null
        }
    }

    suspend fun createMix(
        title: String,
        note: String?,
        imageAId: Long,
        imageAType: String,
        imageBId: Long,
        imageBType: String
    ): Long = withContext(Dispatchers.IO) {
        mixDao.insertMix(
            MixEntity(
                title = title.trim(),
                note = note?.trim()?.ifBlank { null },
                imageAId = imageAId,
                imageAType = imageAType,
                imageBId = imageBId,
                imageBType = imageBType
            )
        )
    }

    suspend fun updateMix(mix: MixEntity) = withContext(Dispatchers.IO) {
        mixDao.updateMix(mix)
    }

    suspend fun deleteMix(id: Long) = withContext(Dispatchers.IO) {
        mixDao.deleteMix(id)
    }

    fun searchMixes(query: String): Flow<List<MixEntity>> = mixDao.searchMixes(query)

    suspend fun seedDemoMixIfEmpty() = withContext(Dispatchers.IO) {
        val current = mixDao.getAllMixes().first()
        if (current.isNotEmpty()) return@withContext

        val photos = photoDao.getAllPhotos().first()
        val inspirations = inspirationDao.getAllInspirationImages().first()

        if (photos.isNotEmpty() && inspirations.isNotEmpty()) {
            mixDao.insertMix(
                MixEntity(
                    title = "Street Reflection Study",
                    note = "Comparing color saturation and edge compression between my 35mm frame and Saul Leiter's Early Color aesthetic.",
                    imageAId = photos[0].id,
                    imageAType = "photo",
                    imageBId = inspirations[0].id,
                    imageBType = "inspiration"
                )
            )
        }
        if (photos.size >= 2) {
            mixDao.insertMix(
                MixEntity(
                    title = "Aperture & DOF Pairing",
                    note = "High contrast silhouette vs shallow depth of field study.",
                    imageAId = photos[0].id,
                    imageAType = "photo",
                    imageBId = photos[1].id,
                    imageBType = "photo"
                )
            )
        }
    }
}
