package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.Polyline
import vn.vietmap.vietmapsdk.annotations.PolylineOptions
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import java.util.Collections

class PolylineActivity : AppCompatActivity() {
    private var polylines: MutableList<Polyline>? = null
    private var polylineOptions: ArrayList<PolylineOptions>? = ArrayList()
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var fullAlpha = true
    private var showPolylines = true
    private var width = true
    private var color = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polyline2)
        mapView = findViewById(R.id.mapViewPolyline)
            if (savedInstanceState != null) {
            polylineOptions = savedInstanceState.getParcelableArrayList(STATE_POLYLINE_OPTIONS)
        } else {
            val tempPolyline = (allPolylines)
            polylineOptions!!.addAll(tempPolyline as Collection<PolylineOptions>)
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(
            OnMapReadyCallback { vietMapGL: VietMapGL ->
                this.vietMapGL = vietMapGL
                vietMapGL.setStyle(VietMapTiles.instance.lightVector())
                vietMapGL.setOnPolylineClickListener { polyline: Polyline ->
                    Toast.makeText(
                        this,
                        "You clicked on polyline with id = " + polyline.id,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                polylines = vietMapGL.addPolylines(polylineOptions!!)
            }
        )
        val fab = findViewById<View>(R.id.fab)
        fab?.setOnClickListener { view: View? ->
            if (vietMapGL != null) {
                if (polylines != null && polylines!!.size > 0) {
                    if (polylines!!.size == 1) {
                        // test for removing annotation
                        vietMapGL.removeAnnotation(polylines!![0])
                    } else {
                        // test for removing annotations
                        vietMapGL.removeAnnotations(polylines!!)
                    }
                }
                polylineOptions!!.clear()
                polylineOptions!!.addAll(randomLine as Collection<PolylineOptions>)
                polylines = vietMapGL.addPolylines(polylineOptions!!)
            }
        }
    }

    private val allPolylines: List<PolylineOptions?>
        private get() {
            val options: MutableList<PolylineOptions?> = java.util.ArrayList()
            options.add(generatePolyline(HOCHIMINH, NINHTHUAN, "#F44336"))
            options.add(generatePolyline(NINHTHUAN, DANANG, "#FF5722"))
            options.add(generatePolyline(DANANG, HUE, "#673AB7"))
            options.add(generatePolyline(HUE, NGHEAN, "#009688"))
            options.add(generatePolyline(NGHEAN, HANOI, "#795548"))
            options.add(generatePolyline(HANOI, HOCHIMINH, "#3F51B5"))
            return options
        }

    private fun generatePolyline(start: LatLng, end: LatLng, color: String): PolylineOptions {
        val line = PolylineOptions()
        line.add(start)
        line.add(end)
        line.color(Color.parseColor(color))
        return line
    }

    val randomLine: List<PolylineOptions?>
        get() {
            val randomLines = allPolylines
            Collections.shuffle(randomLines)
            return object : java.util.ArrayList<PolylineOptions?>() {
                init {
                    add(randomLines[0])
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
//
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STATE_POLYLINE_OPTIONS, polylineOptions)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_polyline, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (polylines!!.size <= 0) {
            Toast.makeText(this, "No polylines on map", Toast.LENGTH_LONG).show()
            return super.onOptionsItemSelected(item)
        }
        return when (item.itemId) {
            R.id.action_id_remove -> {
                // test to remove all annotations
                polylineOptions!!.clear()
                vietMapGL.clear()
                polylines!!.clear()
                true
            }
            R.id.action_id_alpha -> {
                fullAlpha = !fullAlpha
                for (p in polylines!!) {
                    p.alpha = if (fullAlpha) FULL_ALPHA else PARTIAL_ALPHA
                }
                true
            }
            R.id.action_id_color -> {
                color = !color
                for (p in polylines!!) {
                    p.color = if (color) Color.RED else Color.BLUE
                }
                true
            }
            R.id.action_id_width -> {
                width = !width
                for (p in polylines!!) {
                    p.width = if (width) 3.0f else 5.0f
                }
                true
            }
            R.id.action_id_visible -> {
                showPolylines = !showPolylines
                for (p in polylines!!) {
                    p.alpha =
                        if (showPolylines) (if (fullAlpha) FULL_ALPHA else PARTIAL_ALPHA) else NO_ALPHA
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val STATE_POLYLINE_OPTIONS = "polylineOptions"
        private val HOCHIMINH = LatLng(10.791257, 106.669189)
        private val NINHTHUAN = LatLng(11.550254, 108.960579)
        private val DANANG = LatLng(16.045746, 108.202241)
        private val HUE = LatLng(16.469602, 107.577462)
        private val NGHEAN = LatLng(18.932151, 105.577207)
        private val HANOI = LatLng(21.024696, 105.833099)
        private const val FULL_ALPHA = 1.0f
        private const val PARTIAL_ALPHA = 0.5f
        private const val NO_ALPHA = 0.0f
    }
}
