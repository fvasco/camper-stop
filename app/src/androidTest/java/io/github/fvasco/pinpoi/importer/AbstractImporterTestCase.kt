package io.github.fvasco.pinpoi.importer

import android.test.AndroidTestCase
import io.github.fvasco.pinpoi.model.Placemark
import java.io.InputStream
import java.util.*

/**
 * Base importer test case class.

 * @author Francesco Vasco
 */
abstract class AbstractImporterTestCase : AndroidTestCase() {

    /**
     * Execute import

     * @param resource resource file name
     * *
     * @return list of imported placemark
     */
    @Throws(Exception::class)
    fun importPlacemark(importer: AbstractImporter, resource: String, fileFormatFilter: FileFormatFilter = FileFormatFilter.NONE): List<Placemark> {
        javaClass.getResourceAsStream(resource).use {
            return importPlacemark(importer, it, fileFormatFilter)
        }
    }

    /**
     * Execute import
     */
    @Throws(Exception::class)
    fun importPlacemark(importer: AbstractImporter, input: InputStream, fileFormatFilter: FileFormatFilter = FileFormatFilter.NONE): List<Placemark> {
        val list = ArrayList<Placemark>()
        importer.collectionId = 1
        importer.consumer = { list.add(it) }
        importer.fileFormatFilter = fileFormatFilter
        importer.importPlacemarks(input)
        for (p in list) {
            assertEquals(0, p.id)
            assertTrue(!p.name.isEmpty())
            assertTrue(!p.coordinates.latitude.isNaN())
            assertTrue(!p.coordinates.longitude.isNaN())
            assertEquals(1, p.collectionId)
        }
        return list
    }
}
