package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.mock
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author dhleong
 */
class QueryTest {

    private lateinit var db: SupportSQLiteDatabase

    @Before fun setUp() {
        db = SQLeadOpenHelper(
            SupportSQLiteOpenHelper.Configuration.builder(mock {  })
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase?) {
                        initDb()
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                        throw UnsupportedOperationException()
                    }
                })
                .name("test.db")
                .build()
        ).writableDatabase
    }

    private fun initDb() {
        // TODO
    }

    @After fun tearDown() {
        db.close()
    }

    @Test fun `Simple query`() {
        val result = db.query("""
            SELECT 42
            """
        ).use {
            it.getInt(1)
        }

        assert(result).isEqualTo(42)
    }

}