package com.example.vietmapandroidmapsdkexample;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.PolygonOptions;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.LocationComponentOptions;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.plugins.annotation.SymbolManager;
import vn.vietmap.vietmapsdk.plugins.annotation.SymbolOptions;
import vn.vietmap.vietmapsdk.style.layers.FillLayer;
import vn.vietmap.vietmapsdk.style.layers.LineLayer;
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory;
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MapViewExampleActivity extends AppCompatActivity implements OnMapReadyCallback, VietMapGL.OnMapClickListener, VietMapGL.OnMapLongClickListener {

    private MapView mapView;
    private VietMapGL vietmapGL;
    List<Point> polylineCoordinates = new ArrayList<>();
    List<Point> polygonCoordinates = new ArrayList<>();
    private LocationComponent locationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Vietmap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_example);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Toast.makeText(MapViewExampleActivity.this, "Long click on map to place a marker", Toast.LENGTH_LONG).show();

    }

    @Override
        public void onMapReady(@NonNull VietMapGL vietmapGL) {
        this.vietmapGL = vietmapGL;
        vietmapGL.setStyle(new Style.Builder().fromUri("https://run.mocky.io/v3/961aaa3a-f380-46be-9159-09cc985d9326"), style -> {
            enableLocationComponent(style);
            addPolygonLayer();
            addPolylineLayer();
            initMarker();
//            moveMapToLocation(10.753892, 106.672606, 14);

        });
        this.vietmapGL.setOnPolylineClickListener(polyline1 -> Toast.makeText(MapViewExampleActivity.this, "You clicked on polyline with id " + polyline1.getId(), Toast.LENGTH_LONG).show());

        this.vietmapGL.setOnPolygonClickListener(polygon1 -> Toast.makeText(MapViewExampleActivity.this, "You clicked on polygon with id " + polygon1.getId(), Toast.LENGTH_LONG).show());

        this.vietmapGL.setOnMarkerClickListener(marker -> {
            Toast.makeText(MapViewExampleActivity.this, "You clicked on marker with location " + marker.getPosition().toString(), Toast.LENGTH_LONG).show();
            return false;
        });
        this.vietmapGL.addOnMapClickListener(this);
        this.vietmapGL.addOnMapLongClickListener(this);
    }

    private void enableLocationComponent(Style style) {
        locationComponent = vietmapGL.getLocationComponent();
        if (locationComponent != null) {
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 10000.0, 10000.0, null);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.zoomWhileTracking(14);
            locationComponent.setRenderMode(RenderMode.GPS);
            LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(this)
                    .pulseEnabled(true)
                    .pulseColor(Color.BLUE)
                    .pulseAlpha(0.4f)
                    .gpsDrawable(R.drawable.custom_location)
                    .accuracyAlpha(0.0f)
                    .build();

            locationComponent.applyStyle(locationComponentOptions);
        }
    }

    private void addPolylineLayer() {
        polylineCoordinates.add(Point.fromLngLat(106.659260, 10.759879));
        polylineCoordinates.add(Point.fromLngLat(106.675439, 10.752163));
        polylineCoordinates.add(Point.fromLngLat(106.670375, 10.765950));
        polylineCoordinates.add(Point.fromLngLat(106.658702, 10.775183));
        polylineCoordinates.add(Point.fromLngLat(106.651228, 10.778732));
        polylineCoordinates.add(Point.fromLngLat(106.659260, 10.759879));
        LineString lineString = LineString.fromLngLats(polylineCoordinates);
        LineLayer lineLayer = new LineLayer("polyline-layer", "polyline-source-id");
        lineLayer.setProperties(


                PropertyFactory.lineColor(Color.RED),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineOpacity(0.7f)
        );

        Style style = vietmapGL.getStyle();
        if (style != null) {
            style.addSource(new GeoJsonSource("polyline-source-id", lineString));
            style.addLayer(lineLayer);
        }

    }

    private void addPolygonLayer() {
        polygonCoordinates.add(Point.fromLngLat(106.653561, 10.755427));
        polygonCoordinates.add(Point.fromLngLat(106.666822, 10.737105));
        polygonCoordinates.add(Point.fromLngLat(106.689939, 10.760373));
        polygonCoordinates.add(Point.fromLngLat(106.665687, 10.807393));
        polygonCoordinates.add(Point.fromLngLat(106.619878, 10.821045));
        polygonCoordinates.add(Point.fromLngLat(106.650261, 10.766545));
        Polygon polygon = Polygon.fromLngLats(Collections.singletonList(polygonCoordinates));
        FillLayer fillLayer = new FillLayer("polygon-layer", "polygon-source-id");
        fillLayer.setProperties(
                PropertyFactory.fillColor(Color.BLUE),
                PropertyFactory.fillOpacity(0.8f)
        );
        Style style = vietmapGL.getStyle();
        if (style != null) {
            style.addSource(new GeoJsonSource("polygon-source-id", polygon));
            style.addLayer(fillLayer);
        }
//        vietmapGL.addPolygon(new PolygonOptions(new Polygon(polygonCoordinates)));
    }

    private void initMarker() {
        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker);
        vietmapGL.getStyle().addImage("custom_marker", iconBitmap);
    }

    private void addMarker(LatLng position) {
        SymbolManager symbolManager = new SymbolManager(mapView, vietmapGL, vietmapGL.getStyle());
        SymbolOptions symbolOptions = new SymbolOptions()
                .withLatLng(position)
                .withIconImage("custom_marker")
                .withIconSize(0.3f);
        symbolManager.create(symbolOptions);
    }

    private void moveMapToLocation(double latitude, double longitude, Integer zoom) {
        vietmapGL.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {
        PointF screenPoint = vietmapGL.getProjection().toScreenLocation(latLng);
        List<Feature> features = vietmapGL.queryRenderedFeatures(screenPoint);

        for (Feature feature : features) {

            if (feature.geometry().type()=="LineString") {
                Toast.makeText(MapViewExampleActivity.this, "Polyline clicked", Toast.LENGTH_SHORT).show();
                return true; // Return true to indicate that the click event has been handled
            }
            if (feature.geometry().type()=="Polygon") {
                Toast.makeText(MapViewExampleActivity.this, "Polygon clicked", Toast.LENGTH_SHORT).show();
                return true; // Return true to indicate that the click event has been handled
            }
            if (feature.geometry().type()=="Point") {
                Toast.makeText(MapViewExampleActivity.this, "Point clicked", Toast.LENGTH_SHORT).show();
                return true; // Return true to indicate that the click event has been handled
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        addMarker(latLng);
        return false;
    }
}