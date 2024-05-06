package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.PolygonOptions
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.geometry.LatLngBounds
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.VietMapGL

/**
 * Test activity showcasing restricting user gestures to bounds around VietNam's mainland, sea, and Hoang Sa, Truong Sa archipelagos
 */
class RestrictCameraToBoundsActivity : AppCompatActivity() , OnMapReadyCallback{
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restrict_camera_to_bounds)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(p0: VietMapGL) {
        vietMapGL = p0
        vietMapGL.setStyle(VietMapTiles.instance.lightVector()){
            vietMapGL.setMinZoomPreference(2.0)
            showCrossHair()
            setupBounds(VIETNAM_BOUNDS)
        }
    }

    ///  Setup bound to restrict user camera
    private fun setupBounds(bounds: LatLngBounds?) {
        vietMapGL.setLatLngBoundsForCameraTarget(bounds)
        showBoundsArea(bounds)
    }

    /// Show a polygon that draws the area where allow user can move the camera inside
    private fun showBoundsArea(bounds: LatLngBounds?) {
        vietMapGL.clear()
        if (bounds != null) {
            val boundsArea = PolygonOptions()
                .add(bounds.northWest)
                .add(bounds.northEast)
                .add(bounds.southEast)
                .add(bounds.southWest)
            boundsArea.alpha(0.25f)
            boundsArea.fillColor(Color.RED)
            vietMapGL.addPolygon(boundsArea)
        }
    }
    //show a blue point at the center of the map, which inside a bound that draw by another color
    private fun showCrossHair() {
        val crosshair = View(this)
        crosshair.layoutParams = FrameLayout.LayoutParams(10, 10, Gravity.CENTER)
        crosshair.setBackgroundColor(Color.BLUE)
        mapView.addView(crosshair)
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

    companion object {
        private val VIETNAM_BOUNDS = LatLngBounds.Builder()
            .include(LatLng(23.464164, 101.354597

                ))
            .include(LatLng(7.255109, 116.202593))
            .build()
    }
}