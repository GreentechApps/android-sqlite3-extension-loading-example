package snnafi.sqliteextension.example.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import snnafi.sqliteextension.example.model.Verse


@Dao
abstract class QuranDao {

    @Query("SELECT rowid, * FROM VERSES where text MATCH :text")
    abstract fun getVerses(text: String): LiveData<List<Verse>>

    @Query("SELECT rowid, * FROM VERSES where sura = 1 AND ayah = 1")
    abstract fun getFirstVerse(): LiveData<Verse>

    @Query("SELECT rowid, * FROM VERSES")
    abstract fun getVerses(): LiveData<List<Verse>>


}