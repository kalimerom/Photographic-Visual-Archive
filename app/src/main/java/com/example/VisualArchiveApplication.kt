package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.ArchiveRepository
import com.example.data.repository.InspirationRepository
import com.example.data.repository.MixRepository
import com.example.util.AudioRecorderManager
import com.example.util.ImageStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VisualArchiveApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getInstance(this) }
    val storageManager by lazy { ImageStorageManager(this) }
    val audioRecorderManager by lazy { AudioRecorderManager(this) }

    val archiveRepository by lazy {
        ArchiveRepository(
            photoDao = database.photoDao(),
            tagDao = database.tagDao(),
            seriesDao = database.seriesDao(),
            contactSheetDao = database.contactSheetDao(),
            selectionEventDao = database.selectionEventDao(),
            storageManager = storageManager
        )
    }

    val inspirationRepository by lazy {
        InspirationRepository(
            inspirationDao = database.inspirationDao(),
            tagDao = database.tagDao(),
            storageManager = storageManager
        )
    }

    val mixRepository by lazy {
        MixRepository(
            mixDao = database.mixDao(),
            photoDao = database.photoDao(),
            inspirationDao = database.inspirationDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize demo dataset if database is fresh, providing an immediate working archive
        applicationScope.launch {
            try {
                archiveRepository.seedDemoDataIfEmpty()
                inspirationRepository.seedDemoInspirationIfEmpty()
                mixRepository.seedDemoMixIfEmpty()
            } catch (_: Exception) {}
        }
    }
}
