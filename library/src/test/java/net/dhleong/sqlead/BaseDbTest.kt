package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import com.nhaarman.mockito_kotlin.mock
import org.junit.After
import org.junit.Before

/**
 * @author dhleong
 */
abstract class BaseDbTest {
    protected lateinit var db: SupportSQLiteDatabase

    @Before fun setUp() {
        db = SQLeadSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(mock {  })
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        initDb(db)
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                        throw UnsupportedOperationException()
                    }
                })
                .name("test.db")
                .build()
        ).writableDatabase
    }

    protected open fun initDb(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE Ships (
                id INTEGER PRIMARY KEY,
                name TEXT,
                capacity INTEGER
            )
            """)
        db.execSQL("""
            INSERT INTO Ships (name, capacity)
            VALUES ("Serenity", 42)
            """)
    }

    @After fun tearDown() {
        db.close()
    }
}