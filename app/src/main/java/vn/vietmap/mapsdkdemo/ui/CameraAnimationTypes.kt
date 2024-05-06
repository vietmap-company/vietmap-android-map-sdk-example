package vn.vietmap.mapsdkdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import timber.log.Timber
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.maps.VietMapGL.OnCameraIdleListener

class CameraAnimationTypes : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var vietMapGL: VietMapGL
    private lateinit var mapView: MapView
    private var cameraState = false
    private val cameraIdleListener = OnCameraIdleListener {
        if (vietMapGL != null) {
            Timber.w(vietMapGL.cameraPosition.toString())
        }
    }

    private val callback: VietMapGL.CancelableCallback =
        object : VietMapGL.CancelableCallback {
            override fun onCancel() {
                Timber.i("Duration onCancel Callback called.")
                Toast.makeText(
                    this@CameraAnimationTypes.applicationContext,
                    "Ease onCancel Callback called.",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            override fun onFinish() {
                Timber.i("Duration onFinish Callback called.")
                Toast.makeText(
                    this@CameraAnimationTypes.applicationContext,
                    "Ease onFinish Callback called.",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_types)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(p0: VietMapGL) {
        vietMapGL = p0
        vietMapGL.setStyle(VietMapTiles.instance.lightVector())
        vietMapGL.addOnCameraIdleListener(cameraIdleListener)
        val moveButton = findViewById<View>(R.id.cameraMoveButton)
        moveButton?.setOnClickListener { view: View ->
            val cameraPosition = CameraPosition.Builder().target(nextLatLng).zoom(8.0)
                .tilt(30.0)
                .bearing(0.0)
                .tilt(0.0).build()
            vietMapGL.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
        val easeButton = findViewById<View>(R.id.cameraEaseButton)
        easeButton?.setOnClickListener { view: View ->
            val cameraPosition = CameraPosition.Builder().target(nextLatLng).zoom(8.0)
                .bearing(0.0)
                .tilt(30.0).build()
            vietMapGL.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),7500,
                callback)
        }
        val animateButton = findViewById<View>(R.id.cameraAnimateButton)
        animateButton?.setOnClickListener { view: View ->
            val cameraPosition = CameraPosition.Builder().target(nextLatLng).zoom(8.0).bearing(0.0).tilt(20.0).build()
            vietMapGL.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),7500,
                callback)
        }
    }

    private val nextLatLng: LatLng
        private get() {
            cameraState = !cameraState
            return if (cameraState) LAT_LNG_HANOI else LAT_LNG_HOCHIMINH
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
        if (vietMapGL != null) {
            vietMapGL.removeOnCameraIdleListener(cameraIdleListener)
        }
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private val LAT_LNG_HOCHIMINH = LatLng(10.7552921, 106.3648935)
        private val LAT_LNG_HANOI = LatLng(21.0227784, 105.8163212)
    }
}