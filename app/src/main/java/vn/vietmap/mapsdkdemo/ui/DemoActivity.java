package vn.vietmap.mapsdkdemo.ui;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Locale;

import vn.vietmap.mapsdkdemo.R;
import vn.vietmap.mapsdkdemo.utils.IconUtils;
import vn.vietmap.mapsdkdemo.utils.VietMapTiles;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.InfoWindow;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineDefault;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class DemoActivity extends AppCompatActivity implements OnMapReadyCallback, VietMapGL.OnMapClickListener, VietMapGL.OnCameraMoveListener {
    private MapView mapView;
    private VietMapGL vietMapGL;
    private Style style;
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
    private Marker marker = null;
    private static final LatLng HANOI = new LatLng(21.027603, 105.833148);
    private static final DecimalFormat LAT_LON_FORMATTER = new DecimalFormat("#.#####");
    private static final String STATE_MARKER_LIST = "markerList";

    private double currentZoomLevel = 5;
    private boolean isLargeMap = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Vietmap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mapView = findViewById(R.id.demoMapView);
        Button zoomInButton = findViewById(R.id.zoomIn);
        Button zoomOutButton = findViewById(R.id.zoomOut);
        Button resizeMapButton = findViewById(R.id.resizeMap);
        // Please make sure the zoom level in the range 0 <-> 21
        zoomInButton.setOnClickListener(v -> vietMapGL.animateCamera(CameraUpdateFactory.zoomTo(++currentZoomLevel)));
        zoomOutButton.setOnClickListener(v -> vietMapGL.animateCamera(CameraUpdateFactory.zoomTo(--currentZoomLevel)));
        resizeMapButton.setOnClickListener(v -> {
            ViewGroup.LayoutParams params = mapView.getLayoutParams();
            if(isLargeMap) {
                params.width = 200;
                params.height = 300;
            }else{
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            isLargeMap = !isLargeMap;
            mapView.setLayoutParams(params);

        });
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull VietMapGL vietMapGL) {
        this.vietMapGL = vietMapGL;
        vietMapGL.setStyle(new Style.Builder().fromUri(VietMapTiles.Companion.getInstance().lightVector()), style -> {

            this.style = style;
            initLocationEngine();
            enableLocationComponent(style);

            vietMapGL.selectMarker(marker);

            // Show/hide compass
            vietMapGL.getUiSettings().setCompassEnabled(false);

            // Handle on camera move, update the zoom level
            vietMapGL.addOnCameraMoveListener(this);

            // On map click, change the info window contents
            vietMapGL.addOnMapClickListener(this);

            // Focus on HaNoi with 1 second duration
            vietMapGL.animateCamera(CameraUpdateFactory.newLatLng(HANOI), 1000);

            // Keep info windows open on click
            vietMapGL.getUiSettings().setDeselectMarkersOnTap(false);
            addCustomInfoWindowAdapter(vietMapGL);
            marker = addInforwindowMarker(vietMapGL);
        });
    }

    private void addCustomInfoWindowAdapter(VietMapGL vietMapGL) {
        int padding = (int) getResources().getDimension(R.dimen.attr_margin);
        vietMapGL.setInfoWindowAdapter(marker -> {
            /// If you have multiple marker in the map screen, please check them to show the custom info window
            if(marker.getPosition()==HANOI) {
                /// You can return other Android View here to show on the marker
                TextView textView = new TextView(DemoActivity.this);
                textView.setText(marker.getTitle());
                textView.setBackgroundColor(Color.WHITE);
                textView.setText(R.string.action_calculate_distance);
                textView.setTextColor(Color.BLACK);
                textView.setPadding(padding, padding, padding, padding);
                return textView;
            }
            return null;
        });
    }

    private Marker addInforwindowMarker(VietMapGL vietMapGL) {
        return vietMapGL.addMarker(new MarkerOptions()

                .position(HANOI)

                .icon(new IconUtils().drawableToIcon(
                        this,
                        R.drawable.ic_my_location,
                        ResourcesCompat.getColor(getResources(), R.color.blueAccent, getTheme())
                ))
        );
    }
    private void addMarker(LatLng point) {
        PointF pixel = vietMapGL.getProjection().toScreenLocation(point);
        String title = (
                LAT_LON_FORMATTER.format(point.getLatitude()) + ", " +
                        LAT_LON_FORMATTER.format(point.getLongitude())
        );
        String snippet = "X = " + (int) pixel.x + ", Y = " + (int) pixel.y;
        MarkerOptions marker = new MarkerOptions()
                .position(point)
                .title(title)
                .snippet(snippet);
        vietMapGL.addMarker(marker);
    }

    private void enableLocationComponent(Style style) {
        locationComponent = vietMapGL.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                        this, style
                ).build()
        );
        if (!checkPermission()) {
            return;
        }

        /// Camera mode and render mode will automatically follow and move map to the current user
        /// location. Please disable any camera animate/move at this time.
        /// If you need the map follow the user anchor/location, change the camera mode to `CameraMode.TRACKING_COMPASS`
        locationComponent.setCameraMode(
                CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 0.0, 0.0, null
        );
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.zoomWhileTracking(19.0);
        locationComponent.setRenderMode(RenderMode.GPS);
        locationComponent.setLocationEngine(locationEngine);
        updateMyLocationTrackingMode();
        updateMyLocationRenderMode();
    }

    private void updateMyLocationTrackingMode() {
        int[] vietmapTrackingMode = new int[]{
                CameraMode.NONE,
                CameraMode.TRACKING,
                CameraMode.TRACKING_COMPASS,
                CameraMode.TRACKING_GPS
        };
        if (locationComponent != null) {
            locationComponent.setCameraMode(vietmapTrackingMode[1]);
        }
    }

    private void updateMyLocationRenderMode() {
        int[] vietmapRenderModes = new int[]{RenderMode.NORMAL, RenderMode.COMPASS, RenderMode.GPS};
        if (locationComponent != null) {
            locationComponent.setRenderMode(vietmapRenderModes[2]);
        }
    }


    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void initLocationEngine() {
        /// Engine for update user location
        LocationEngineDefault locationEngineDefault = LocationEngineDefault.INSTANCE;
        locationEngine = locationEngineDefault.getDefaultLocationEngine(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vietMapGL != null) {
            vietMapGL.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng p0) {
        if (marker == null) {
            return false;
        }

        addMarker(p0);
        // Distance from click to marker
        double distanceKm = marker.getPosition().distanceTo(p0) / 1000;

        // Get the info window
        InfoWindow infoWindow = marker.getInfoWindow();

        // Get the view from the info window
        if (infoWindow != null && infoWindow.getView() != null) {
            // Set the new text on the text view in the info window
            TextView textView = (TextView) infoWindow.getView();
            textView.setText(String.format(Locale.getDefault(), "%.2fkm", distanceKm));
            // Update the info window position (as the text length changes)
            textView.post(infoWindow::update);
        }
        return true;
    }

    @Override
    public void onCameraMove() {
        CameraPosition position = vietMapGL.getCameraPosition();

        // Get the current zoom level
          currentZoomLevel = position.zoom;

    }
}