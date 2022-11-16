package snnafi.sqliteextension.example.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import snnafi.sqliteextension.example.model.Hadith


@Dao
abstract class HadithDao {

    @Query("SELECT * FROM Hadith where content MATCH :text")
    abstract fun getHadiths(text: String): LiveData<List<Hadith>>

}