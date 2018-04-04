package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import com.nhaarman.mockito_kotlin.mock
import org.junit.After
import org.junit.Before
import java.io.File

/**
 * @author dhleong
 */
abstract class BaseDbTest(
    inMemory: Boolean = true
) {

    private val dbDir =
        if (inMemory) null
        else File(".db-test")

    @Suppress("LeakingThis")
    private val factory = SQLeadSQLiteOpenHelperFactory(
        dbDirectory = dbDir
    )

    protected lateinit var db: SupportSQLiteDatabase

    @Before fun setUp() {
        if (dbDir != null) {
            // ensure we start with a clean slate;
            File(dbDir, DB_FILE_NAME).delete()
        }

        db = createDbUsingCallback(
            object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    initDb(db)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                    throw UnsupportedOperationException()
                }
            }
        )
    }

    protected fun createDbUsingCallback(
        callback: SupportSQLiteOpenHelper.Callback
    ): SupportSQLiteDatabase =
        factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(mock {  })
                .callback(callback)
                .name(DB_FILE_NAME)
                .build()
        ).writableDatabase

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

        db.execSQL("""
            CREATE TABLE Pilots (
                id INTEGER PRIMARY KEY,
                name TEXT,
                ship INTEGER REFERENCES Ship(id)
            )
            """)
        db.execSQL("""
            INSERT INTO Pilots (name, ship)
            VALUES ("Wash", (SELECT id FROM Ships WHERE name = "Serenity"))
            """)
    }

    @After fun tearDown() {
        db.close()
    }

    companion object {
        const val DB_FILE_NAME = "test.db"
    }
}