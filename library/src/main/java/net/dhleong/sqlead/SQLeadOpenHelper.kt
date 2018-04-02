package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import java.io.File

/**
 * @author dhleong
 */
class SQLeadOpenHelper(
    private val config: SupportSQLiteOpenHelper.Configuration,
    inMemory: Boolean = true
) : SupportSQLiteOpenHelper {

    private val db by lazy {
        SQLeadSupportDatabase(
            dbPath =
                if (inMemory) null
                else databaseName
        ).also {
            if (inMemory || !File(databaseName).exists()) {
                config.callback.onCreate(it)
            }
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
