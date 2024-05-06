package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.model.CityStateMarker
import vn.vietmap.mapsdkdemo.model.CityStateMarkerOptions
import vn.vietmap.mapsdkdemo.utils.IconUtils
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.Marker
import vn.vietmap.vietmapsdk.annotations.MarkerOptions
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.VietMapGL
import java.text.DecimalFormat

class CustomInfoWindowActivity : AppCompatActivity(),
    VietMapGL.OnInfoWindowCloseListener,
    VietMapGL.OnInfoWindowClickListener,
    VietMapGL.OnInfoWindowLongClickListener {
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var customMarker: Marker? = null
    private val mapLongClickListener = VietMapGL.OnMapLongClickListener { point ->
        if (customMarker != null) {
            // Remove previous added marker
            vietMapGL.removeAnnotation(customMarker!!)
            customMarker = null
        }

        // Add marker on long click location with default marker image
        customMarker = vietMapGL.addMarker(
            MarkerOptions()
                .title("Custom Marker")
                .snippet(
                    DecimalFormat("#.#####").format(point.latitude) + ", " +
                            DecimalFormat("#.#####").format(point.longitude)
                )
                .position(point)
        )
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_info_window)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            vietMapGL = it
            vietMapGL.setStyle(VietMapTiles.instance.lightVector()) {
                /// this function will add 4 markers with city icon
                addCustomInfoWindowMarker()
                /// Change color of 4 markers you've added above
                addCustomInfoWindowAdapter()
                ///--------------------------------------------------------
                addInfoWindow()
                addInfoWindowListeners()
            }
        }
    }

    private fun addInfoWindowListeners() {
        vietMapGL.onInfoWindowCloseListener = this
        vietMapGL.addOnMapLongClickListener(mapLongClickListener)
        vietMapGL.onInfoWindowClickListener = this
        vietMapGL.onInfoWindowLongClickListener = this
    }
    private fun addInfoWindow() {
        vietMapGL.addMarker(
            MarkerOptions().title("Quần đảo Hoàng Sa").snippet("Đà Nẵng, Việt Nam").position(
                LatLng(16.651943, 112.298148)
            )
        )
        vietMapGL.addMarker(
            MarkerOptions().title("Quần đảo Trường Sa").snippet("Khánh Hoà, Việt Nam").position(
                LatLng(10.684165, 115.731585)
            )
        )
        vietMapGL.addMarker(
            MarkerOptions().title("Phú Quốc").snippet("Kiên Giang, Việt Nam").position(
                LatLng(10.259237, 104.001404)
            )
        )

    }

    private fun addCustomInfoWindowMarker() {
        vietMapGL.addMarkers(
            listOf(
                generateCityStateMarker(
                    "Hà Nội",
                    21.027144, 105.828982,
                    "#795548"
                ), generateCityStateMarker(
                    "Huế", 16.447318, 107.534942,
                    "#795548"
                ),
                generateCityStateMarker(
                    "Nha Trang", 12.247798, 109.162579,
                    "#795548"
                ),
                generateCityStateMarker(
                    "TP.Hồ Chí Minh",
                    10.814748, 106.649797,
                    "#795548"
                )
            )
        )
    }

    private fun generateCityStateMarker(
        title: String,
        lat: Double,
        lng: Double,
        color: String
    ): CityStateMarkerOptions {
        val marker = CityStateMarkerOptions()
        marker.title(title)
        marker.position(LatLng(lat, lng))
        marker.infoWindowBackground(color)

        val icon =
            IconUtils().drawableToIcon(this, R.drawable.ic_location_city, Color.parseColor(color))
        marker.icon(icon)
        return marker
    }

    private fun addCustomInfoWindowAdapter() {
        vietMapGL.infoWindowAdapter = object : VietMapGL.InfoWindowAdapter {
            private val tenDp = resources.getDimension(R.dimen.attr_margin).toInt()
            override fun getInfoWindow(marker: Marker): View? {
                val textView = TextView(this@CustomInfoWindowActivity)
                textView.text = marker.title
                textView.setTextColor(Color.WHITE)
                if (marker is CityStateMarker) {
                    textView.setBackgroundColor(Color.parseColor(marker.infoWindowBackgroundColor))
                }
                textView.setPadding(tenDp, tenDp, tenDp, tenDp)
                return textView
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

    override fun onInfoWindowClose(p0: Marker) {
        Toast.makeText(this, "Marker ${p0.title} closed", Toast.LENGTH_LONG).show()
    }

    override fun onInfoWindowClick(marker: Marker): Boolean {
        Toast.makeText(applicationContext, "OnClick: " + marker.title, Toast.LENGTH_LONG).show()
        // returning true will leave the info window open
        return false
    }

    override fun onInfoWindowLongClick(marker: Marker) {
        Toast.makeText(applicationContext, "OnLongClick: " + marker.title, Toast.LENGTH_LONG).show()
    }
}