package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.repository.ChatRepository
import com.example.data.repository.PreferencesRepository

class NovaApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var geminiClient: GeminiClient
        private set

    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nova_ai.db"
        ).fallbackToDestructiveMigration().build()

        preferencesRepository = PreferencesRepository(applicationContext)
        geminiClient = GeminiClient(preferencesRepository)
        chatRepository = ChatRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao(),
            userAccountDao = database.userAccountDao()
        )
    }

    companion object {
        lateinit var instance: NovaApplication
            private set
    }
}
