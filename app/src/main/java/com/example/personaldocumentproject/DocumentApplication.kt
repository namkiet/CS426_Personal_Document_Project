package com.example.personaldocumentproject

import android.app.Application
import com.example.personaldocumentproject.di.AppContainer

class DocumentApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}