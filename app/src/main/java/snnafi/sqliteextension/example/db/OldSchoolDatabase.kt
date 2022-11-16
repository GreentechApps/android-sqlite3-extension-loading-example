package snnafi.sqliteextension.example.db

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteException
import android.util.Log
import io.requery.android.database.sqlite.SQLiteCustomExtension
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class OldSchoolDatabase(val context: Context) : SQLiteOpenHelper(context, "quran.db", null, 1) {

    private var db: SQLiteDatabase? = null
    private var dbPath = ""
    private val dbName = "quran.db"

    init {
        dbPath =
            context.getDatabasePath(dbName).absolutePath // "/data/data/snnafi.sqliteextension.example/databases/quran.db"
        Log.d("DB PATH", dbPath)
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    override fun createConfiguration(path: String?, openFlags: Int): SQLiteDatabaseConfiguration {
        Log.d("OldSchoolDatabase", "On Create Configuration")

        val config = SQLiteDatabaseConfiguration(
            SQLiteDatabaseConfiguration.MEMORY_DB_PATH,
            SQLiteDatabase.CREATE_IF_NECESSARY
        )

        config.customExtensions.add(
            SQLiteCustomExtension(
                "libarabictokenizer.so",
                "sqlite3_sqlitearabictokenizer_init"
            )
        )
        return config
    }

    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist) {
            // do nothing - database already exist
        } else {
            // By calling this method and
            // the empty database will be
            // created into the default system
            // path of your application
            // so we are gonna be able
            // to overwrite that database
            // with our database.
            this.writableDatabase
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error(
                    "Error copying database"
                )
            }
        }
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        // Open your local db as the input stream
        val myInput: InputStream = context.getAssets()
            .open("db/$dbName")

        // Path to the just created empty db
        val outFileName: String = dbPath

        // Open the empty db as the output stream
        val myOutput: OutputStream = FileOutputStream(outFileName)

        // transfer bytes from the
        // inputfile to the outputfile
        val buffer = ByteArray(1024)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }

        // Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val path: String = dbPath
            checkDB = SQLiteDatabase
                .openDatabase(
                    path, null,
                    SQLiteDatabase.OPEN_READONLY
                )
        } catch (e: SQLiteException) {

            // database doesn't exist yet.
            Log.e("message", "" + e)
        }
        checkDB?.close()
        return checkDB != null
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        // Open the database
        val path: String = dbPath
        db = SQLiteDatabase
            .openDatabase(
                path, null,
                SQLiteDatabase.OPEN_READONLY
            )
    }

    @Synchronized
    override fun close() {
        // close the database.
        db?.close()
        super.close()
    }
}