package vn.vietmap.mapsdkdemo.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.IconUtils
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.Vietmap
import vn.vietmap.vietmapsdk.annotations.Marker
import vn.vietmap.vietmapsdk.annotations.MarkerOptions
import vn.vietmap.vietmapsdk.annotations.Polygon
import vn.vietmap.vietmapsdk.annotations.PolygonOptions
import vn.vietmap.vietmapsdk.annotations.Polyline
import vn.vietmap.vietmapsdk.annotations.PolylineOptions
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.location.LocationComponent
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.engine.LocationEngine
import vn.vietmap.vietmapsdk.location.engine.LocationEngineDefault
import vn.vietmap.vietmapsdk.location.modes.CameraMode
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.style.expressions.Expression
import vn.vietmap.vietmapsdk.style.layers.LineLayer
import vn.vietmap.vietmapsdk.style.layers.PropertyValue
import vn.vietmap.vietmapsdk.style.layers.RasterLayer


class VietmapScreen : AppCompatActivity(), VietMapGL.OnMapClickListener {
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL

    private var polylines: MutableList<Polyline>? = null
    private var polylineOptions: ArrayList<PolylineOptions?>? = ArrayList()
    private var polygon: Polygon? = null
    private var locationComponent: LocationComponent? = null
    private var locationEngine: LocationEngine? = null
    private var style: Style? = null
    private var isVector = false

    companion object {
        private const val STATE_POLYLINE_OPTIONS = "polylineOptions"
        private val HOCHIMINH = LatLng(10.791257, 106.669189)
        private val NINHTHUAN = LatLng(11.550254, 108.960579)
        private val DANANG = LatLng(16.045746, 108.202241)
        private val HUE = LatLng(16.469602, 107.577462)
        private val NGHEAN = LatLng(18.932151, 105.577207)
        private val HANOI = LatLng(21.024696, 105.833099)
    }

    private val allPolylines: List<PolylineOptions?>
        private get() {
            val options: MutableList<PolylineOptions?> = ArrayList()
            options.add(generatePolyline(HOCHIMINH, NINHTHUAN, "#F44336"))
            options.add(generatePolyline(NINHTHUAN, DANANG, "#FF5722"))
            options.add(generatePolyline(DANANG, HUE, "#673AB7"))
            options.add(generatePolyline(HUE, NGHEAN, "#009688"))
            options.add(generatePolyline(NGHEAN, HANOI, "#795548"))
            return options
        }

