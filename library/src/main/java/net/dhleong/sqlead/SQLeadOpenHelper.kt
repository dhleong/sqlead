package net.dhleong.sqlead

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import java.io.File

/**
 * @author dhleong
 */
class SQLeadOpenHelper(
    private val config: SupportSQLiteOpenHelper.Configuration,
    dbDirectory: File? = null
) : SupportSQLiteOpenHelper {

    private val dbFile = dbDirectory?.let {
        if (!it.exists()) it.mkdirs()

        File(dbDirectory, databaseName)
    }

    private val db by lazy {
        // check *first*, since creating the connection might
        // implicitly touch the file
        val dbExists = dbFile != null && dbFile.exists()

        SQLeadSupportDatabase(
            dbPath = dbFile?.path
        ).also {
            val currentVersion = it.version
            val newVersion = config.callback.version

            config.callback.onConfigure(it)
            if (!dbExists) {
                config.callback.onCreate(it)
            } else if (currentVersion < newVersion) {
                config.callback.onUpgrade(it, currentVersion, newVersion)
            } else if (currentVersion > newVersion) {
                config.callback.onDowngrade(it, currentVersion, newVersion)
            }
            it.version = newVersion
            config.callback.onOpen(it)
        }
    }

    override fun getDatabaseName(): String = config.name ?: "test.db"

    override fun getWritableDatabase(): SupportSQLiteDatabase = db

    override fun getReadableDatabase(): SupportSQLiteDatabase = db

    override fun close() {
        db.close()
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
    }

}
