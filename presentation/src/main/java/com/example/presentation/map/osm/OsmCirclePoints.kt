package com.example.presentation.map.osm

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.osmdroid.util.GeoPoint

internal fun generateCirclePoints(center: GeoPoint, radiusMeters: Double, steps: Int = 64): List<GeoPoint> {
    val earthRadius = 6_371_000.0
    val lat = Math.toRadians(center.latitude)
    val lng = Math.toRadians(center.longitude)
    val d = radiusMeters / earthRadius

    return (0..steps).map { i ->
        val bearing = Math.toRadians(i * 360.0 / steps)
        val latR = asin(sin(lat) * cos(d) + cos(lat) * sin(d) * cos(bearing))
        val lngR = lng + atan2(sin(bearing) * sin(d) * cos(lat), cos(d) - sin(lat) * sin(latR))
        GeoPoint(Math.toDegrees(latR), Math.toDegrees(lngR))
    }
}
