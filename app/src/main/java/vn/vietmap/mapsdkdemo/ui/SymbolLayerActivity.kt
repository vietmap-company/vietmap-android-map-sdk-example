package vn.vietmap.mapsdkdemo.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import timber.log.Timber
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.maps.VietMapGLOptions
import vn.vietmap.vietmapsdk.style.expressions.Expression
import vn.vietmap.vietmapsdk.style.layers.Property
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory
import vn.vietmap.vietmapsdk.style.layers.SymbolLayer
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource
import vn.vietmap.vietmapsdk.style.sources.Source
import vn.vietmap.vietmapsdk.utils.BitmapUtils
import java.util.Arrays
import java.util.Objects
import java.util.Random

class SymbolLayerActivity : AppCompatActivity() , VietMapGL.OnMapClickListener{
    private val random = Random()
    private var markerSource: GeoJsonSource? = null
    private var markerCollection: FeatureCollection? = null
    private var markerSymbolLayer: SymbolLayer? = null
    private var vietmapSignSymbolLayer: SymbolLayer? = null
    private var numberFormatSymbolLayer: SymbolLayer? = null
    private lateinit var vietMapGL: VietMapGL
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symbol_layer)
        val vietMapGLOptions = VietMapGLOptions.createFromAttributes(this)
        vietMapGLOptions.camera(
            CameraPosition.Builder().target(
                LatLng(52.35273, 4.91638)
            )
                .zoom(13.0)
                .build()
        )
        mapView = MapView(applicationContext, vietMapGLOptions)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{
            vietMapGL = it

            val carBitmap = BitmapUtils.getBitmapFromDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_action_name, null)
            )

            // marker source
            markerCollection = FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(4.91638, 52.35673),
                        featureProperties("1", "Android")
                    ),
                    Feature.fromGeometry(
                        Point.fromLngLat(4.91638, 52.34673),
                        featureProperties("2", "Car")
                    )
                )
            )
            markerSource = GeoJsonSource(MARKER_SOURCE, markerCollection)

            // marker layer
            markerSymbolLayer = SymbolLayer(
                MARKER_LAYER,
                MARKER_SOURCE
            )
                .withProperties(
                    PropertyFactory.iconImage(
                        Expression.get(TITLE_FEATURE_PROPERTY)),
                    PropertyFactory.iconIgnorePlacement(true),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconSize(
                        Expression.switchCase(
                            Expression.toBool(
                                Expression.get(
                                    SELECTED_FEATURE_PROPERTY
                                )
                            ),
                            Expression.literal(1.5f),
                            Expression.literal(1.0f)
                        )
                    ),
                    PropertyFactory.iconAnchor(
                        Property.ICON_ANCHOR_BOTTOM),
                    PropertyFactory.iconColor(Color.BLUE),
                    PropertyFactory.textField(TEXT_FIELD_EXPRESSION),
                    PropertyFactory.textFont(NORMAL_FONT_STACK),
                    PropertyFactory.textColor(Color.BLUE),
                    PropertyFactory.textAllowOverlap(true),
                    PropertyFactory.textIgnorePlacement(true),
                    PropertyFactory.textAnchor(
                        Property.TEXT_ANCHOR_TOP),
                    PropertyFactory.textSize(10f)
                )

            // vietmap sign layer
            val vietmapSignSource: Source =
                GeoJsonSource(VIETMAP_SIGN_SOURCE, Point.fromLngLat(4.91638, 52.3510))
            vietmapSignSymbolLayer = SymbolLayer(
                VIETMAP_SIGN_LAYER,
                VIETMAP_SIGN_SOURCE
            )
            shuffleVietmapSign()

            // number format layer
            val numberFormatSource: Source =
                GeoJsonSource(NUMBER_FORMAT_SOURCE, Point.fromLngLat(4.92756, 52.3516))
            numberFormatSymbolLayer = SymbolLayer(
                NUMBER_FORMAT_LAYER,
                NUMBER_FORMAT_SOURCE
            )
            numberFormatSymbolLayer!!.setProperties(
                PropertyFactory.textField(
                    Expression.numberFormat(
                        123.456789,
                        Expression.NumberFormatOption.locale("nl-NL"),
                        Expression.NumberFormatOption.currency("EUR")
                    )
                )
            )
            vietMapGL.setStyle(
                    Style.Builder()
                .fromUri(VietMapTiles.instance.lightVector())
                .withImage("Car", Objects.requireNonNull(carBitmap)!!, false)
                .withSources(markerSource, vietmapSignSource, numberFormatSource)
                .withLayers(markerSymbolLayer, vietmapSignSymbolLayer, numberFormatSymbolLayer))

            vietMapGL.addOnMapClickListener(this)
        }
        (findViewById<View>(R.id.container) as ViewGroup).addView(mapView)

        mapView.addOnStyleImageMissingListener { id: String? ->
            val style = vietMapGL.style
            if (style != null) {
                Timber.e("Adding image with id: %s", id)
                val androidIcon =
                    BitmapUtils.getBitmapFromDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_action_name, null))
                style.addImage(id!!, Objects.requireNonNull(androidIcon)!!)
            }
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        // Query which features are clicked
        val screenLoc = vietMapGL.projection.toScreenLocation(point)
        val markerFeatures = vietMapGL.queryRenderedFeatures(screenLoc, MARKER_LAYER)
        if (!markerFeatures.isEmpty()) {
            for (feature in Objects.requireNonNull(markerCollection!!.features())!!) {
                if (feature.getStringProperty(ID_FEATURE_PROPERTY)
                    == markerFeatures[0].getStringProperty(ID_FEATURE_PROPERTY)
                ) {
                    // use DDS
                    val selected = feature.getBooleanProperty(SELECTED_FEATURE_PROPERTY)
                    feature.addBooleanProperty(SELECTED_FEATURE_PROPERTY, !selected)

                    // validate symbol flicker regression for #13407
                    markerSymbolLayer!!.setProperties(
                        PropertyFactory.iconOpacity(
                            Expression.match(
                                Expression.get(ID_FEATURE_PROPERTY),
                                Expression.literal(1.0f),
                                Expression.stop(
                                    feature.getStringProperty("id"),
                                    if (selected) 0.3f else 1.0f
                                )
                            )
                        )
                    )
                }
            }
            markerSource!!.setGeoJson(markerCollection)
        } else {
            val vietmapSignFeatures = vietMapGL.queryRenderedFeatures(screenLoc, VIETMAP_SIGN_LAYER)
            if (!vietmapSignFeatures.isEmpty()) {
                shuffleVietmapSign()
            }
        }
        return false
    }

    private fun toggleTextSize() {
        if (markerSymbolLayer != null) {
            val size: Number? = markerSymbolLayer!!.textSize.getValue()
            if (size != null) {
                markerSymbolLayer!!.setProperties(
                    if (size as Float > 10) {
                        PropertyFactory.textSize(10f)
                    } else {
                        PropertyFactory.textSize(20f)
                    }
                )
            }
        }
    }

    private fun toggleTextField() {
        if (markerSymbolLayer != null) {
            if (TEXT_FIELD_EXPRESSION == markerSymbolLayer!!.textField.expression) {
                markerSymbolLayer!!.setProperties(PropertyFactory.textField("āA"))
            } else {
                markerSymbolLayer!!.setProperties(PropertyFactory.textField(TEXT_FIELD_EXPRESSION))
            }
        }
    }

    private fun toggleTextFont() {
        if (markerSymbolLayer != null) {
            if (Arrays.equals(markerSymbolLayer!!.textFont.getValue(), NORMAL_FONT_STACK)) {
                markerSymbolLayer!!.setProperties(PropertyFactory.textFont(BOLD_FONT_STACK))
            } else {
                markerSymbolLayer!!.setProperties(PropertyFactory.textFont(NORMAL_FONT_STACK))
            }
        }
    }

    private fun shuffleVietmapSign() {
        if (vietmapSignSymbolLayer != null) {
            vietmapSignSymbolLayer!!.setProperties(
                PropertyFactory.textField(
                    Expression.format(
                        Expression.formatEntry("V", Expression.FormatOption.formatFontScale(2.0)),
                        getRandomColorEntryForString("i"),
                        getRandomColorEntryForString("e"),
                        getRandomColorEntryForString("t"),
                        getRandomColorEntryForString("m"),
                        getRandomColorEntryForString("a"),
                        getRandomColorEntryForString("p"),
                    )
                ),
                PropertyFactory.textColor(Color.BLACK),
                PropertyFactory.textFont(BOLD_FONT_STACK),
                PropertyFactory.textSize(25f),
                PropertyFactory.textRotationAlignment(
                    Property.TEXT_ROTATION_ALIGNMENT_MAP)
            )
        }
    }

    private fun getRandomColorEntryForString(string: String): Expression.FormatEntry {
        return Expression.formatEntry(
            string,
            Expression.FormatOption.formatTextColor(
                Expression.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
            )
        )
    }

    private fun featureProperties(id: String, title: String): JsonObject {
        val `object` = JsonObject()
        `object`.add(ID_FEATURE_PROPERTY, JsonPrimitive(id))
        `object`.add(TITLE_FEATURE_PROPERTY, JsonPrimitive(title))
        `object`.add(SELECTED_FEATURE_PROPERTY, JsonPrimitive(false))
        return `object`
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
        if (vietMapGL != null) {
            vietMapGL.removeOnMapClickListener(this)
        }
        mapView.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_symbol_layer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_text_size -> {
                toggleTextSize()
                true
            }

            R.id.action_toggle_text_field -> {
                toggleTextField()
                true
            }

            R.id.action_toggle_text_font -> {
                toggleTextFont()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val ID_FEATURE_PROPERTY = "id"
        private const val SELECTED_FEATURE_PROPERTY = "selected"
        private const val TITLE_FEATURE_PROPERTY = "title"
        private val NORMAL_FONT_STACK = arrayOf("DIN Offc Pro Regular", "Arial Unicode MS Regular")
        private val BOLD_FONT_STACK = arrayOf("DIN Offc Pro Bold", "Arial Unicode MS Regular")

        // layer & source constants
        private const val MARKER_SOURCE = "marker-source"
        private const val MARKER_LAYER = "marker-layer"
        private const val VIETMAP_SIGN_SOURCE = "vietmap-sign-source"
        private const val VIETMAP_SIGN_LAYER = "vietmap-sign-layer"
        private const val NUMBER_FORMAT_SOURCE = "vietmap-number-source"
        private const val NUMBER_FORMAT_LAYER = "vietmap-number-layer"
        private val TEXT_FIELD_EXPRESSION = Expression.switchCase(
            Expression.toBool(
                Expression.get(SELECTED_FEATURE_PROPERTY)),
            Expression.format(
                Expression.formatEntry(
                    Expression.get(TITLE_FEATURE_PROPERTY),
                    Expression.FormatOption.formatTextFont(BOLD_FONT_STACK)
                ),
                Expression.formatEntry("\nis fun!", Expression.FormatOption.formatFontScale(0.75))
            ),
            Expression.format(
                Expression.formatEntry("This is", Expression.FormatOption.formatFontScale(0.75)),
                Expression.formatEntry(
                    Expression.concat(
                        Expression.literal("\n"),
                        Expression.get(TITLE_FEATURE_PROPERTY)
                    ),
                    Expression.FormatOption.formatFontScale(1.25),
                    Expression.FormatOption.formatTextFont(BOLD_FONT_STACK)
                )
            )
        )
    }
}