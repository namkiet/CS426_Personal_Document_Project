package com.example.personaldocumentproject

import android.app.Application
import com.example.personaldocumentproject.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DocumentApplication : Application() {
    lateinit var container: AppContainer
        private set
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            if (BuildConfig.DEBUG) {
                container.seedDebugDocuments()
            }
            container.warmCaches()
        }
    }
}