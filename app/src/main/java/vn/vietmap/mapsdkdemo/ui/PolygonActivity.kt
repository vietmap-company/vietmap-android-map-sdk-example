package vn.vietmap.mapsdkdemo.ui

import android.graphics.Camera
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.Polygon
import vn.vietmap.vietmapsdk.annotations.PolygonOptions
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.maps.VietMapGLOptions
import java.util.ArrayList

class PolygonActivity : AppCompatActivity() , OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var polygon: Polygon? = null
    private var fullAlpha = true
    private var polygonIsVisible = true
    private var color = true
    private var allPoints = true
    private var holes = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_polygon)
        val options = VietMapGLOptions.createFromAttributes(this, null)
            .attributionTintColor(Config.RED_COLOR)
            .compassFadesWhenFacingNorth(false)
            .camera(CameraPosition.Builder().target(LatLng(21.522585, 105.685699)).zoom(11.0).build())
        mapView = MapView(this, options)
        mapView.id = R.id.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setContentView(mapView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_id_alpha -> {
                fullAlpha = !fullAlpha
                polygon!!.alpha =
                    if (fullAlpha) Config.FULL_ALPHA else Config.PARTIAL_ALPHA
                true
            }
            R.id.action_id_visible -> {
                polygonIsVisible = !polygonIsVisible
                polygon!!.alpha =
                    if (polygonIsVisible) if (fullAlpha) Config.FULL_ALPHA else Config.PARTIAL_ALPHA else Config.NO_ALPHA
                true
            }
            R.id.action_id_points -> {
                allPoints = !allPoints
                polygon!!.points =
                    if (allPoints) Config.STAR_SHAPE_POINTS else Config.BROKEN_SHAPE_POINTS
                true
            }
            R.id.action_id_color -> {
                color = !color
                polygon!!.fillColor =
                    if (color) Config.BLUE_COLOR else Config.RED_COLOR
                true
            }
            R.id.action_id_holes -> {
                holes = !holes
                polygon!!.holes =
                    if (holes) Config.STAR_SHAPE_HOLES else emptyList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_polygon, menu)
        return true
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
    internal object Config {
        val BLUE_COLOR = Color.parseColor("#2A5DFF")
        val RED_COLOR = Color.parseColor("#FF0000")
        const val FULL_ALPHA = 1.0f
        const val PARTIAL_ALPHA = 0.5f
        const val NO_ALPHA = 0.0f
        val STAR_SHAPE_POINTS: ArrayList<LatLng?> = object : ArrayList<LatLng?>() {
            init {
                add(LatLng(21.522585, 105.685699))
                add(LatLng(21.534611, 105.708873))
                add(LatLng(21.530883, 105.678833))
                add(LatLng(21.547115, 105.667503))
                add(LatLng(21.530643, 105.660121))
                add(LatLng(21.533529, 105.636260))
                add(LatLng(21.521743, 105.659091))
                add(LatLng(21.510677, 105.648792))
                add(LatLng(21.515008, 105.664070))
                add(LatLng(21.502496, 105.669048))
                add(LatLng(21.515369, 105.678489))
                add(LatLng(21.506346, 105.702007))
                add(LatLng(21.522585, 105.685699))
            }
        }
        val BROKEN_SHAPE_POINTS = STAR_SHAPE_POINTS.subList(0, STAR_SHAPE_POINTS.size - 3)
        val STAR_SHAPE_HOLES: ArrayList<List<LatLng?>?> = object : ArrayList<List<LatLng?>?>() {
            init {
                add(
                    ArrayList<LatLng>(object : ArrayList<LatLng?>() {
                        init {
                            add(LatLng(21.521743, 105.669091))
                            add(LatLng(21.530483, 105.676833))
                            add(LatLng(21.520483, 105.676833))
                            add(LatLng(21.521743, 105.669091))
                        }
                    })
                )
                add(
                    ArrayList<LatLng>(object : ArrayList<LatLng?>() {
                        init {
                            add(LatLng(21.529743, 105.662791))
                            add(LatLng(21.525543, 105.662791))
                            add(LatLng(21.525543, 105.660))
                            add(LatLng(21.527743, 105.660))
                            add(LatLng(21.529743, 105.662791))
                        }
                    })
                )
            }
        }
    }

    override fun onMapReady(p0: VietMapGL) {
        vietMapGL = p0
        vietMapGL.setStyle(VietMapTiles.instance.lightVector())
        vietMapGL.setOnPolygonClickListener { polygon : Polygon ->
            Toast.makeText(this,
                "You clicked on polygon with id = " + polygon.id,
                Toast.LENGTH_SHORT).show()
        }
        polygon = vietMapGL.addPolygon(
            PolygonOptions()
                .addAll(Config.STAR_SHAPE_POINTS)
                .fillColor(Config.BLUE_COLOR)
        )
    }
}