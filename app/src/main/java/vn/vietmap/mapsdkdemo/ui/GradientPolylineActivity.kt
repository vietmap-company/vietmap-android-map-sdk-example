package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.ResourceUtils
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.style.expressions.Expression
import vn.vietmap.vietmapsdk.style.layers.LineLayer
import vn.vietmap.vietmapsdk.style.layers.Property
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory
import vn.vietmap.vietmapsdk.style.layers.PropertyValue
import vn.vietmap.vietmapsdk.style.layers.TransitionOptions
import vn.vietmap.vietmapsdk.style.sources.GeoJsonOptions
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource

class GradientPolylineActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_polyline)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { vietMapGL ->

            val geoJson = ResourceUtils.readRawResource(
                applicationContext,
                R.raw.test_line_gradient_feature
            )

            val lineLayer =
                LineLayer("gradient", LINE_SOURCE).withProperties(

                    PropertyFactory.lineGradient(
                        Expression.interpolate(
                            Expression.linear(),
                            Expression.lineProgress(),
                            Expression.stop(0f, Expression.rgb(0, 0, 255)),
                            Expression.stop(0.25f, Expression.rgb(102, 153, 255)),
                            Expression.stop(0.5f, Expression.rgb(0, 255, 0)),
                            Expression.stop(0.75f, Expression.rgb(255, 153, 153)),
                            Expression.stop(1f, Expression.rgb(255, 0, 0))
                        )
                    ),
                    PropertyFactory.lineColor(Color.RED),
                    PropertyFactory.lineWidth(10.0f),
                    PropertyFactory.lineCap(
                        Property.LINE_CAP_ROUND
                    ),
                    PropertyFactory.lineJoin(
                        Property.LINE_JOIN_ROUND
                    )
                )
            lineLayer.lineColorTransition = TransitionOptions(10000, 5000)
            vietMapGL.setStyle(
                Style.Builder().fromUri(VietMapTiles.instance.lightVector()).withSource(
                    GeoJsonSource(LINE_SOURCE, geoJson, GeoJsonOptions().withLineMetrics(true))
                ).withLayerBelow(
                    lineLayer,
                    "vmadmin_province"
                )
            )
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

    companion object {
        const val LINE_SOURCE = "gradient"
    }
}