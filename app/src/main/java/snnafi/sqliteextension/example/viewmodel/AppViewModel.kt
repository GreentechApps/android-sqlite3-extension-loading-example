package snnafi.sqliteextension.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import snnafi.sqliteextension.example.model.Hadith
import snnafi.sqliteextension.example.model.Verse
import snnafi.sqliteextension.example.repository.AppRepository

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository()

    fun getFirstVerse(): LiveData<Verse> {
        return repository.getFirstVerse();
    }

    fun getVerses(): LiveData<List<Verse>> {
        return repository.getVerses();
    }

    fun getVerses(text: String): LiveData<List<Verse>> {
        return repository.getVerses(text);
    }

    fun getHadiths(text: String): LiveData<List<Hadith>> {
        return repository.getHadiths(text)
    }
}