package vn.vietmap.mapsdkdemo.ui

import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.Utils
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.geometry.LatLngBounds
import vn.vietmap.vietmapsdk.location.LocationComponent
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.engine.LocationEngine
import vn.vietmap.vietmapsdk.location.engine.LocationEngineDefault
import vn.vietmap.vietmapsdk.location.engine.LocationEngineRequest
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL

class ManualLocationUpdatesActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private var locationComponent: LocationComponent? = null
    private var locationEngine: LocationEngine? = null
    private lateinit var vietMapGL: VietMapGL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_locatoin_updates)
        locationEngine = LocationEngineDefault.getDefaultLocationEngine(applicationContext)
        val fabManualUpdate = findViewById<View>(R.id.fabManualLocationChange)
        fabManualUpdate.setOnClickListener {
            val location = Utils.getVietNamRandomCoordinate()
            if (locationComponent != null && locationComponent!!.locationEngine == null) {
                locationComponent!!.forceLocationUpdate(
                    location
                )
            }
            Toast.makeText(applicationContext, "Updated location to lat: ${location.latitude}, long: ${location.longitude}", Toast.LENGTH_LONG).show()
        }
        fabManualUpdate.isEnabled = false
        val fabToggle = findViewById<FloatingActionButton>(R.id.fabToggleManualLocation)
        fabToggle.setOnClickListener {
            if (locationComponent != null) {
                locationComponent!!.locationEngine =
                    if (locationComponent!!.locationEngine == null) locationEngine else null
                if (locationComponent!!.locationEngine == null) {
                    fabToggle.setImageResource(R.drawable.ic_stars)
                    fabManualUpdate.isEnabled = true
                    fabManualUpdate.alpha = 1f
                    Toast.makeText(
                        this@ManualLocationUpdatesActivity.applicationContext,
                        "LocationEngine disabled, use manual updates. ",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    fabToggle.setImageResource(R.drawable.ic_location_city)
                    fabManualUpdate.isEnabled = false
                    fabManualUpdate.alpha = 0.5f
                    Toast.makeText(
                        this@ManualLocationUpdatesActivity.applicationContext,
                        "LocationEngine enabled, SDK will update the location from device GPS",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: VietMapGL) {

        p0.setStyle(
            VietMapTiles.instance.googleSatellite()
        ) { style: Style? ->
            vietMapGL = p0
            locationComponent = p0.locationComponent
            locationComponent!!.activateLocationComponent(
                LocationComponentActivationOptions
                    .builder(this, style!!)
                    .locationEngine(locationEngine)
                    .locationEngineRequest(
                        LocationEngineRequest.Builder(500)
                            .setFastestInterval(500)
                            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build()
                    )
                    .build()
            )
            locationComponent!!.isLocationComponentEnabled = true
            locationComponent!!.renderMode = RenderMode.COMPASS
            vietMapGL.setMaxZoomPreference(18.0)
            mapView.addOnCameraDidChangeListener {
                println(vietMapGL.cameraPosition.zoom)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}