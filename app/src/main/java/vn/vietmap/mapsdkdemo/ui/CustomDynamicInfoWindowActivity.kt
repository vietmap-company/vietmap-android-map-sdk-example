package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.IconUtils
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.Marker
import vn.vietmap.vietmapsdk.annotations.MarkerOptions
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import java.util.Locale

class CustomDynamicInfoWindowActivity : AppCompatActivity(), OnMapReadyCallback,
    VietMapGL.OnMapClickListener {
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_dynamic_info_window)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: VietMapGL) {

        vietMapGL = map
        map.setStyle(VietMapTiles.instance.lightVector())

        // Add info window adapter
        addCustomInfoWindowAdapter(vietMapGL!!)

        // Keep info windows open on click
        vietMapGL.uiSettings.isDeselectMarkersOnTap = false

        // Add a marker
        marker = addMarker(vietMapGL!!)
        vietMapGL.selectMarker(marker!!)

        // On map click, change the info window contents
        vietMapGL.addOnMapClickListener(this)

        // Focus on Paris
        vietMapGL.animateCamera(CameraUpdateFactory.newLatLng(HANOI))
    }

    override fun onMapClick(p0: LatLng): Boolean {

        if (marker == null) {
            return false
        }

        // Distance from click to marker
        val distanceKm = marker!!.position.distanceTo(p0) / 1000

        // Get the info window
        val infoWindow = marker!!.infoWindow

        // Get the view from the info window
        if (infoWindow != null && infoWindow.view != null) {
            // Set the new text on the text view in the info window
            val textView = infoWindow.view as TextView?
            textView!!.text = String.format(Locale.getDefault(), "%.2fkm", distanceKm)
            // Update the info window position (as the text length changes)
            textView.post { infoWindow.update() }
        }
        return true
    }

    private fun addMarker(vietMapGL: VietMapGL): Marker {
        return vietMapGL.addMarker(
            MarkerOptions()
                .position(HANOI)
                .icon(
                    IconUtils().drawableToIcon(
                        this,
                        R.drawable.ic_my_location,
                        ResourcesCompat.getColor(resources, R.color.blueAccent, theme)
                    )
                )
        )
    }

    private fun addCustomInfoWindowAdapter(vietMapGL: VietMapGL) {
        val padding = resources.getDimension(R.dimen.attr_margin).toInt()
        vietMapGL.infoWindowAdapter = VietMapGL.InfoWindowAdapter { marker: Marker ->
            val textView = TextView(this)
            textView.text = marker.title
            textView.setBackgroundColor(Color.WHITE)
            textView.setText(R.string.action_calculate_distance)
            textView.setTextColor(Color.BLACK)
            textView.setPadding(padding, padding, padding, padding)
            textView
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
        vietMapGL.removeOnMapClickListener(this)
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private val HANOI = LatLng(21.027603, 105.833148)
    }
}