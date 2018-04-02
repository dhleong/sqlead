package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper

/**
 * @author dhleong
 */
class SQLeadOpenHelper(
    private val config: SupportSQLiteOpenHelper.Configuration
) : SupportSQLiteOpenHelper {

    private val db by lazy {
        SQLeadSupportDatabase().also {
            config.callback.onCreate(it)
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
