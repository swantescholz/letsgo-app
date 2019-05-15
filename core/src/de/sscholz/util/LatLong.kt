package de.sscholz.util

import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

/**
 * lat is North (90 deg), South (-90 deg)
 * long is West (-180 deg) to East (180 deg)
 */
data class LatLong(val lat: Double, val long: Double) {
    override fun toString(): String {
        return "%.8f N, %.8f E".enFormat(lat, long)
    }

    fun geodesicDistanceTo(otherLocation: LatLong): Float {
        val lat2 = otherLocation.lat
        val long2 = otherLocation.long
        val earthRadius = 6371000.0 //meters
        val dLat = Math.toRadians(lat2 - lat)
        val dLng = Math.toRadians(long2 - long)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }

    // how much meter each (x-coordinate of result) and *afterwards* how many
    // meters north do I have to go on earth to get from this location to otherLocation?
    fun eastNorthDistanceTo(otherLocation: LatLong): Vector2 {
        var dx = geodesicDistanceTo(LatLong(lat, otherLocation.long))
        var dy = geodesicDistanceTo(LatLong(otherLocation.lat, long))
        if (long > otherLocation.long) {
            dx *= -1
        }
        if (lat > otherLocation.lat) {
            dy *= -1
        }
        return vec2(dx, dy)
    }

    operator fun plus(other: LatLong): LatLong {
        return LatLong(lat + other.lat, long + other.long)
    }
}