    /// Define list of lat lng points to draw a polygon
    val STAR_SHAPE_POINTS: ArrayList<LatLng?> = object : ArrayList<LatLng?>() {
        init {
            add(LatLng(10.791257, 106.669189))
            add(LatLng(11.550254, 108.960579))
            add(LatLng(16.045746, 108.202241))
            add(LatLng(16.469602, 107.577462))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Vietmap.getInstance(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vietmap)
        mapView = findViewById(R.id.vmMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { vietMapGL: VietMapGL ->
            this.vietMapGL = vietMapGL

            /// Add vietmap vector style to vietmapSDK
            vietMapGL.setStyle(
                Style.Builder()
                    .fromUri(VietMapTiles.instance.lightVector())
            ) {
                style = it
                initLocationEngine()
                enableLocationComponent(it)
                it.addLayerBelow(RasterLayer("raster", "raster_id"), "vmadmin_province")
            }

            vietMapGL.setOnPolylineClickListener { polyline: Polyline ->
                Toast.makeText(
                    this,
                    "You clicked on polyline with id = " + polyline.id,
                    Toast.LENGTH_SHORT
                ).show()
            }
            addPolygon()
            addPolyline()
            addLineLayer("LayerTest", "1234")
            vietMapGL.addOnMapClickListener(this)
            val removeLine = findViewById<Button>(R.id.removeLine)
            val addLineButton = findViewById<Button>(R.id.button)
            val changeTileButton = findViewById<Button>(R.id.changeTile)
            removeLine.setOnClickListener {
            }
            addLineButton.setOnClickListener {
                addPolyline()
            }
            changeTileButton.setOnClickListener {
                if (isVector) {
                    vietMapGL?.setStyle("https://maps.vietmap.vn/api/maps/google/styles.json?apikey=YOUR_API_KEY_HERE")
                    isVector = false
                } else {
                    isVector = true
                    vietMapGL?.setStyle("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=YOUR_API_KEY_HERE")

                }
            }
        }

    }

    private fun addMarker(position: LatLng): Marker {
        return vietMapGL!!.addMarker(
            MarkerOptions()
                .position(position)
                .title("Vietmap")
                .snippet("Vietmap Android SDK")
                .icon(
                    IconUtils().drawableToIcon(
                        this,
                        R.drawable.ic_launcher_foreground,
                        ResourcesCompat.getColor(resources, R.color.black, theme)
                    )
                )
        )
    }
    /// Define all polylines to add to the map

    private fun generatePolyline(start: LatLng, end: LatLng, color: String): PolylineOptions {
        val line = PolylineOptions()
        line.add(start)
        line.add(end)
        line.color(Color.parseColor(color))
        return line
    }

    /// Add all polylines to the map
    private fun addPolyline() {
        /// Add all polylines to the map
        polylineOptions!!.addAll(allPolylines)
        polylines = vietMapGL.addPolylines(polylineOptions!!)
        ///Add below line to implement on polyline click listener
        vietMapGL.setOnPolylineClickListener { polyline: Polyline ->
            Toast.makeText(
                this,
                "You clicked on polyline with id = " + polyline.id,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addLineLayer(
        layerName: String,
        sourceName: String,
        belowLayerId: String? = null,
        sourceLayer: String? = null,
        minZoom: Float? = null,
        maxZoom: Float? = null,
        properties: Array<PropertyValue<*>>? = null,
        filter: Expression? = null
    ) {
        val lineLayer = LineLayer(layerName, sourceName)
        if (properties != null) {
            lineLayer.setProperties(*properties)
        }
        if (sourceLayer != null) {
            lineLayer.setSourceLayer(sourceLayer)
        }
        if (minZoom != null) {
            lineLayer.minZoom = minZoom
        }
        if (maxZoom != null) {
            lineLayer.maxZoom = maxZoom
        }
        if (filter != null) {
            lineLayer.setFilter(filter)
        }
        if (belowLayerId != null) {
            style?.addLayerBelow(lineLayer, belowLayerId)
        } else {
            style?.addLayerBelow(lineLayer, "vmadmin_province")
        }
    }


    /// Add a polygon to the map

    private fun addPolygon() {
        /// Add a polygon to the map
        polygon = vietMapGL.addPolygon(
            PolygonOptions()
                .addAll(STAR_SHAPE_POINTS)
                .fillColor(Color.parseColor("#3bb2d0"))
        )
        /// Add below line to implement on polygon click listener
        vietMapGL.setOnPolygonClickListener { polygon: Polygon ->
            Toast.makeText(
                this,
                "You clicked on polygon with id = " + polygon.id,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMapClick(latLng: LatLng): Boolean {
        addMarker(latLng)
        return false
    }

    private fun enableLocationComponent(style: Style?) {
        locationComponent = vietMapGL!!.locationComponent
        if (locationComponent != null) {
            locationComponent!!.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    this, style!!
                ).build()
            )
            if (!checkPermission()) {
                return
            }
            locationComponent!!.setCameraMode(
                CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 0.0, 0.0, null
            )
            locationComponent!!.isLocationComponentEnabled = true
            locationComponent!!.zoomWhileTracking(19.0)
            locationComponent!!.renderMode = RenderMode.GPS
            locationComponent!!.locationEngine = locationEngine
        }
        updateMyLocationTrackingMode()
        updateMyLocationRenderMode()
    }

    private fun updateMyLocationTrackingMode() {
        val vietmapTrackingMode = intArrayOf(
            CameraMode.NONE,
            CameraMode.TRACKING,
            CameraMode.TRACKING_COMPASS,
            CameraMode.TRACKING_GPS
        )
        locationComponent!!.cameraMode = vietmapTrackingMode[0]
    }

    private fun updateMyLocationRenderMode() {
        val vietmapRenderModes = intArrayOf(RenderMode.NORMAL, RenderMode.COMPASS, RenderMode.GPS)
        locationComponent!!.renderMode = vietmapRenderModes[0]
    }

    /// check the permission is granted or not before using location
    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this, ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun initLocationEngine() {
        val locationEngineDefault = LocationEngineDefault
        locationEngine = locationEngineDefault.getDefaultLocationEngine(this)
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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}