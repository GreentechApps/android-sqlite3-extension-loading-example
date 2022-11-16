package snnafi.sqliteextension.example.repository

import androidx.lifecycle.LiveData
import snnafi.sqliteextension.example.App
import snnafi.sqliteextension.example.model.Hadith
import snnafi.sqliteextension.example.model.Verse

class AppRepository {

    fun getFirstVerse(): LiveData<Verse> {
        return App.quranDatabase.quranDao().getFirstVerse();
    }

    fun getVerses(): LiveData<List<Verse>> {
        return App.quranDatabase.quranDao().getVerses();
    }

    fun getVerses(text: String): LiveData<List<Verse>> {
        return App.quranDatabase.quranDao().getVerses(text);
    }

    fun getHadiths(text: String): LiveData<List<Hadith>> {
        return App.hadithDatabase.hadithDao().getHadiths(text)
    }
}