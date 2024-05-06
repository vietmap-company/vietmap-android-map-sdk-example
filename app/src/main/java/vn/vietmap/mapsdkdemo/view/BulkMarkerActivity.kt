package vn.vietmap.mapsdkdemo.view

import android.app.ProgressDialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.MenuItemCompat
import timber.log.Timber
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.GeoParseUtil
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.annotations.MarkerOptions
import vn.vietmap.vietmapsdk.camera.CameraUpdate
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.VietMapGL
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import kotlin.random.Random

class BulkMarkerActivity : AppCompatActivity(), OnItemSelectedListener {
    private lateinit var vietMapGL: VietMapGL
    private lateinit var mapView: MapView
    private var locations: List<LatLng>? = null

    private var markerList: ArrayList<MarkerOptions>? = ArrayList()
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulk_marker)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { vietMapGL ->
            this.vietMapGL = vietMapGL
            vietMapGL.setStyle(VietMapTiles.instance.lightVector()) { style ->
                vietMapGL.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(18.764211, 105.609802), 7.0
                    )
                )
            }
            vietMapGL.addOnMapLongClickListener { point: LatLng ->
                addMarker(point)
                false
            }
            vietMapGL.addOnMapClickListener { point: LatLng ->
                addMarker(point)
                false
            }
            if (savedInstanceState != null) {
                markerList = savedInstanceState.getParcelableArrayList(STATE_MARKER_LIST)
                if (markerList != null) {
                    vietMapGL.addMarkers(markerList!!)
                }
            }
        }
    }

    private fun addMarker(point: LatLng) {
        val pixel = vietMapGL.projection.toScreenLocation(point)
        val title = (
                LAT_LON_FORMATTER.format(point.latitude) + ", " +
                        LAT_LON_FORMATTER.format(point.longitude)
                )
        val snippet = "X = " + pixel.x.toInt() + ", Y = " + pixel.y.toInt()
        val marker = MarkerOptions()
            .position(point)
            .title(title)
            .snippet(snippet)
        markerList!!.add(marker)
        vietMapGL.addMarker(marker)
    }
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val amount = Integer.valueOf(resources.getStringArray(R.array.bulk_marker_list)[p2])
        if (locations == null) {
            progressDialog = ProgressDialog.show(this, "Loading", "Fetching markers", false)
            LoadLocationTask(this, amount).execute()
        } else {
            showMarkers(amount)
        }
    }

    private fun onLatLngListLoaded(latLngs: List<LatLng>?, amount: Int) {
        progressDialog?.hide()
        locations = latLngs
        showMarkers(amount)
    }

    private fun showMarkers(amount: Int) {
        var amount = amount
        if (vietMapGL == null || locations == null || mapView.isDestroyed) {
            return
        }
        vietMapGL.removeAnnotations()
        if (locations!!.size < amount) {
            amount = locations!!.size
        }
        showGLMarkers(amount)
    }

    private fun showGLMarkers(amount: Int) {
        val markerOptionsList: MutableList<MarkerOptions> = ArrayList()
        val formatter = DecimalFormat("#.####")
        val random = java.util.Random()
        var randomIndex: Int

        for (i in 0 until amount) {
            randomIndex = random.nextInt(locations!!.size)
            var latLng = locations!![randomIndex]
            markerOptionsList.add(
                MarkerOptions().position(latLng).title(i.toString())
                    .snippet(formatter.format(latLng.latitude) + "`, " + formatter.format(latLng.longitude))
            )
        }
        vietMapGL.addMarkers(markerOptionsList)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
//        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.bulk_marker_list,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        menuInflater.inflate(R.menu.menu_bulk_marker, menu)
        val item = menu.findItem(R.id.spinner)
        val spinner = MenuItemCompat.getActionView(item) as Spinner
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = this@BulkMarkerActivity
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
        outState.putParcelableArrayList(STATE_MARKER_LIST, markerList)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private class LoadLocationTask constructor(
        activity: BulkMarkerActivity,
        private val amount: Int
    ) :
        AsyncTask<Void?, Int?, List<LatLng>?>() {
        private val activity: WeakReference<BulkMarkerActivity>
        override fun doInBackground(vararg p0: Void?): List<LatLng>? {
            val activity = activity.get()
            if (activity != null) {
                var json: String? = null
                try {
                    json = GeoParseUtil.loadStringFromAssets(
                        activity.applicationContext,
                        "points.geojson"
                    )
                } catch (exception: IOException) {
                    Timber.e(exception, "Error while add caption")
                }
                if (json != null) {
                    return GeoParseUtil.parseGeoJsonCoordinates(json)
                }
            }
            return null
        }

        override fun onPostExecute(result: List<LatLng>?) {
            super.onPostExecute(result)
            val activity = activity.get()
            activity?.onLatLngListLoaded(result, amount)
        }

        init {
            this.activity = WeakReference(activity)
        }
    }

    companion object {
        private val LAT_LON_FORMATTER = DecimalFormat("#.#####")
        private const val STATE_MARKER_LIST = "markerList"
    }
}