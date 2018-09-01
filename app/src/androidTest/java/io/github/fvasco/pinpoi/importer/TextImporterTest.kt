package io.github.fvasco.pinpoi.importer

import android.support.test.runner.AndroidJUnit4
import io.github.fvasco.pinpoi.util.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author Francesco Vasco
 */
@RunWith(AndroidJUnit4::class)
class TextImporterTest : AbstractImporterTestCase() {
    @Test
    fun testImportImplAsc() {
        val list = importPlacemark(TextImporter(), "asc.txt")
        assertEquals(4, list.size)
    }

    @Test
    fun testImportImplCsv() {
        val list = importPlacemark(TextImporter(), "csv.txt")
        assertEquals(3, list.size)
        assertEquals(Coordinates(1.0F, 2.5F), list[0].coordinates)
        assertEquals("location1 to import", list[0].name)
    }

    @Test
    fun testImportImplCsvLatLon() {
        val list = importPlacemark(TextImporter(), "csv.txt", FileFormatFilter.CSV_LAT_LON)
        assertEquals(Coordinates(1.0F, 2.5F), list[0].coordinates)
        assertEquals("location1 to import", list[0].name)
    }

    @Test
    fun testImportImplCsvLonLat() {
        val list = importPlacemark(TextImporter(), "csv.txt", FileFormatFilter.CSV_LON_LAT)
        assertEquals(Coordinates(2.5F, 1.0F), list[0].coordinates)
        assertEquals("location1 to import", list[0].name)
    }
}
