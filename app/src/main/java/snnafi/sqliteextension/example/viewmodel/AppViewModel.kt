package snnafi.sqliteextension.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import snnafi.sqliteextension.example.model.Verse
import snnafi.sqliteextension.example.repository.AppRepository

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var repository: AppRepository

    init {
        repository = AppRepository.getInstance(application)
    }

    fun getVerses(text: String): List<Verse> {
        return repository.getVerses(text);
    }
}