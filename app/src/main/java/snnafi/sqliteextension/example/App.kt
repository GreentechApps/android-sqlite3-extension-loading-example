package snnafi.sqliteextension.example

import android.app.Application
import snnafi.sqliteextension.example.db.QuranDatabase
import snnafi.sqliteextension.example.repository.OldSchoolRepo


class App : Application() {

    companion object {
        lateinit var singleton: App
        lateinit var oldSchoolWay: OldSchoolRepo
        lateinit var quranDatabase: QuranDatabase
        // Github file limit 100 MB. hadith.db size 134.99 MB
        //   lateinit var hadithDatabase: HadithDatabase

        fun getInstance(): App {
            return singleton
        }
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this
        oldSchoolWay = OldSchoolRepo.getInstance(this)
        quranDatabase = QuranDatabase.getInstance(this);
        //   hadithDatabase = HadithDatabase.getInstance(this);
    }

}