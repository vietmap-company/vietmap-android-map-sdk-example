package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.style.expressions.Expression
import vn.vietmap.vietmapsdk.style.layers.FillExtrusionLayer
import vn.vietmap.vietmapsdk.style.layers.Property
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory
import vn.vietmap.vietmapsdk.style.light.Light
import vn.vietmap.vietmapsdk.style.light.Position
import vn.vietmap.vietmapsdk.utils.ColorUtils
/// Pending
class BuildingFillExtrusionActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var light: Light? = null
    private var isMapAnchorLight = false
    private var isLowIntensityLight = false
    private var isRedColor = false
    private var isInitPosition = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_fill_extrusion)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(
            OnMapReadyCallback { map: VietMapGL? ->
                if (map != null) {
                    vietMapGL = map
                }
                vietMapGL.setStyle("https://api.maptiler.com/maps/basic-v2/style.json?key=erfJ8OKYfrgKdU6J1SXm") { style: Style ->
                    setupBuildings(style)
                    setupLight()
                }
            }
        )
    }
    private fun setupBuildings(style: Style) {
        val fillExtrusionLayer =
            FillExtrusionLayer(
                "3d-buildings",
                "composite"
            )
        fillExtrusionLayer.sourceLayer = "building"
        fillExtrusionLayer.setFilter(
            Expression.eq(
                Expression.get("extrude"),
                Expression.literal("true")
            )
        )
        fillExtrusionLayer.minZoom = 15f
        fillExtrusionLayer.setProperties(
            PropertyFactory.fillExtrusionColor(Color.LTGRAY),
            PropertyFactory.fillExtrusionHeight(
                Expression.get("height")),
            PropertyFactory.fillExtrusionBase(
                Expression.get("min_height")),
            PropertyFactory.fillExtrusionOpacity(0.9f)
        )
        style.addLayer(fillExtrusionLayer)
    }

    private fun setupLight() {
        light = vietMapGL.style!!.light
        findViewById<View>(R.id.fabLightPosition).setOnClickListener { v: View? ->
            isInitPosition = !isInitPosition
            if (isInitPosition) {
                light!!.position =
                    Position(1.5f, 90f, 80f)
            } else {
                light!!.position =
                    Position(1.15f, 210f, 30f)
            }
        }
        findViewById<View>(R.id.fabLightColor).setOnClickListener { v: View? ->
            isRedColor = !isRedColor
            light!!.color = ColorUtils.colorToRgbaString(if (isRedColor) Color.RED else Color.BLUE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_building, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (light != null) {
            val id = item.itemId
            if (id == R.id.menu_action_anchor) {
                isMapAnchorLight = !isMapAnchorLight
                light!!.anchor =
                    if (isMapAnchorLight) Property.ANCHOR_MAP else Property.ANCHOR_VIEWPORT
            } else if (id == R.id.menu_action_intensity) {
                isLowIntensityLight = !isLowIntensityLight
                light!!.intensity = if (isLowIntensityLight) 0.35f else 1.0f
            }
        }
        return super.onOptionsItemSelected(item)
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

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}