package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["captureDate"]),
        Index(value = ["importedAt"])
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val localPath: String,
    val thumbnailPath: String,
    val originalFileName: String,
    val importedAt: Long = System.currentTimeMillis(),
    val captureDate: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensModel: String? = null,
    val focalLength: String? = null,
    val aperture: String? = null,
    val shutterSpeed: String? = null,
    val iso: String? = null,
    val exposureBias: String? = null,
    val whiteBalance: String? = null,
    val flash: String? = null,
    val orientation: Int = 0,
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val software: String? = null,
    val editedVersionPath: String? = null,
    val voiceMemoPath: String? = null,
    val voiceMemoDurationMs: Long? = null,
    val selectionStatus: String = "none" // "none", "keep", "reject", "hero"
)

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "photo_tags",
    primaryKeys = ["photoId", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class PhotoTagCrossRef(
    val photoId: Long,
    val tagId: Long
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val coverPhotoId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "series_photos",
    primaryKeys = ["seriesId", "photoId"],
    indices = [Index(value = ["photoId"])]
)
data class SeriesPhotoCrossRef(
    val seriesId: Long,
    val photoId: Long,
    val orderIndex: Int = 0
)

@Entity(tableName = "contact_sheets")
data class ContactSheetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contact_sheet_photos",
    indices = [Index(value = ["contactSheetId"]), Index(value = ["photoId"])]
)
data class ContactSheetPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactSheetId: Long,
    val photoId: Long,
    val orderIndex: Int = 0,
    val status: String = "normal" // "normal", "selected", "hero"
)

@Entity(tableName = "inspiration_photographers")
data class InspirationPhotographerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inspiration_projects",
    indices = [Index(value = ["photographerId"])]
)
data class InspirationProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photographerId: Long? = null,
    val title: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inspiration_images",
    indices = [
        Index(value = ["photographerId"]),
        Index(value = ["projectId"])
    ]
)
data class InspirationImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val localPath: String,
    val thumbnailPath: String,
    val photographerName: String? = null,
    val photographerId: Long? = null,
    val projectName: String? = null,
    val projectId: Long? = null,
    val sourceUrl: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inspiration_tags",
    primaryKeys = ["inspirationImageId", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class InspirationTagCrossRef(
    val inspirationImageId: Long,
    val tagId: Long
)

@Entity(tableName = "mixes")
data class MixEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val imageAId: Long,
    val imageAType: String, // "photo" or "inspiration"
    val imageBId: Long,
    val imageBType: String  // "photo" or "inspiration"
)

@Entity(
    tableName = "selection_events",
    indices = [Index(value = ["photoId"])]
)
data class SelectionEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photoId: Long,
    val action: String, // "keep", "reject", "hero", "select"
    val timestamp: Long = System.currentTimeMillis()
)
