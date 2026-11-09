package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactSheetEntity
import com.example.data.model.ContactSheetPhotoEntity
import com.example.data.model.InspirationImageEntity
import com.example.data.model.InspirationPhotographerEntity
import com.example.data.model.InspirationProjectEntity
import com.example.data.model.InspirationTagCrossRef
import com.example.data.model.MixEntity
import com.example.data.model.PhotoEntity
import com.example.data.model.PhotoTagCrossRef
import com.example.data.model.SelectionEventEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.SeriesPhotoCrossRef
import com.example.data.model.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY captureDate DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    fun getPhotoById(id: Long): Flow<PhotoEntity?>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoByIdSync(id: Long): PhotoEntity?

    @Query("SELECT * FROM photos WHERE id IN (:ids)")
    fun getPhotosByIds(ids: List<Long>): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id IN (:ids)")
    suspend fun getPhotosByIdsSync(ids: List<Long>): List<PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>): List<Long>

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)

    @Query("""
        SELECT * FROM photos 
        WHERE originalFileName LIKE '%' || :query || '%'
           OR cameraMake LIKE '%' || :query || '%'
           OR cameraModel LIKE '%' || :query || '%'
           OR lensModel LIKE '%' || :query || '%'
           OR focalLength LIKE '%' || :query || '%'
           OR aperture LIKE '%' || :query || '%'
           OR shutterSpeed LIKE '%' || :query || '%'
           OR iso LIKE '%' || :query || '%'
        ORDER BY captureDate DESC
    """)
    fun searchPhotos(query: String): Flow<List<PhotoEntity>>

    @Query("SELECT COUNT(*) FROM photos")
    fun getPhotoCount(): Flow<Int>
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToPhoto(crossRef: PhotoTagCrossRef)

    @Query("DELETE FROM photo_tags WHERE photoId = :photoId AND tagId = :tagId")
    suspend fun removeTagFromPhoto(photoId: Long, tagId: Long)

    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN photo_tags ON tags.id = photo_tags.tagId 
        WHERE photo_tags.photoId = :photoId
    """)
    fun getTagsForPhoto(photoId: Long): Flow<List<TagEntity>>

    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN inspiration_tags ON tags.id = inspiration_tags.tagId 
        WHERE inspiration_tags.inspirationImageId = :imageId
    """)
    fun getTagsForInspirationImage(imageId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToInspirationImage(crossRef: InspirationTagCrossRef)
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY createdAt DESC")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :id LIMIT 1")
    fun getSeriesById(id: Long): Flow<SeriesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity): Long

    @Update
    suspend fun updateSeries(series: SeriesEntity)

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteSeries(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPhotosToSeries(crossRefs: List<SeriesPhotoCrossRef>)

    @Query("DELETE FROM series_photos WHERE seriesId = :seriesId AND photoId = :photoId")
    suspend fun removePhotoFromSeries(seriesId: Long, photoId: Long)

    @Query("""
        SELECT photos.* FROM photos 
        INNER JOIN series_photos ON photos.id = series_photos.photoId 
        WHERE series_photos.seriesId = :seriesId 
        ORDER BY series_photos.orderIndex ASC
    """)
    fun getPhotosForSeries(seriesId: Long): Flow<List<PhotoEntity>>

    @Query("""
        SELECT series.* FROM series 
        INNER JOIN series_photos ON series.id = series_photos.seriesId 
        WHERE series_photos.photoId = :photoId
    """)
    fun getSeriesForPhoto(photoId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT COUNT(*) FROM series_photos WHERE seriesId = :seriesId")
    fun getSeriesPhotoCount(seriesId: Long): Flow<Int>

    @Query("SELECT * FROM series WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchSeries(query: String): Flow<List<SeriesEntity>>
}

@Dao
interface ContactSheetDao {
    @Query("SELECT * FROM contact_sheets ORDER BY createdAt DESC")
    fun getAllContactSheets(): Flow<List<ContactSheetEntity>>

    @Query("SELECT * FROM contact_sheets WHERE id = :id LIMIT 1")
    fun getContactSheetById(id: Long): Flow<ContactSheetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactSheet(sheet: ContactSheetEntity): Long

    @Query("DELETE FROM contact_sheets WHERE id = :id")
    suspend fun deleteContactSheet(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactSheetPhotos(photos: List<ContactSheetPhotoEntity>)

    @Query("SELECT * FROM contact_sheet_photos WHERE contactSheetId = :contactSheetId ORDER BY orderIndex ASC")
    fun getContactSheetPhotos(contactSheetId: Long): Flow<List<ContactSheetPhotoEntity>>

    @Query("UPDATE contact_sheet_photos SET status = :status WHERE id = :id")
    suspend fun updatePhotoStatus(id: Long, status: String)

    @Query("DELETE FROM contact_sheet_photos WHERE contactSheetId = :contactSheetId")
    suspend fun deletePhotosForSheet(contactSheetId: Long)
}

@Dao
interface InspirationDao {
    @Query("SELECT * FROM inspiration_images ORDER BY createdAt DESC")
    fun getAllInspirationImages(): Flow<List<InspirationImageEntity>>

    @Query("SELECT * FROM inspiration_images WHERE id = :id LIMIT 1")
    fun getInspirationImageById(id: Long): Flow<InspirationImageEntity?>

    @Query("SELECT * FROM inspiration_images WHERE id = :id LIMIT 1")
    suspend fun getInspirationImageByIdSync(id: Long): InspirationImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspirationImage(image: InspirationImageEntity): Long

    @Update
    suspend fun updateInspirationImage(image: InspirationImageEntity)

    @Query("DELETE FROM inspiration_images WHERE id = :id")
    suspend fun deleteInspirationImage(id: Long)

    @Query("SELECT * FROM inspiration_photographers ORDER BY name ASC")
    fun getAllPhotographers(): Flow<List<InspirationPhotographerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotographer(photographer: InspirationPhotographerEntity): Long

    @Query("SELECT * FROM inspiration_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<InspirationProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: InspirationProjectEntity): Long

    @Query("""
        SELECT * FROM inspiration_images 
        WHERE photographerName LIKE '%' || :query || '%'
           OR projectName LIKE '%' || :query || '%'
           OR note LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchInspirationImages(query: String): Flow<List<InspirationImageEntity>>

    @Query("""
        SELECT * FROM inspiration_photographers 
        WHERE name LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
    """)
    fun searchPhotographers(query: String): Flow<List<InspirationPhotographerEntity>>

    @Query("""
        SELECT * FROM inspiration_projects 
        WHERE title LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
    """)
    fun searchProjects(query: String): Flow<List<InspirationProjectEntity>>
}

@Dao
interface MixDao {
    @Query("SELECT * FROM mixes ORDER BY createdAt DESC")
    fun getAllMixes(): Flow<List<MixEntity>>

    @Query("SELECT * FROM mixes WHERE id = :id LIMIT 1")
    fun getMixById(id: Long): Flow<MixEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMix(mix: MixEntity): Long

    @Update
    suspend fun updateMix(mix: MixEntity)

    @Query("DELETE FROM mixes WHERE id = :id")
    suspend fun deleteMix(id: Long)

    @Query("SELECT * FROM mixes WHERE title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun searchMixes(query: String): Flow<List<MixEntity>>
}

@Dao
interface SelectionEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SelectionEventEntity)

    @Query("SELECT * FROM selection_events WHERE photoId = :photoId ORDER BY timestamp DESC")
    fun getEventsForPhoto(photoId: Long): Flow<List<SelectionEventEntity>>

    @Query("SELECT COUNT(*) FROM selection_events WHERE action = :action")
    fun countByAction(action: String): Flow<Int>
}
