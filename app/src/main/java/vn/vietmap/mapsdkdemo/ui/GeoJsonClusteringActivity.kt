package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.mapbox.geojson.Feature
import timber.log.Timber
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.style.expressions.Expression
import vn.vietmap.vietmapsdk.style.layers.CircleLayer
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory
import vn.vietmap.vietmapsdk.style.layers.SymbolLayer
import vn.vietmap.vietmapsdk.style.sources.GeoJsonOptions
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource
import vn.vietmap.vietmapsdk.utils.BitmapUtils
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
enum class ClickOption(val selectedOption: Int) {
    /// Clicking a cluster will zoom to the level where it dissolves
    ZOOM_TO_CLUSTER(0),
    /// Clicking a cluster will show the details of the cluster children"
    SHOW_CLUSTER_DETAIL(1),
    /// Clicking a cluster will show the details of the cluster leaves with an offset and limit
    SHOW_CLUSTER_DETAIL_AND_OFFSET(2)
}
class GeoJsonClusteringActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private var clusterSource: GeoJsonSource? = null
    private var clickOptionCounter:ClickOption = ClickOption.ZOOM_TO_CLUSTER
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_json_clustering)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            if (map != null) {
                vietMapGL = map
            }
            vietMapGL.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(37.7749, 122.4194),
                    0.0
                )
            )
            val clusterLayers = arrayOf(
                intArrayOf(
                    150,
                    ResourcesCompat.getColor(
                        resources,
                        R.color.redAccent,
                        theme
                    )
                ),
                intArrayOf(20, ResourcesCompat.getColor(resources, R.color.greenAccent, theme)),
                intArrayOf(
                    0,
                    ResourcesCompat.getColor(
                        resources,
                        R.color.blueAccent,
                        theme
                    )
                )
            )
            try {
                vietMapGL.setStyle(
                    Style.Builder()
                        .fromUri(VietMapTiles.instance.lightVector())
                        .withSource(createClusterSource().also { clusterSource = it })
                        .withLayer(createSymbolLayer())
                        .withLayer(createClusterLevelLayer(0, clusterLayers))
                        .withLayer(createClusterLevelLayer(1, clusterLayers))
                        .withLayer(createClusterLevelLayer(2, clusterLayers))
                        .withLayer(createClusterTextLayer())
                        .withImage(
                            "icon-id",
                            Objects.requireNonNull(
                                BitmapUtils.getBitmapFromDrawable(
                                    ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.ic_stars,
                                        null
                                    )
                                )
                            )!!,
                            true
                        )
                )
            } catch (exception: URISyntaxException) {
                Timber.e(exception)
            }
            vietMapGL.addOnMapClickListener { latLng: LatLng? ->
                val point = vietMapGL.projection.toScreenLocation(latLng!!)
                val features =
                    vietMapGL.queryRenderedFeatures(
                        point,
                        "cluster-0",
                        "cluster-1",
                        "cluster-2"
                    )
                if (features.isNotEmpty()) {
                    onClusterClick(features[0], Point(point.x.toInt(), point.y.toInt()))
                }
                true
            }

        }
        findViewById<View>(R.id.fab).setOnClickListener() {

            updateClickOptionCounter()
            notifyClickOptionUpdate()
        }
    }

    private fun onClusterClick(cluster: Feature, clickPoint: Point) {
        if (clickOptionCounter == ClickOption.ZOOM_TO_CLUSTER) {
            val nextZoomLevel = clusterSource!!.getClusterExpansionZoom(cluster).toDouble()
            val zoomDelta = nextZoomLevel - vietMapGL.cameraPosition.zoom
            vietMapGL.animateCamera(
                CameraUpdateFactory.zoomBy(
                    zoomDelta + CAMERA_ZOOM_DELTA,
                    clickPoint
                )
            )
            Toast.makeText(this, "Zooming to $nextZoomLevel", Toast.LENGTH_SHORT).show()
        } else if (clickOptionCounter == ClickOption.SHOW_CLUSTER_DETAIL) {
            val collection = clusterSource!!.getClusterChildren(cluster)
            Toast.makeText(this, "Children: " + collection.toJson(), Toast.LENGTH_SHORT).show()
        } else {
            val collection = clusterSource!!.getClusterLeaves(cluster, 2, 1)
            Toast.makeText(this, "Leaves: " + collection.toJson(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createClusterSource(): GeoJsonSource {
        return GeoJsonSource(
            "earthquakes",
            URI("asset://earthquakes.geojson"),
            GeoJsonOptions()
                .withCluster(true)
                .withClusterMaxZoom(14)
                .withClusterRadius(50)
                .withClusterProperty(
                    "max",
                    Expression.max(
                        Expression.accumulated(), Expression.get("max")
                    ),
                    Expression.get("mag")
                )
                .withClusterProperty("sum", Expression.literal("+"), Expression.get("mag"))
                .withClusterProperty(
                    "felt",
                    Expression.literal("any"),
                    Expression.neq(
                        Expression.get("felt"), Expression.literal("null")
                    )
                )
        )
    }

    private fun createSymbolLayer(): SymbolLayer {
        return SymbolLayer(
            "unclustered-points",
            "earthquakes"
        )
            .withProperties(
                PropertyFactory.iconImage("icon-id"),
                PropertyFactory.iconSize(
                    Expression.division(
                        Expression.get("mag"),
                        Expression.literal(4.0f)
                    )
                ),
                PropertyFactory.iconColor(
                    Expression.interpolate(
                        Expression.exponential(1),
                        Expression.get("mag"),
                        Expression.stop(2.0, Expression.rgb(0, 255, 0)),
                        Expression.stop(4.5, Expression.rgb(0, 0, 255)),
                        Expression.stop(7.0, Expression.rgb(255, 0, 0))
                    )
                )
            )
            .withFilter(Expression.has("mag"))
    }

    private fun createClusterLevelLayer(level: Int, layerColors: Array<IntArray>): CircleLayer {
        val circles = CircleLayer(
            "cluster-$level",
            "earthquakes"
        )
        circles.setProperties(
            PropertyFactory.circleColor(layerColors[level][1]),
            PropertyFactory.circleRadius(18f)
        )
        val pointCount = Expression.toNumber(
            Expression.get("point_count")
        )
        circles.setFilter(
            if (level == 0) {
                Expression.all(
                    Expression.has("point_count"),
                    Expression.gte(
                        pointCount,
                        Expression.literal(layerColors[level][0])
                    )
                )
            } else {
                Expression.all(
                    Expression.has("point_count"),
                    Expression.gt(
                        pointCount,
                        Expression.literal(layerColors[level][0])
                    ),
                    Expression.lt(
                        pointCount,
                        Expression.literal(layerColors[level - 1][0])
                    )
                )
            }
        )
        return circles
    }

    private fun createClusterTextLayer(): SymbolLayer {
        return SymbolLayer(
            "property",
            "earthquakes"
        )
            .withProperties(
                PropertyFactory.textField(
                    Expression.concat(
                        Expression.get("point_count"),
                        Expression.literal(", "),
                        Expression.get("max")
                    )
                ),
                PropertyFactory.textSize(12f),
                PropertyFactory.textColor(Color.WHITE),
                PropertyFactory.textIgnorePlacement(true),
                PropertyFactory.textAllowOverlap(true)
            )
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateClickOptionCounter() {
        if (clickOptionCounter == ClickOption.SHOW_CLUSTER_DETAIL_AND_OFFSET) {
            clickOptionCounter = ClickOption.ZOOM_TO_CLUSTER
        } else if(clickOptionCounter == ClickOption.ZOOM_TO_CLUSTER){
            clickOptionCounter = ClickOption.SHOW_CLUSTER_DETAIL
        }else{
            clickOptionCounter = ClickOption.ZOOM_TO_CLUSTER
        }
    }

    private fun notifyClickOptionUpdate() {
        if (clickOptionCounter == ClickOption.ZOOM_TO_CLUSTER) {
            Toast.makeText(
                this@GeoJsonClusteringActivity,
                "Clicking a cluster will zoom to the level where it dissolves",
                Toast.LENGTH_SHORT
            ).show()
        } else if (clickOptionCounter == ClickOption.SHOW_CLUSTER_DETAIL) {
            Toast.makeText(
                this@GeoJsonClusteringActivity,
                "Clicking a cluster will show the details of the cluster children",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this@GeoJsonClusteringActivity,
                "Clicking a cluster will show the details of the cluster leaves with an offset and limit",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val CAMERA_ZOOM_DELTA = 0.01
    }
}