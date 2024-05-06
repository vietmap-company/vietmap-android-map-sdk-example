package vn.vietmap.mapsdkdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.IconUtils
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.Marker
import vn.vietmap.vietmapsdk.annotations.MarkerOptions
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.VietMapGL

class DynamicMarkerChangeActivity : AppCompatActivity() {
    lateinit var vietmapGL:VietMapGL
    lateinit var mapView: MapView

    private var marker: Marker? = null
    companion object {
        private val LAT_LNG_HANOI = LatLng(21.0227784,105.8163212)
        private val LAT_LNG_HOCHIMINH = LatLng(10.7552921,106.3648935)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_marker_change)
        mapView = findViewById(R.id.mapView)
        mapView.tag = false
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{
            vietmapGL = it
            VietMapTiles.instance.lightVector()
            val markerOptions = MarkerOptions().position(LAT_LNG_HANOI).icon(IconUtils().drawableToIcon(this, R.drawable.ic_stars,ResourcesCompat.getColor(resources,
                vn.vietmap.vietmapsdk.R.color.vietmap_blue,theme) )).title("Ha Noi").snippet("VietNam Capital")
            marker = vietmapGL.addMarker(markerOptions)
        }
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        fab.setOnClickListener{
            updateMarker()
        }
    }
    private  fun updateMarker(){
        val first = mapView.tag as Boolean
        mapView.tag = !first
        marker!!.position = if(first) LAT_LNG_HANOI else LAT_LNG_HOCHIMINH
        marker!!.icon = IconUtils().drawableToIcon(this, R.drawable.ic_stars, if(first){ResourcesCompat.getColor(resources, vn.vietmap.services.android.navigation.R.color.vietmap_blue, theme)}else
        {ResourcesCompat.getColor(resources, R.color.black, theme)})
        marker!!.title = if(first)"Ha Noi" else "Ho Chi Minh"
        marker!!.snippet = if(first)"VietNam Capital" else "HoChiMinh city"
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