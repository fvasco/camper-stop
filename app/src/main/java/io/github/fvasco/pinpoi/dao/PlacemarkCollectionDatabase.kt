package io.github.fvasco.pinpoi.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Placemark collection database
 *
 * @author Francesco Vasco
 */
internal class PlacemarkCollectionDatabase(context: Context) :
    SQLiteOpenHelper(context, "PlacemarkCollection", null, /*version*/ 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE PLACEMARK_COLLECTION (" +
                    "_ID INTEGER primary key autoincrement," +
                    "name TEXT NOT NULL," +
                    "description TEXT NOT NULL," +
                    "source TEXT NOT NULL," +
                    "category TEXT NOT NULL," +
                    "last_update INTEGER NOT NULL," +
                    "poi_count INTEGER NOT NULL," +
                    "fileFormatFilter TEXT NOT NULL" +
                    ")"
        )
        db.execSQL("CREATE UNIQUE INDEX IDX_PLACEMARK_COLL_NAME ON PLACEMARK_COLLECTION (name)")
        db.execSQL("CREATE UNIQUE INDEX IDX_PLACEMARK_COLL_CAT_NAME ON PLACEMARK_COLLECTION (category,name)")

        // insert collection example: UNESCO
        db.execSQL(
            "INSERT INTO PLACEMARK_COLLECTION (name," +
                    "description," +
                    "source," +
                    "category," +
                    "last_update, poi_count," +
                    "fileFormatFilter)" +
                    "VALUES" +
                    "('World Heritage List'," +
                    "'Terms and Conditions of Use: http://whc.unesco.org/en/syndication/'," +
                    "'https://whc.unesco.org/en/list/kml/'," +
                    "''," +
                    "0, 0," +
                    "'KML')"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("UPDATE PLACEMARK_COLLECTION SET category='' where category is null")
        }
        if (oldVersion < 3) {
            db.execSQL("alter table PLACEMARK_COLLECTION add column fileFormatFilter TEXT")
        }
    }
}
