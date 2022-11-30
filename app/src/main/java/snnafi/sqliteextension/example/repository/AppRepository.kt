package snnafi.sqliteextension.example.repository

import android.content.Context
import android.database.Cursor
import android.util.Log
import snnafi.sqliteextension.example.db.DatabaseOpenHelper
import snnafi.sqliteextension.example.model.Verse
import java.nio.ByteBuffer
import java.nio.ByteOrder


class AppRepository {
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
            val oldSchoolRepo = AppRepository()
            oldSchoolRepo.init(context)
            return oldSchoolRepo
        }

    }

    /**
     * Convert byte array to int array (little endian).
     */
    fun ByteArray.toIntArray(): Array<Int> {
        val intBuf = ByteBuffer.wrap(this)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asIntBuffer()
        val array = IntArray(intBuf.remaining())
        intBuf.get(array)
        return array.toTypedArray()
    }

    fun getVerses(text: String): List<Verse> {
        val verses = ArrayList<Verse>()
        val database = helper.readableDatabase
//        val query = "SELECT roundFunction2(3.14)"
        val query = "SELECT rowid, *, rank(matchinfo(dict1, 'pcx'), 1.0) FROM dict1 where dict1 MATCH '$text' order by rank(matchinfo(dict1, 'pcx'), 1.0)"
//        val query = "SELECT rowid, *, rank(matchinfo(verses,'pcx'), 1.0) FROM verses where text MATCH '$text'"
//        val query = "SELECT rowid, *, rank(matchinfo(verses), 1.0) FROM verses where text MATCH '$text' order by rank(matchinfo(verses), 1.0)"
//        val query = "SELECT rowid, * FROM verses where text MATCH '$text'"
        Log.d("Ranking", "song score: $query")
        // Add a sqlite function to rank the results okapi_bm25
        database.addFunction("rank", 2) { args, result ->
            var score = 0.0

            val pOffset = 0
            val cOffset = 1

            val matchinfo = args.getBlob(0).toIntArray()
            val termCount = matchinfo[pOffset]
            val colCount = matchinfo[cOffset]

            val xOffset = cOffset + 1

            for (column in 0 until colCount) {
                var columnScore = 0.0
                for (i in 0 until termCount) {
                    val currentX = xOffset + (3 * (column + i * colCount))
                    val hitCount = matchinfo[currentX].toDouble()

                    if (hitCount > 0) {
                        Log.d(
                            "Ranking",
                            "column $column i: $i score: $columnScore ${(2 + i * column * 3) * 4}"
                        )
                        columnScore += (1 + hitCount/10)
                    }
                }
                score += columnScore/termCount
            }

            Log.d("Ranking", "song score: $score")
//            result.set(matchinfo.contentToString())
            result.set(-score)
        }

        val cursor: Cursor = database.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                var first = cursor.getInt(0)
                var second = cursor.getInt(1)
                var third = cursor.getInt(2)
                var fourth = cursor.getString(3)
                val verse =
                    Verse(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(5)
                    )
                verses.add(verse)
            } while (cursor.moveToNext())
        }

        return verses
    }

}