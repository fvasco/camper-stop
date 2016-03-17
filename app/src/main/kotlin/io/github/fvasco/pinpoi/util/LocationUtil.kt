package io.github.fvasco.pinpoi.util

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import io.github.fvasco.pinpoi.BuildConfig
import io.github.fvasco.pinpoi.PlacemarkDetailActivity
import io.github.fvasco.pinpoi.model.PlacemarkBase
import java.io.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * Location related utility

 * @author Francesco Vasco
 */
object LocationUtil {

    private val ADDRESS_CACHE_SIZE = 512
    /**
     * Store resolved address
     */
    private val ADDRESS_CACHE = LinkedHashMap<Coordinates, String>(ADDRESS_CACHE_SIZE * 2, .75f, true)
    private val addressCacheFile by lazy { File(Util.applicationContext.cacheDir, "addressCache") }

    val geocoder: Geocoder? by lazy {
        val applicationContext = Util.applicationContext
        if (Geocoder.isPresent())
            Geocoder(applicationContext)
        else null
    }

    /**
     * Get address and call optional addressConsumer in main looper
     */
    fun getAddressStringAsync(
            coordinates: Coordinates,
            addressConsumer: ((String?)->Unit)?): Future<String> {
        return Util.EXECUTOR.submit(Callable<kotlin.String> {
            var addressString: String? = synchronized (ADDRESS_CACHE) {
                if (ADDRESS_CACHE.isEmpty()) restoreAddressCache()
                ADDRESS_CACHE[coordinates]
            }
            if (addressString == null // resolve geocoder
                    && geocoder != null) {
                val addresses: List<Address>
                try {
                    addresses = LocationUtil.geocoder!!.getFromLocation(coordinates.latitude.toDouble(), coordinates.longitude.toDouble(), 1)
                } catch (e: Exception) {
                    addresses = listOf()
                }

                if (!addresses.isEmpty()) {
                    addressString = LocationUtil.toString(addresses[0])
                    // save result in cache
                    synchronized (ADDRESS_CACHE) {
                        ADDRESS_CACHE.put(coordinates, addressString!!)
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        saveAddressCache()
                    }
                }
            }
            if (addressConsumer != null) {
                if (Thread.interrupted()) {
                    throw InterruptedException()
                }
                Util.MAIN_LOOPER_HANDLER.post { addressConsumer(addressString) }
            }
            addressString
        })
    }

    fun newLocation(latitude: Double, longitude: Double): Location {
        val location = Location(Util::class.java.simpleName)
        location.latitude = latitude
        location.longitude = longitude
        location.accuracy = 0f
        location.time = System.currentTimeMillis()
        return location
    }

    /**
     * Convert an [Address] to address string
     */
    fun toString(address: Address?): String? {
        if (address == null) return null

        val separator = ", "
        if (address.maxAddressLineIndex == 0) {
            return address.getAddressLine(0)
        } else if (address.maxAddressLineIndex > 0) {
            val stringBuilder = StringBuilder(address.getAddressLine(0))
            for (i in 1..address.maxAddressLineIndex) {
                stringBuilder.append(separator).append(address.getAddressLine(i))
            }
            return stringBuilder.toString()
        } else {
            val stringBuilder = StringBuilder()
            append(address.featureName, separator, stringBuilder)
            append(address.locality, separator, stringBuilder)
            append(address.adminArea, separator, stringBuilder)
            append(address.countryCode, separator, stringBuilder)
            return if (stringBuilder.isEmpty()) address.toString() else stringBuilder.toString()
        }
    }

    /**
     * Open external map app

     * @param placemark       placemark to open
     * *
     * @param forceAppChooser if true show always app chooser
     */
    fun openExternalMap(placemark: PlacemarkBase, forceAppChooser: Boolean, context: Context) {
        try {
            val coordinateFormatted = placemark.coordinates.toString()
            val uri = Uri.Builder().scheme("geo").authority(coordinateFormatted).appendQueryParameter("q", coordinateFormatted + '(' + placemark.name + ')').build()
            var intent = Intent(Intent.ACTION_VIEW, uri)
            if (forceAppChooser) {
                intent = Intent.createChooser(intent, placemark.name)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(PlacemarkDetailActivity::class.java.simpleName, "Error on map click", e)
            showToast(e)
        }

    }

    private fun restoreAddressCache() {
        if (BuildConfig.DEBUG && !Thread.holdsLock(ADDRESS_CACHE)) throw AssertionError()
        if (addressCacheFile.canRead()) {
            try {
                val inputStream = DataInputStream(BufferedInputStream(FileInputStream(addressCacheFile)))
                try {
                    // first item is entry count
                    for (i in inputStream.readShort() downTo 1) {
                        val latitude = inputStream.readFloat()
                        val longitude = inputStream.readFloat()
                        val address = inputStream.readUTF()
                        ADDRESS_CACHE.put(Coordinates(latitude, longitude), address)
                    }
                } finally {
                    inputStream.close()
                }
            } catch (e: IOException) {
                Log.w(LocationUtil::class.java.simpleName, e)
                //noinspection ResultOfMethodCallIgnored
                addressCacheFile.delete()
            }

        }
    }

    private fun saveAddressCache() {
        if (BuildConfig.DEBUG && !Thread.holdsLock(ADDRESS_CACHE)) throw AssertionError()
        try {
            DataOutputStream(BufferedOutputStream(FileOutputStream(addressCacheFile))).use { outputStream ->
                // first item is entry count
                val iterator = ADDRESS_CACHE.entries.iterator()
                for (i in ADDRESS_CACHE.size downTo ADDRESS_CACHE_SIZE + 1) {
                    iterator.next()
                }
                outputStream.writeShort(Math.min(ADDRESS_CACHE_SIZE, ADDRESS_CACHE.size))
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val coordinates = entry.key
                    outputStream.writeFloat(coordinates.latitude)
                    outputStream.writeFloat(coordinates.longitude)
                    outputStream.writeUTF(entry.value)
                }
            }
        } catch (e: IOException) {
            Log.w(LocationUtil::class.java.simpleName, e)
            //noinspection ResultOfMethodCallIgnored
            addressCacheFile.delete()
        }

    }
}
