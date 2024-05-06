package vn.vietmap.mapsdkdemo.utils

import android.location.Location
import vn.vietmap.vietmapsdk.geometry.LatLngBounds
import timber.log.Timber
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.Style
import java.util.*

/**
 * Useful utilities used throughout the test app.
 */
object Utils {
    private val STYLES = arrayOf(
        VietMapTiles.instance.lightVector(),
        VietMapTiles.instance.lightRaster(),
        VietMapTiles.instance.google(),
        VietMapTiles.instance.googleSatellite()
    )
    private var index = 0

    /**
     * Utility to cycle through map styles. Useful to test if runtime styling source and layers transfer over to new
     * style.
     *
     * @return a string ID representing the map style
     */
    fun nextStyle(): String {
        index++
        if (index == STYLES.size) {
            index = 0
        }
        return STYLES[index]
    }

    /**
     * Utility for getting a random coordinate inside a provided bounds and creates a [Location] from it.
     *
     * @param bounds bounds of the generated location
     * @return a [Location] object using the random coordinate
     */
    fun getRandomLocation(bounds: LatLngBounds): Location {
        val random = Random()
        val randomLat = bounds.latitudeSouth + (bounds.latitudeNorth - bounds.latitudeSouth) * random.nextDouble()
        val randomLon = bounds.longitudeWest + (bounds.longitudeEast - bounds.longitudeWest) * random.nextDouble()
        val location = Location("random-loc")
        location.longitude = randomLon
        location.latitude = randomLat
        Timber.d("getRandomLatLng: %s", location.toString())
        return location
    }
    fun getVietNamRandomCoordinate(): Location {
        // Define the bounding box for Vietnam
        val minLat = 8.18
        val maxLat = 23.39
        val minLong = 102.14
        val maxLong = 109.46
        // Generate random latitude and longitude within the bounds
        val randomLat = kotlin.random.Random.nextDouble(minLat, maxLat)
        val randomLong = kotlin.random.Random.nextDouble(minLong, maxLong)
        val location = Location("random-loc")
        location.longitude = randomLong
        location.latitude = randomLat
        return location
    }
}
