package snnafi.sqliteextension.example.db

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SupportSQLiteOpenHelper
import io.requery.android.database.DatabaseErrorHandler
import io.requery.android.database.sqlite.SQLiteCustomExtension
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteOpenHelper

/**
 * Implements [SupportSQLiteOpenHelper.Factory] using the SQLite implementation shipped in
 * this library.
 */
class CustomRequerySQLiteOpenHelperFactory @JvmOverloads constructor(private val configurationOptions: Iterable<ConfigurationOptions> = emptyList()) :
    SupportSQLiteOpenHelper.Factory {
    override fun create(config: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return CallbackSQLiteOpenHelper(
            config.context, config.name, config.callback,
            configurationOptions
        )
    }

    private class CallbackSQLiteOpenHelper internal constructor(
        context: Context?,
        name: String?,
        private val callback: SupportSQLiteOpenHelper.Callback,
        private val configurationOptions: Iterable<ConfigurationOptions>
    ) :
        SQLiteOpenHelper(
            context, name, null, callback.version, CallbackDatabaseErrorHandler(
                callback
            )
        ) {
        override fun onConfigure(db: SQLiteDatabase) {
            callback.onConfigure(db)
        }

        override fun onCreate(db: SQLiteDatabase) {
            callback.onCreate(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            callback.onUpgrade(db, oldVersion, newVersion)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            callback.onDowngrade(db, oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            callback.onOpen(db)
        }

        override fun createConfiguration(
            path: String,
            openFlags: Int
        ): SQLiteDatabaseConfiguration {
            Log.d("CustomRequery..Factory", "On Create Configuration")


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
    }

    private class CallbackDatabaseErrorHandler internal constructor(private val callback: SupportSQLiteOpenHelper.Callback) :
        DatabaseErrorHandler {
        override fun onCorruption(db: SQLiteDatabase) {
            callback.onCorruption(db)
        }
    }

    interface ConfigurationOptions {
        fun apply(configuration: SQLiteDatabaseConfiguration?): SQLiteDatabaseConfiguration
    }
}