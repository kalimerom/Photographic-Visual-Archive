package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ContactSheetDao
import com.example.data.dao.InspirationDao
import com.example.data.dao.MixDao
import com.example.data.dao.PhotoDao
import com.example.data.dao.SelectionEventDao
import com.example.data.dao.SeriesDao
import com.example.data.dao.TagDao
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

@Database(
    entities = [
        PhotoEntity::class,
        TagEntity::class,
        PhotoTagCrossRef::class,
        SeriesEntity::class,
        SeriesPhotoCrossRef::class,
        ContactSheetEntity::class,
        ContactSheetPhotoEntity::class,
        InspirationPhotographerEntity::class,
        InspirationProjectEntity::class,
        InspirationImageEntity::class,
        InspirationTagCrossRef::class,
        MixEntity::class,
        SelectionEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun tagDao(): TagDao
    abstract fun seriesDao(): SeriesDao
    abstract fun contactSheetDao(): ContactSheetDao
    abstract fun inspirationDao(): InspirationDao
    abstract fun mixDao(): MixDao
    abstract fun selectionEventDao(): SelectionEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "visual_archive.db"
                ).fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
