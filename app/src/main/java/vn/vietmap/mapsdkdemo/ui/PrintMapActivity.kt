package vn.vietmap.mapsdkdemo.ui

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.print.PrintHelper
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.VietMapGL

class PrintMapActivity : AppCompatActivity() ,VietMapGL.SnapshotReadyCallback{
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_map)
mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{
            this.vietMapGL = it
            vietMapGL.setStyle(VietMapTiles.instance.lightVector())
        }
        val fab = findViewById<View>(R.id.fab)
        fab?.setOnClickListener{
            view:View? ->

            if (vietMapGL != null && vietMapGL.style != null) {
                vietMapGL.snapshot(this)
            }
        }
    }

    override fun onSnapshotReady(p0: Bitmap) {
        val photoPrinter = PrintHelper(this)
        photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FIT
        photoPrinter.printBitmap("vietmap.jpg - vietmap print map", p0)
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