package vn.vietmap.mapsdkdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapFragment
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.SupportMapFragment
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.maps.VietMapGLOptions

class MapFragmentActivity : AppCompatActivity(),
    MapFragment.OnMapViewReadyCallback,
    OnMapReadyCallback,
    MapView.OnDidFinishRenderingFrameListener {
    private lateinit var vietMapGL: VietMapGL
    private lateinit var mapView: MapView
    private var initialCameraAnimation = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_fragment)
        val mapFragment: SupportMapFragment?
        if (savedInstanceState == null) {
            mapFragment = SupportMapFragment.newInstance(createFragmentOptions())
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, mapFragment, TAG)
                .commit()
        } else {
            mapFragment = supportFragmentManager.findFragmentByTag(TAG) as SupportMapFragment?
        }
        mapFragment!!.getMapAsync(this)
    }


    private fun createFragmentOptions(): VietMapGLOptions {
        val options = VietMapGLOptions.createFromAttributes(this, null)
//        options.scrollGesturesEnabled(false)
//        options.zoomGesturesEnabled(false)
//        options.tiltGesturesEnabled(false)
//        options.rotateGesturesEnabled(false)
        options.debugActive(false)
        val dc = LatLng(16.90252,105.02291)
        options.minZoomPreference(9.0)
        options.maxZoomPreference(11.0)
        options.camera(
            CameraPosition.Builder()
                .target(dc)
                .zoom(11.0)
                .build()
        )
        return options
    }

    override fun onMapViewReady(map: MapView) {
        mapView = map
        mapView.addOnDidFinishRenderingFrameListener(this)
    }

    override fun onMapReady(map: VietMapGL) {
        vietMapGL = map
        vietMapGL.setStyle(VietMapTiles.instance.lightVector())
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.removeOnDidFinishRenderingFrameListener(this)
    }

    override fun onDidFinishRenderingFrame(fully: Boolean) {
        if (initialCameraAnimation && fully && vietMapGL != null) {
            vietMapGL.animateCamera(
                CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().tilt(45.0).build()),
                5000
            )
            initialCameraAnimation = false
        }
    }

    companion object {
        private const val TAG = "vn.vietmap.map"
    }
}