package snnafi.sqliteextension.example.repository

import android.content.Context
import android.database.Cursor
import android.util.Log
import snnafi.sqliteextension.example.db.DatabaseOpenHelper
import snnafi.sqliteextension.example.model.Verse

class AppRepository() {
    private lateinit var helper: DatabaseOpenHelper

    fun init(context: Context) {
        helper = DatabaseOpenHelper(context)
        try {
            helper.createDataBase()
            helper.openDataBase()

        } catch (e: Exception) {
            Log.d("AppRepository", e.localizedMessage ?: e.toString())
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context).also { INSTANCE = it }
        }

        fun build(context: Context): AppRepository {
            val oldSchoolRepo = AppRepository();
            oldSchoolRepo.init(context)
            return oldSchoolRepo
        }

    }

    fun getVerses(text: String): List<Verse> {
        val verses = ArrayList<Verse>()
        val database = helper.readableDatabase;
        val query = "SELECT rowid, * FROM verses where text MATCH '$text'"
        val cursor: Cursor = database.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val verse =
                    Verse(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3))
                verses.add(verse)
            } while (cursor.moveToNext());
        }

        return verses;
    }

}