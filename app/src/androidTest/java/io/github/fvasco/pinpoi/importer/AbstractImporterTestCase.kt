package io.github.fvasco.pinpoi.importer

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.github.fvasco.pinpoi.model.Placemark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import java.io.InputStream


/**
 * Base importer test case class.

 * @author Francesco Vasco
 */
abstract class AbstractImporterTestCase {


    protected lateinit var context: Context
        private set

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    /**
     * Execute import

     * @param resource resource file name
     * *
     * @return list of imported placemark
     */
    fun importPlacemark(
        importer: AbstractImporter,
        resource: String,
        fileFormatFilter: FileFormatFilter = FileFormatFilter.NONE
    ): List<Placemark> {
        checkNotNull(javaClass.getResourceAsStream(resource)).use {
            return importPlacemark(importer, it, fileFormatFilter)
        }
    }

    /**
     * Execute import
     */
    fun importPlacemark(
        importer: AbstractImporter,
        input: InputStream,
        fileFormatFilter: FileFormatFilter = FileFormatFilter.NONE
    ): List<Placemark> {
        val list = ArrayList<Placemark>()
        importer.collectionId = 1
        importer.consumer = { list.add(it) }
        importer.fileFormatFilter = fileFormatFilter
        importer.importPlacemarks(input)
        for (p in list) {
            assertEquals(0, p.id)
            assertTrue(p.name.isNotEmpty())
            assertTrue(!p.coordinates.latitude.isNaN())
            assertTrue(!p.coordinates.longitude.isNaN())
            assertEquals(1, p.collectionId)
        }
        return list
    }
}
