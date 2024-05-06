package vn.vietmap.mapsdkdemo.ui

import android.annotation.SuppressLint
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import vn.vietmap.android.gestures.AndroidGesturesManager
import vn.vietmap.android.gestures.MoveGestureDetector
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.databinding.ActivityDraggableMarkerBinding
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.IconFactory
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory
import vn.vietmap.vietmapsdk.style.layers.SymbolLayer
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource

class DraggableMarkerActivity : AppCompatActivity() {
    companion object {
        private const val sourceId = "source_draggable"
        private const val layerId = "layer_draggable"
        private const val markerImageId = "marker_icon_draggable"

        private var latestId: Long = 0
        fun generateMarkerId(): String {
            if (latestId == Long.MAX_VALUE) {
                throw RuntimeException("You've added too many markers.")
            }
            return latestId++.toString()
        }
    }

    private val actionBarHeight: Int by lazy {
        supportActionBar?.height ?: 0
    }

    private lateinit var binding: ActivityDraggableMarkerBinding
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    private val featureCollection = FeatureCollection.fromFeatures(mutableListOf())
    private val source = GeoJsonSource(sourceId, featureCollection)
    private val layer = SymbolLayer(
        layerId,
        sourceId
    )
        .withProperties(
            PropertyFactory.iconImage(markerImageId),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)
        )

    private var draggableSymbolsManager: DraggableSymbolsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draggable_marker)
        binding = ActivityDraggableMarkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Toast.makeText(applicationContext, "Click and hold the marker to move it", Toast.LENGTH_LONG)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { vmgl ->
            this.vietMapGL = vmgl

            vmgl.setStyle(
                Style.Builder()
                    .fromUri(VietMapTiles.instance.lightVector())
                    .withImage(markerImageId, IconFactory.getInstance(this).defaultMarker().bitmap)
                    .withSource(source)
                    .withLayer(layer)
            )

            // Add initial markers
            addMarker(LatLng(21.0283, 105.8538))
            addMarker(LatLng(15.999796, 108.192122))
            addMarker(LatLng(10.791285, 106.630474))

            // Initial camera position
            vmgl.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(15.999796, 108.192122),
                    3.0
                )
            )

            vmgl.addOnMapClickListener {
                // Adding a marker on map click
                val features = vmgl.queryRenderedSymbols(it, layerId)
                if (features.isEmpty()) {
                    addMarker(it)
                } else {
                    // Displaying marker info on marker click
                    Snackbar.make(
                        mapView,
                        "Marker's position: %.4f, %.4f".format(it.latitude, it.longitude),
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }

                false
            }

            draggableSymbolsManager = DraggableSymbolsManager(
                mapView,
                vmgl,
                featureCollection,
                source,
                layerId,
                actionBarHeight,
                0
            )

            // Adding symbol drag listeners
            draggableSymbolsManager?.addOnSymbolDragListener(object : DraggableSymbolsManager.OnSymbolDragListener {
                override fun onSymbolDragStarted(id: String) {
                    binding.draggedMarkerPositionTv.visibility = View.VISIBLE
                    Snackbar.make(
                        mapView,
                        "Marker drag started (%s)".format(id),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }

                @SuppressLint("SetTextI18n")
                override fun onSymbolDrag(id: String) {
                    val point = featureCollection.features()?.find {
                        it.id() == id
                    }?.geometry() as Point
                    binding.draggedMarkerPositionTv.text = "Dragged marker's position: %.4f, %.4f".format(point.latitude(), point.longitude())
                }

                override fun onSymbolDragFinished(id: String) {
                    binding.draggedMarkerPositionTv.visibility = View.GONE
                    val point = featureCollection.features()?.find {
                        it.id() == id
                    }?.geometry() as Point
                    Snackbar.make(
                        mapView,
                        "Marker drag finished (%s) at point %.4f, %.4f".format(id,point.latitude(), point.longitude()),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            })
        }
    }


    private fun addMarker(latLng: LatLng) {
        featureCollection.features()?.add(
            Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude), null, generateMarkerId())
        )
        source.setGeoJson(featureCollection)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Dispatching parent's touch events to the manager
        draggableSymbolsManager?.onParentTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
    class DraggableSymbolsManager(
        mapView: MapView,
        private val vietMapGL: VietMapGL,
        private val symbolsCollection: FeatureCollection,
        private val symbolsSource: GeoJsonSource,
        private val symbolsLayerId: String,
        private val touchAreaShiftY: Int = 0,
        private val touchAreaShiftX: Int = 0,
        private val touchAreaMaxX: Int = mapView.width,
        private val touchAreaMaxY: Int = mapView.height
    ) {

        private val androidGesturesManager: AndroidGesturesManager =
            AndroidGesturesManager(
                mapView.context,
                false
            )
        private var draggedSymbolId: String? = null
        private val onSymbolDragListeners: MutableList<OnSymbolDragListener> = mutableListOf<OnSymbolDragListener>()

        init {
            vietMapGL.addOnMapLongClickListener {
                // Starting the drag process on long click
                draggedSymbolId = vietMapGL.queryRenderedSymbols(it, symbolsLayerId).firstOrNull()?.id()?.also { id ->
                    vietMapGL.uiSettings.setAllGesturesEnabled(false)
                    vietMapGL.gesturesManager.moveGestureDetector.interrupt()
                    notifyOnSymbolDragListeners {
                        onSymbolDragStarted(id)
                    }
                }
                false
            }

            androidGesturesManager.setMoveGestureListener(MyMoveGestureListener())
        }

        inner class MyMoveGestureListener : MoveGestureDetector.OnMoveGestureListener {
            override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
                return true
            }

            override fun onMove(detector: MoveGestureDetector, distanceX: Float, distanceY: Float): Boolean {
                if (detector.pointersCount > 1) {
                    // Stopping the drag when we don't work with a simple, on-pointer move anymore
                    stopDragging()
                    return true
                }

                // Updating symbol's position
                draggedSymbolId?.also { draggedSymbolId ->
                    val moveObject = detector.getMoveObject(0)
                    val point = PointF(moveObject.currentX - touchAreaShiftX, moveObject.currentY - touchAreaShiftY)

                    if (point.x < 0 || point.y < 0 || point.x > touchAreaMaxX || point.y > touchAreaMaxY) {
                        stopDragging()
                    }

                    val latLng = vietMapGL.projection.fromScreenLocation(point)

                    symbolsCollection.features()?.indexOfFirst {
                        it.id() == draggedSymbolId
                    }?.also { index ->
                        symbolsCollection.features()?.get(index)?.also { oldFeature ->
                            val properties = oldFeature.properties()
                            val newFeature = Feature.fromGeometry(
                                Point.fromLngLat(latLng.longitude, latLng.latitude),
                                properties,
                                draggedSymbolId
                            )
                            symbolsCollection.features()?.set(index, newFeature)
                            symbolsSource.setGeoJson(symbolsCollection)
                            notifyOnSymbolDragListeners {
                                onSymbolDrag(draggedSymbolId)
                            }
                            return true
                        }
                    }
                }

                return false
            }

            override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) {
                // Stopping the drag when move ends
                stopDragging()
            }
        }

        private fun stopDragging() {
            vietMapGL.uiSettings.setAllGesturesEnabled(true)
            draggedSymbolId?.let {
                notifyOnSymbolDragListeners {
                    onSymbolDragFinished(it)
                }
            }
            draggedSymbolId = null
        }

        fun onParentTouchEvent(ev: MotionEvent?) {
            androidGesturesManager.onTouchEvent(ev)
        }

        private fun notifyOnSymbolDragListeners(action: OnSymbolDragListener.() -> Unit) {
            onSymbolDragListeners.forEach(action)
        }

        fun addOnSymbolDragListener(listener: OnSymbolDragListener) {
            onSymbolDragListeners.add(listener)
        }

        fun removeOnSymbolDragListener(listener: OnSymbolDragListener) {
            onSymbolDragListeners.remove(listener)
        }

        interface OnSymbolDragListener {
            fun onSymbolDragStarted(id: String)
            fun onSymbolDrag(id: String)
            fun onSymbolDragFinished(id: String)
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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.let {
            mapView.onSaveInstanceState(it)
        }
    }
}

private fun VietMapGL.queryRenderedSymbols(latLng: LatLng, layerId: String): List<Feature> {
    return this.queryRenderedFeatures(this.projection.toScreenLocation(latLng), layerId)
}