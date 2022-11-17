package snnafi.sqliteextension.example

import android.app.Application

class App : Application() {

    companion object {
        lateinit var singleton: App

        fun getInstance(): App {
            return singleton
        }
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this
    }

}