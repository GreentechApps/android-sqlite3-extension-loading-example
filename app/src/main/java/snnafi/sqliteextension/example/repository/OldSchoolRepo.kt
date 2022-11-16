package snnafi.sqliteextension.example.repository

import android.content.Context
import android.database.Cursor
import android.util.Log
import snnafi.sqliteextension.example.db.OldSchoolDatabase
import snnafi.sqliteextension.example.model.Verse

class OldSchoolRepo() {
    private lateinit var db: OldSchoolDatabase

    fun init(context: Context) {
        db = OldSchoolDatabase(context)
        try {
            db.createDataBase()
            db.openDataBase()

        } catch (e: Exception) {
            Log.d("OldSchoolRepo", e.localizedMessage ?: e.toString())
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: OldSchoolRepo? = null

        fun getInstance(context: Context): OldSchoolRepo = INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context).also { INSTANCE = it }
        }

        fun build(context: Context): OldSchoolRepo {
            val oldSchoolRepo = OldSchoolRepo();
            oldSchoolRepo.init(context)
            return oldSchoolRepo
        }

    }

    fun getVerses(text: String): List<Verse> {
        val verses = ArrayList<Verse>()
        val database = db.readableDatabase;
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