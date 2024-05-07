package vn.vietmap.mapsdkdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import vn.vietmap.mapsdkdemo.ui.AnimatedSymbolLayerActivity
import vn.vietmap.mapsdkdemo.ui.AnimatorAnimationActivity
import vn.vietmap.mapsdkdemo.ui.BuildingFillExtrusionActivity
import vn.vietmap.mapsdkdemo.ui.CameraAnimationTypes
import vn.vietmap.mapsdkdemo.ui.CountFeatureInBoxActivity
import vn.vietmap.mapsdkdemo.ui.CustomDynamicInfoWindowActivity
import vn.vietmap.mapsdkdemo.ui.CustomInfoWindowActivity
import vn.vietmap.mapsdkdemo.ui.DemoActivity
import vn.vietmap.mapsdkdemo.ui.DraggableMarkerActivity
import vn.vietmap.mapsdkdemo.ui.DynamicMarkerChangeActivity
import vn.vietmap.mapsdkdemo.ui.GeoJsonClusteringActivity
import vn.vietmap.mapsdkdemo.ui.GradientPolylineActivity
import vn.vietmap.mapsdkdemo.ui.HeatMapLayerActivity
import vn.vietmap.mapsdkdemo.ui.ManualLocationUpdatesActivity
import vn.vietmap.mapsdkdemo.ui.MapFragmentActivity
import vn.vietmap.mapsdkdemo.ui.MultiMapActivity
import vn.vietmap.mapsdkdemo.ui.PolygonActivity
import vn.vietmap.mapsdkdemo.ui.PolylineActivity
import vn.vietmap.mapsdkdemo.ui.PrintMapActivity
import vn.vietmap.mapsdkdemo.ui.RestrictCameraToBoundsActivity
import vn.vietmap.mapsdkdemo.ui.ShowUserLocationActivity
import vn.vietmap.mapsdkdemo.ui.SymbolLayerActivity
import vn.vietmap.mapsdkdemo.ui.VietMapGLSurfaceRecyclerView
import vn.vietmap.mapsdkdemo.ui.VietMapViewPager
import vn.vietmap.mapsdkdemo.ui.VietmapScreen
import vn.vietmap.mapsdkdemo.ui.theme.MyApplicationTheme
import vn.vietmap.mapsdkdemo.view.BulkMarkerActivity
import vn.vietmap.vietmapsdk.Vietmap
import vn.vietmap.vietmapsdk.location.permissions.PermissionsListener
import vn.vietmap.vietmapsdk.location.permissions.PermissionsManager
import vn.vietmap.vietmapsdk.maps.VietMapGL


class MainActivity : ComponentActivity() {

    private var permissionsManager:PermissionsManager?=null

    private fun checkPermissions() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
        } else {
            permissionsManager =
                PermissionsManager(
                    object :
                        PermissionsListener {
                        override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                            Toast.makeText(
                                applicationContext,
                                "You need to accept location permissions.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onPermissionResult(granted: Boolean) {
                            if (granted) {
                            } else {
                                finish()
                            }
                        }
                    })
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Vietmap.getInstance(this)
        checkPermissions()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {

                Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment =  Alignment.CenterHorizontally,
                    ) {
                    Text(text = "Show map")
                    Button(onClick = {
                        Intent(applicationContext, VietmapScreen::class.java).also {
                            startActivity(it)
                        }
                    }) {
                        Text(text = "Simple map")
                    }
                    Button(onClick = {
                        Intent(applicationContext, ShowUserLocationActivity::class.java).also {
                            startActivity(it)
                        }
                    }) {
                        Text(text = "Show user location with multiple tracking mode")
                    }
                    Button(onClick = {
                        Intent(applicationContext, DraggableMarkerActivity::class.java).also {
                            startActivity(it)
                        }
                    }) {
                        Text(text = "Draggable marker")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, VietMapViewPager::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                     Text(text = "Map nested view pager")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, VietMapGLSurfaceRecyclerView::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Vietmap GLSurface recycler view pager")
                    }
                    Text(text = "Map fragment")
                    Button(
                        onClick = {
                            Intent(applicationContext, MultiMapActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Multiple map on screen")
                    }

                    Button(
                        onClick = {
                            Intent(applicationContext, MapFragmentActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Map Fragment")
                    }

                    Text(text = "Annotations")
                    Button(
                        onClick = {
                            Intent(applicationContext, BulkMarkerActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Bulk marker activity")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, DynamicMarkerChangeActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Dynamic marker change activity")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, PolygonActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Polygon")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, PolylineActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Polyline")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, GradientPolylineActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Gradient Polyline")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, BuildingFillExtrusionActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "BuildingFillExtrusionActivity (Running with maptiler street style)")
                    }

                    Button(
                        onClick = {
                            Intent(applicationContext, AnimatedSymbolLayerActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Animated symbol activity")
                    }
                    Text(text = "Camera")

                    Button(
                        onClick = {
                            Intent(applicationContext, CameraAnimationTypes::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Animation Types")
                    }

                    Button(
                        onClick = {
                            Intent(applicationContext, AnimatorAnimationActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Animation Camera")
                    }
                    Text(text = "Query feature")
                    Button(
                        onClick = {
                            Intent(applicationContext, CountFeatureInBoxActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Query feature box")
                    }

                    Text(text = "Map Layer")
                    Button(
                        onClick = {
                            Intent(applicationContext, HeatMapLayerActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Heatmap layer")
                    }
                    Text(text = "Print a map")
                    Button(
                        onClick = {
                            Intent(applicationContext, PrintMapActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Print a map screenshot")
                    }

                    Text(text = "Restrict camera")
                    Button(
                        onClick = {
                            Intent(applicationContext, RestrictCameraToBoundsActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Restrict camera bound")
                    }
                    Text(text = "Building Fill Extrusion")
                    Button(
                        onClick = {
                            Intent(applicationContext, BuildingFillExtrusionActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Building Fill Extrusion")
                    }
                    Text(text = "InfoWindow")
                    Button(
                        onClick = {
                            Intent(applicationContext, CustomDynamicInfoWindowActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Custom DynamicInfoWindow")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, CustomInfoWindowActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Custom InfoWindow")
                    }
                    Text(text = "Clustering")
                    Button(
                        onClick = {
                            Intent(applicationContext, GeoJsonClusteringActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "GeoJsonClusteringActivity")
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, ManualLocationUpdatesActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "ManualLocationUpdatesActivity")
                    }

                    Text(text = "Demo Screen")

                    Button(
                        onClick = {
                            Intent(applicationContext, DemoActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    ){
                        Text(text = "Demo Activity")
                    }
                }
            }
        }
    }
}
