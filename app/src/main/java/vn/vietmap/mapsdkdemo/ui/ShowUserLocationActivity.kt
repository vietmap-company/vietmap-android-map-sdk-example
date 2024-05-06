package vn.vietmap.mapsdkdemo.ui

import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.view.get
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.location.LocationComponent
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.LocationComponentOptions
import vn.vietmap.vietmapsdk.location.OnLocationCameraTransitionListener
import vn.vietmap.vietmapsdk.location.engine.LocationEngineRequest
import vn.vietmap.vietmapsdk.location.modes.CameraMode
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL

class ShowUserLocationActivity : AppCompatActivity() {
    private var location: Location? = null

    private lateinit var locationModeBtn: Button
    private lateinit var locationTrackingBtn: Button
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var locationComponent: LocationComponent? = null


    @CameraMode.Mode
    private var cameraMode = CameraMode.TRACKING

    @RenderMode.Mode
    private var renderMode = RenderMode.NORMAL

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_user_location)
        mapView = findViewById(R.id.mapView)
        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable(SAVED_STATE_LOCATION)
        }
        Toast.makeText(applicationContext, "Change mode and tracking options to see how it work", Toast.LENGTH_LONG)
        locationModeBtn = findViewById(R.id.button_location_mode)
        locationModeBtn.setOnClickListener(
            View.OnClickListener setOnClickListener@{ v: View? ->
                if (locationComponent == null) {
                    return@setOnClickListener
                }
                showModeListDialog()
            }
        )
        locationTrackingBtn = findViewById(R.id.button_location_tracking)
        locationTrackingBtn.setOnClickListener(
            View.OnClickListener setOnClickListener@{ v: View? ->
                if (locationComponent == null) {
                    return@setOnClickListener
                }
                showTrackingListDialog()
            }
        )
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { vmgl ->
            vietMapGL = vmgl
            vietMapGL.setStyle(VietMapTiles.instance.lightVector()) {
                locationComponent = vietMapGL.locationComponent
                val locationComponentOptions = LocationComponentOptions.builder(this)
                    .pulseEnabled(true)
                    .build()
                val locationComponentActivationOptions =
                    buildLocationComponentActionOptions(it, locationComponentOptions)
                locationComponent!!.activateLocationComponent(locationComponentActivationOptions)
                locationComponent!!.isLocationComponentEnabled = true
                locationComponent!!.cameraMode = cameraMode
                locationComponent!!.forceLocationUpdate(location)
            }

        }
    }

    private fun showTrackingListDialog() {
        val trackingTypes: MutableList<String> = ArrayList()
        trackingTypes.addAll(
            listOf(
                "None",
                "None Compass",
                "None GPS",
                "Tracking",
                "Tracking Compass",
                "Tracking GPS",
                "Tracking GPS North"
            )
        )
        val profileAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, trackingTypes)
        val listPopup = ListPopupWindow(this)
        listPopup.setAdapter(profileAdapter)
        listPopup.anchorView = locationTrackingBtn
        listPopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val selectedTrackingType = trackingTypes[position]
            locationTrackingBtn.text = selectedTrackingType

            if (selectedTrackingType.contentEquals("None")) {
                setCameraTrackingMode(CameraMode.NONE)
            } else if (selectedTrackingType.contentEquals("None Compass")) {
                setCameraTrackingMode(CameraMode.NONE_COMPASS)
            } else if (selectedTrackingType.contentEquals("None GPS")) {
                setCameraTrackingMode(CameraMode.NONE_GPS)
            } else if (selectedTrackingType.contentEquals("Tracking")) {
                setCameraTrackingMode(CameraMode.TRACKING)
            } else if (selectedTrackingType.contentEquals("Tracking Compass")) {
                setCameraTrackingMode(CameraMode.TRACKING_COMPASS)
            } else if (selectedTrackingType.contentEquals("Tracking GPS")) {
                setCameraTrackingMode(CameraMode.TRACKING_GPS)
            } else if (selectedTrackingType.contentEquals("Tracking GPS North")) {
                setCameraTrackingMode(CameraMode.TRACKING_GPS_NORTH)
            }
            listPopup.dismiss()

        }
        listPopup.show()
    }

    private fun setCameraTrackingMode(@CameraMode.Mode mode: Int) {
        locationComponent!!.setCameraMode(
            mode,
            1200,
            16.0,
            null,
            45.0,
            object :
                OnLocationCameraTransitionListener {
                override fun onLocationCameraTransitionFinished(@CameraMode.Mode cameraMode: Int) {
                    Toast.makeText(
                        applicationContext,
                        "Transition finished",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onLocationCameraTransitionCanceled(@CameraMode.Mode cameraMode: Int) {
                    Toast.makeText(
                        applicationContext,
                        "Transition canceled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun showModeListDialog() {
        val modes: MutableList<String> = ArrayList()
        modes.addAll(listOf("Normal", "Compass", "GPS"))
        val profileAdapter =
            ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, modes)
        val listPopup = ListPopupWindow(applicationContext)
        listPopup.setAdapter(profileAdapter)
        listPopup.anchorView = locationModeBtn
        listPopup.setOnItemClickListener { parent: AdapterView<*>?, itemView: View?, position: Int, id: Long ->
            val selectedMode = modes[position]
            locationModeBtn.text = selectedMode
            if (selectedMode.contentEquals("Normal")) {
                setRendererMode(RenderMode.NORMAL)
            } else if (selectedMode.contentEquals("Compass")) {
                setRendererMode(RenderMode.COMPASS)
            } else if (selectedMode.contentEquals("GPS")) {
                setRendererMode(RenderMode.GPS)
            }
            listPopup.dismiss()
        }
        listPopup.show()

    }

    @SuppressLint("SetTextI18n")
    private fun setRendererMode(@RenderMode.Mode mode: Int) {
        renderMode = mode
        locationComponent!!.renderMode = mode
        if (mode == RenderMode.NORMAL) {
            locationModeBtn.text = "Normal"
        } else if (mode == RenderMode.COMPASS) {
            locationModeBtn.text = "Compass"
        } else if (mode == RenderMode.GPS) {
            locationModeBtn.text = "Gps"
        }
    }

    private fun buildLocationComponentActionOptions(
        style: Style,
        locationComponentOptions: LocationComponentOptions
    ): LocationComponentActivationOptions {
        return LocationComponentActivationOptions.builder(applicationContext, style)
            .locationComponentOptions(locationComponentOptions).useDefaultLocationEngine(true)
            .locationEngineRequest(
                LocationEngineRequest.Builder(750).setFastestInterval(750)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build()
            ).build()
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

    @SuppressLint("MissingPermission")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        if (locationComponent != null) {
            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent!!.lastKnownLocation)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val SAVED_STATE_LOCATION = "saved_state_location"
        private const val TAG = "Vmgl-BasicLocationPulsingCircleActivity"
    }
}