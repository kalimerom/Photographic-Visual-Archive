package com.example.data.repository

import android.net.Uri
import com.example.data.dao.InspirationDao
import com.example.data.dao.TagDao
import com.example.data.model.InspirationImageEntity
import com.example.data.model.InspirationPhotographerEntity
import com.example.data.model.InspirationProjectEntity
import com.example.data.model.InspirationTagCrossRef
import com.example.data.model.TagEntity
import com.example.util.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class InspirationRepository(
    private val inspirationDao: InspirationDao,
    private val tagDao: TagDao,
    private val storageManager: ImageStorageManager
) {
    val allImages: Flow<List<InspirationImageEntity>> = inspirationDao.getAllInspirationImages()
    val allPhotographers: Flow<List<InspirationPhotographerEntity>> = inspirationDao.getAllPhotographers()
    val allProjects: Flow<List<InspirationProjectEntity>> = inspirationDao.getAllProjects()

    fun getImageById(id: Long): Flow<InspirationImageEntity?> = inspirationDao.getInspirationImageById(id)
    suspend fun getImageByIdSync(id: Long): InspirationImageEntity? = inspirationDao.getInspirationImageByIdSync(id)

    suspend fun addInspirationImage(
        uri: Uri?,
        photographerName: String?,
        projectName: String?,
        sourceUrl: String?,
        note: String?,
        tags: List<String>
    ): Long = withContext(Dispatchers.IO) {
        val (origPath, thumbPath) = if (uri != null) {
            storageManager.saveInspirationImage(uri)
        } else {
            // Note: user can save a reference record with sourceUrl / note even without an image file
            Pair("", "")
        }

        val entity = InspirationImageEntity(
            localPath = origPath,
            thumbnailPath = thumbPath,
            photographerName = photographerName?.trim()?.ifBlank { null },
            projectName = projectName?.trim()?.ifBlank { null },
            sourceUrl = sourceUrl?.trim()?.ifBlank { null },
            note = note?.trim()?.ifBlank { null }
        )
        val imageId = inspirationDao.insertInspirationImage(entity)

        // Ensure photographer exists
        photographerName?.trim()?.takeIf { it.isNotBlank() }?.let { pName ->
            val existing = inspirationDao.searchPhotographers(pName).first()
            if (existing.none { it.name.equals(pName, ignoreCase = true) }) {
                inspirationDao.insertPhotographer(InspirationPhotographerEntity(name = pName))
            }
        }

        // Add tags
        tags.forEach { tagStr ->
            val clean = tagStr.trim().lowercase()
            if (clean.isNotBlank()) {
                var tag = tagDao.getTagByName(clean)
                val tagId = if (tag == null) {
                    tagDao.insertTag(TagEntity(name = clean))
                } else {
                    tag.id
                }
                tagDao.addTagToInspirationImage(InspirationTagCrossRef(inspirationImageId = imageId, tagId = tagId))
            }
        }

        imageId
    }

    suspend fun addPhotographer(name: String, notes: String?): Long = withContext(Dispatchers.IO) {
        inspirationDao.insertPhotographer(
            InspirationPhotographerEntity(name = name.trim(), notes = notes?.trim()?.ifBlank { null })
        )
    }

    suspend fun addProject(title: String, photographerId: Long?, notes: String?): Long = withContext(Dispatchers.IO) {
        inspirationDao.insertProject(
            InspirationProjectEntity(title = title.trim(), photographerId = photographerId, notes = notes?.trim()?.ifBlank { null })
        )
    }

    suspend fun deleteInspirationImage(id: Long) = withContext(Dispatchers.IO) {
        val img = inspirationDao.getInspirationImageByIdSync(id)
        if (img != null) {
            if (img.localPath.isNotBlank()) storageManager.deleteFile(img.localPath)
            if (img.thumbnailPath.isNotBlank()) storageManager.deleteFile(img.thumbnailPath)
            inspirationDao.deleteInspirationImage(id)
        }
    }

    fun getTagsForInspirationImage(imageId: Long): Flow<List<TagEntity>> =
        tagDao.getTagsForInspirationImage(imageId)

    fun searchImages(query: String): Flow<List<InspirationImageEntity>> = inspirationDao.searchInspirationImages(query)
    fun searchPhotographers(query: String): Flow<List<InspirationPhotographerEntity>> = inspirationDao.searchPhotographers(query)
    fun searchProjects(query: String): Flow<List<InspirationProjectEntity>> = inspirationDao.searchProjects(query)

    suspend fun seedDemoInspirationIfEmpty() = withContext(Dispatchers.IO) {
        val current = inspirationDao.getAllInspirationImages().first()
        if (current.isNotEmpty()) return@withContext

        // Create iconic Saul Leiter / Early Color study
        val (orig, thumb) = storageManager.createInspirationPlaceholder("Saul Leiter", "Early Color", "reflection")

        val pId = inspirationDao.insertPhotographer(
            InspirationPhotographerEntity(
                name = "Saul Leiter",
                notes = "Pioneer of early color street photography. Master of reflections, steam, condensed framing, and painterly red accents."
            )
        )

        val prId = inspirationDao.insertProject(
            InspirationProjectEntity(
                photographerId = pId,
                title = "Early Color",
                notes = "New York 1950s. Kodachrome film, umbrellas, raindrops on coffee shop windows."
            )
        )

        val imgId = inspirationDao.insertInspirationImage(
            InspirationImageEntity(
                localPath = orig,
                thumbnailPath = thumb,
                photographerName = "Saul Leiter",
                photographerId = pId,
                projectName = "Early Color",
                projectId = prId,
                sourceUrl = "https://www.saulleiterfoundation.org",
                note = "Study the layering of the red neon through the condensation. The focal point is deliberately obscured by foreground elements."
            )
        )

        val sampleTags = listOf("color", "reflection", "street", "layering", "rain")
        sampleTags.forEach { tagName ->
            val tag = tagDao.getTagByName(tagName)
            val tagId = tag?.id ?: tagDao.insertTag(TagEntity(name = tagName))
            tagDao.addTagToInspirationImage(InspirationTagCrossRef(inspirationImageId = imgId, tagId = tagId))
        }

        // Add second photographer reference: Fan Ho
        inspirationDao.insertPhotographer(
            InspirationPhotographerEntity(
                name = "Fan Ho",
                notes = "Hong Kong street photographer renowned for dramatic cinematic light, geometrical shadows, and smoke."
            )
        )
    }
}
