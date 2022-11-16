package snnafi.sqliteextension.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import snnafi.sqliteextension.example.dao.HadithDao
import snnafi.sqliteextension.example.model.Hadith

@Database(entities = [Hadith::class], version = 1)
abstract class HadithDatabase : RoomDatabase() {

    abstract fun hadithDao(): HadithDao

    companion object {
        @Volatile
        private var INSTANCE: HadithDatabase? = null

        fun getInstance(context: Context): HadithDatabase = INSTANCE
            ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }


        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, HadithDatabase::class.java, "hadith.db")
                .openHelperFactory(CustomRequerySQLiteOpenHelperFactory())
                .createFromAsset("db/hadith.db")
                .fallbackToDestructiveMigration()
                .build()
    }

}