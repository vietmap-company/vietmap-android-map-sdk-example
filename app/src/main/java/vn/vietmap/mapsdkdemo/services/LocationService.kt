package vn.vietmap.mapsdkdemo.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

internal object LocationService {
    suspend fun getCurrentLocation(context: Context): Point {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        when {
            !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> throw LocationServiceException.LocationDisabledException()
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> throw LocationServiceException.NoNetworkEnabledException()
            else -> {
                // Building FusedLocationProviderClient
                val locationProvider = LocationServices.getFusedLocationProviderClient(context)
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .build()
                runCatching {
                    val location = if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        throw LocationServiceException.MissingPermissionException()
                    } else {
                        locationProvider.getCurrentLocation(request, null).await()
                    }
                    return Point.fromLngLat(location.longitude, location.latitude)
                }.getOrElse {
                    throw LocationServiceException.UnknownException(stace = it.stackTraceToString())
                }
            }
        }
    }
    sealed class LocationServiceException : Exception() {
        class MissingPermissionException : LocationServiceException()
        class LocationDisabledException : LocationServiceException()
        class NoNetworkEnabledException : LocationServiceException()
        class UnknownException(val stace: String) : LocationServiceException()
    }
}