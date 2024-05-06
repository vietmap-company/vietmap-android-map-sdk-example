package vn.vietmap.mapsdkdemo.ui

import android.graphics.PointF
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import timber.log.Timber
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.VietMapGL

class CountFeatureInBoxActivity : AppCompatActivity(), OnMapReadyCallback , VietMapGL.OnMapClickListener{
    private lateinit var vietMapGL: VietMapGL
    lateinit var mapView: MapView
        private set
    private lateinit var selectionBox:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_feature_in_box)
        mapView = findViewById(R.id.mapView)
         selectionBox = findViewById<View>(R.id.selection_box)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(p0: VietMapGL) {
        vietMapGL = p0
        vietMapGL.setStyle(VietMapTiles.instance.lightVector())
        selectionBox.setOnClickListener{view:View->
            /// will return the location of selectionBox into the mapView
            val top = selectionBox.top - mapView.top
            val left = selectionBox.left - mapView.left
            /// left + selectionBox will return the position of the right of the box
            val box = RectF(left.toFloat(), top.toFloat(), (left+selectionBox.width).toFloat(), (top+ selectionBox.height).toFloat())
            Timber.i("Querying box %s", box)
            val features = vietMapGL.queryRenderedFeatures(box)
            Toast.makeText(
                this@CountFeatureInBoxActivity,
                String.format("%s features in box", features.size),
                Toast.LENGTH_SHORT
            ).show()
        }

        vietMapGL.addOnMapClickListener(this)
        Toast.makeText(applicationContext, "Click on marker or rectangle box to query the data from the map", Toast.LENGTH_LONG)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        if (::vietMapGL.isInitialized) {
            // Regression test for #14394
            vietMapGL.queryRenderedFeatures(PointF(0F, 0F))
        }
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


    override fun onMapClick(p0: LatLng): Boolean {
        val screenPosition = vietMapGL.projection.toScreenLocation(p0)
        val queryData = vietMapGL.queryRenderedFeatures(screenPosition)
        if(queryData.isEmpty()){
            Toast.makeText(this, "Not found feature on location you're clicked", Toast.LENGTH_LONG).show()
        }else{
            queryData.first().also {
                println(it)
                val name = (it.properties()?.get("shortname"))
                Toast.makeText(this, "Found feature with name: $name", Toast.LENGTH_LONG).show()
            }
        }

        return  true
    }

}