package io.github.fvasco.pinpoi.util

import java.util.Comparator

import io.github.fvasco.pinpoi.model.PlacemarkSearchResult

/**
 * Compare placemark using distance from a specific [Coordinates]

 * @author Francesco Vasco
 */
class PlacemarkDistanceComparator(private val center: Coordinates) : Comparator<PlacemarkSearchResult> {

    override fun compare(plhs: PlacemarkSearchResult, prhs: PlacemarkSearchResult): Int {
        val lhs = plhs.coordinates
        val rhs = prhs.coordinates
        var res = java.lang.Double.compare(calculateDistance(lhs), calculateDistance(rhs))
        if (res == 0) {
            // equals <==> same coordinates
            res = java.lang.Float.compare(lhs.latitude, rhs.latitude)
            if (res == 0) res = java.lang.Float.compare(lhs.longitude, rhs.longitude)
        }
        return res
    }

    /**
     * Calculate distance to placemark
     * {@see Location@distanceTo}
     */
    fun calculateDistance(p: Coordinates): Double {
        return center.distanceTo(p).toDouble()
    }
}
