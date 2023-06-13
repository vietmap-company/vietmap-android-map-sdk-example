package com.example.vietmapandroidmapsdkexample;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MapViewExampleActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    List<Point> polylineCoordinates = new ArrayList<>();
    List<Point> polygonCoordinates = new ArrayList<>();
    private LocationComponent locationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_example);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(YOUR_VIETMAP_STYLE_URL_HERE), style -> {
            enableLocationComponent(style);
            addPolylineLayer();
            addPolygonLayer();
            addMarker();
//            moveMapToLocation(10.753892, 106.672606, 14);

        });

        this.mapboxMap.addOnMapClickListener(this);
    }

    private void enableLocationComponent(Style style) {
        locationComponent = mapboxMap.getLocationComponent();
        if (locationComponent != null) {
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 10000.0, 10000.0, null);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.zoomWhileTracking(14);
            locationComponent.setRenderMode(RenderMode.GPS);
        }
    }

    private void addPolylineLayer() {
        polylineCoordinates.add(Point.fromLngLat(106.659260, 10.759879));
        polylineCoordinates.add(Point.fromLngLat(106.670375, 10.765950));
        polylineCoordinates.add(Point.fromLngLat(106.675439, 10.752163));
        polylineCoordinates.add(Point.fromLngLat(106.658702,10.775183));
        LineString lineString = LineString.fromLngLats(polylineCoordinates);
        LineLayer lineLayer = new LineLayer("polyline-layer", "polyline-source-id");
        lineLayer.setProperties(
                PropertyFactory.lineColor(Color.RED),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineOpacity(0.7f)
        );
        Style style = mapboxMap.getStyle();
        if (style != null) {
            style.addSource(new GeoJsonSource("polyline-source-id", lineString));
            style.addLayer(lineLayer);
        }
    }

    private void addPolygonLayer() {
        polygonCoordinates.add(Point.fromLngLat(106.659260, 10.759879));
        polygonCoordinates.add(Point.fromLngLat(106.670375, 10.765950));
        polygonCoordinates.add(Point.fromLngLat(106.675439, 10.752163));
        polygonCoordinates.add(Point.fromLngLat(106.658702,10.775183));
        Polygon polygon = Polygon.fromLngLats(Collections.singletonList(polygonCoordinates));
        FillLayer fillLayer = new FillLayer("polygon-layer", "polygon-source-id");
        fillLayer.setProperties(
                PropertyFactory.fillColor(Color.BLUE),
                PropertyFactory.fillOpacity(0.8f)
        );
        Style style = mapboxMap.getStyle();
        if (style != null) {
            style.addSource(new GeoJsonSource("polygon-source-id", polygon));
            style.addLayer(fillLayer);
        }

    }


    private void addMarker() {
        LatLng markerPosition = new LatLng(10.775183,106.658702);
        LatLng marker2Position = new LatLng(10.768353, 106.662049

        );
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, mapboxMap.getStyle());
        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker);
        mapboxMap.getStyle().addImage("custom_marker",iconBitmap);
        SymbolOptions symbolOptions = new SymbolOptions()
                .withLatLng(markerPosition)
                .withIconImage("custom_marker")
                .withIconSize(0.3f);
        symbolManager.create(symbolOptions);
        SymbolOptions symbolOptionss = new SymbolOptions()
                .withLatLng(marker2Position)
                .withIconImage("custom_marker")
                .withIconSize(0.3f);
        symbolManager.create(symbolOptionss);

    }
    private void moveMapToLocation(double latitude, double longitude, Integer zoom) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {
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

}