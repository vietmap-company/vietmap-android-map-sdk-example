package vn.vietmap.mapsdkdemo.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.geojson.Point
import vn.vietmap.mapsdkdemo.services.LocationService
//import vn.vietmap.services.android.navigation.ui.v5.R
//import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.location.LocationComponent
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.engine.LocationEngine
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback
import vn.vietmap.vietmapsdk.location.engine.LocationEngineRequest
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult
import vn.vietmap.vietmapsdk.location.modes.CameraMode
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.VietMapGL
import java.lang.Exception

@Preview
@SuppressLint("MissingPermission")
@Composable
fun VietMapGL(modifier: Modifier = Modifier, vmgl: VietMapGL? =null){
    val context = LocalContext.current
//    val marker = remember(context){
////        context.getDrawable(R.drawable.map_marker_dark)!!.toBitmap()
//    }
    var locationEngine: LocationEngine? = null
    var locationComponent: LocationComponent? = null
    AndroidView(factory = {
        MapView(it).also { mapview->
            mapview.getMapAsync { vietmapGL ->
//                locationEngine =
//                    LocationEngineProvider.getBestLocationEngine(it)
                vietmapGL?.setStyle("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=YOUR_API_KEY_HERE"){
                        style ->
                    locationComponent = vietmapGL.locationComponent
                    locationComponent!!.activateLocationComponent(
                        LocationComponentActivationOptions
                            .builder(it, style!!)
                            .useSpecializedLocationLayer(true)
                            .locationEngine(locationEngine)
                            .locationEngineRequest(
                                LocationEngineRequest.Builder(750)
                                    .setFastestInterval(750)
                                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                                    .build()
                            )
                            .build()
                    )
                    locationComponent!!.isLocationComponentEnabled = true
                    locationComponent!!.cameraMode = CameraMode.TRACKING_COMPASS
                    locationEngine?.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
                        override fun onSuccess(p0: LocationEngineResult?) {
                            vietmapGL?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p0?.lastLocation?.latitude?:0.0,p0?.lastLocation?.longitude?:0.0),11.0))
                        }

                        override fun onFailure(p0: Exception) {
                            TODO("Not yet implemented")
                        }
                    } )

                }

            }
        }

    })
}
@Preview
@Composable
fun MapScreen(){
    var point: Point? by remember {
        mutableStateOf(null)
    }
    var relaunch by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (!permissions.values.all { it }) {
                //handle permission denied
            }
            else {
                relaunch = !relaunch
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        VietMapGL(
            modifier = Modifier
                .fillMaxSize()
        )
    }

    LaunchedEffect(key1 = relaunch) {
        try {
            LocationService
            val location = LocationService.getCurrentLocation(context)
            point = Point.fromLngLat(location.longitude(), location.latitude())

        } catch (e: LocationService.LocationServiceException) {
            when (e) {
                is LocationService.LocationServiceException.LocationDisabledException -> {
                    //handle location disabled, show dialog or a snack-bar to enable location
                }

                is LocationService.LocationServiceException.MissingPermissionException -> {
                    permissionRequest.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                is LocationService.LocationServiceException.NoNetworkEnabledException -> {
                    //handle no network enabled, show dialog or a snack-bar to enable network
                }

                is LocationService.LocationServiceException.UnknownException -> {
                    //handle unknown exception
                }
            }
        }
    }
}
