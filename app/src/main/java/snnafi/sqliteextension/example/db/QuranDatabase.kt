package snnafi.sqliteextension.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import snnafi.sqliteextension.example.dao.QuranDao
import snnafi.sqliteextension.example.model.Verse

@Database(entities = [Verse::class], version = 1)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getInstance(context: Context): QuranDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, QuranDatabase::class.java, "quran.db")
                .openHelperFactory(CustomRequerySQLiteOpenHelperFactory())
                .createFromAsset("db/quran.db")
                .fallbackToDestructiveMigration()
                .build()
    }

}