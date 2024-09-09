# **VietMap Navigation Android SDK documentation**
## Table of contents
[1. Gradle and AndroidManifest configure](/README.md#i-add-dependencies-below-to-buildgradle-module-app)

[2. Create a mapview activity](/README.md#ii-create-an-activity-to-show-map)

[3. Show the current GPS location of the device](/README.md#create-enablelocationcomponent-function-to-show-the-user-current-location)

[4. Add a marker](/README.md#add-a-marker)

[5. Add a line/polyline](/README.md#add-a-polyline)

[6. Add a shape/polygon](/README.md#add-a-polygon)

[7. Move map camera to a specific location](/README.md#move-map-camera-to-a-specific-location)

[8. Add some necessary function](/README.md#add-some-necessary-function)

[9. Request location permission](/README.md#add-below-code-to-mainactivity-class-to-request-location-permission)

[10. Android auto implement](https://github.com/vietmap-company/vietmap-android-auto)
###  **I**. Add dependencies below to build.gradle module app

```gradle
    implementation 'com.github.vietmap-company:maps-sdk-android:2.5.1'
    implementation 'com.github.vietmap-company:vietmap-services-turf-android:1.0.2'
    implementation 'com.squareup.picasso:picasso:2.8'
    implementation 'com.github.vietmap-company:vietmap-services-geojson-android:1.0.0'
    implementation group: 'com.squareup.okhttp3', name: 'okhttp', version: '3.2.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.google.code.gson:gson:2.10.1'
```
Configure the **jitpack repository** in the **setting.gradle** file
```gradle

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add two lines below to the repositories block (In setting.gradle file)
        maven { url 'https://plugins.gradle.org/m2' }
        maven { url 'https://jitpack.io' }
    }
}
```
With older projects, add to the **build.gradle file at module project**
```gradle
allprojects {
    repositories {
        google()
        maven { url "https://jitpack.io" }
    }
}
```
Upgrade the **compileSdk** and **targetSdk** to version **_33_**
```gradle
compileSdk 33
```
```gradle
targetSdk 33
```
Add the below permission request to the  **AndroidManifest.xml** file
```xml
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```


### **II**. Create an activity to show map


Create an new **activity** with name **MapViewExampleActivity**

In the **xml** file of created **activity**, add below code
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MapViewExampleActivity">

    <vn.vietmap.vietmapsdk.maps.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:vietmap_cameraZoom="1"
        android:visibility="visible"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintHeight_percent="1"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

Activity needs to implement some of the following Listener classes to catch user events and process them.


```java
public class MapViewExampleActivity extends AppCompatActivity 
implements OnMapReadyCallback, VietMapGL.OnMapClickListener, 
VietMapGL.OnMapLongClickListener {


}
```

>   - OnMapReadyCallback: Listen when map initial successfully and return a map style
>   - VietMapGL.OnMapClickListener, VietMapGL.OnMapLongClickListener, VietMapGL.OnMoveListener: Listen map events

Define necessary variables

```java
    private MapView mapView;
    private VietMapGL vietmapGL;
    List<Point> polylineCoordinates = new ArrayList<>();
    List<Point> polygonCoordinates = new ArrayList<>();
    private LocationComponent locationComponent;
```
In **onCreate** function, add some necessary code to init the map view
```java
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
```
### In **onMapReady** function:
Add listener function and handle them when SDK receives user actions callback
```java
    @Override
    public void onMapReady(@NonNull VietMapGL vietmapGL) {
        this.vietmapGL = vietmapGL;
        vietmapGL.setStyle(new Style.Builder().fromUri(YOUR_MAP_STYLE_GOES_HERE), style -> {
            enableLocationComponent(style);
            addPolygonLayer();
            addPolylineLayer();
            initMarker();
//            moveMapToLocation(10.753892, 106.672606, 14);
        });
        this.vietmapGL.setOnPolylineClickListener(polyline -> Toast.makeText(MapViewExampleActivity.this, "You clicked on polyline with id " + polyline.getId(), Toast.LENGTH_LONG).show());

        this.vietmapGL.setOnPolygonClickListener(polygon -> Toast.makeText(MapViewExampleActivity.this, "You clicked on polygon with id " + polygon.getId(), Toast.LENGTH_LONG).show());

        this.vietmapGL.setOnMarkerClickListener(marker -> {
            Toast.makeText(MapViewExampleActivity.this, "You clicked on marker with location " + marker.getPosition().toString(), Toast.LENGTH_LONG).show();
            return false;
        });
        this.vietmapGL.addOnMapClickListener(this);
        this.vietmapGL.addOnMapLongClickListener(this);
    }
```

#### Create **_enableLocationComponent_** function to show the user current location.
```java
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
                    //Customize gps location icon here
                    .gpsDrawable(R.drawable.custom_location)
                    .accuracyAlpha(0.0f)
                    .build();

            locationComponent.applyStyle(locationComponentOptions);
        }
    }
```

#### Add a **_polyline_**
```java
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
```
#### Add a **_polygon_**
```java

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

    }
```
#### Add a **_marker_**
```java
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
```

On long click callback function
```java
@Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        addMarker(latLng);
        return false;
    }
```

#### Move map camera to a specific location
```java
    private void moveMapToLocation(double latitude, double longitude, Integer zoom) {
        vietmapGL.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }
```

#### Add some necessary function
```java
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
```

#### Add below code to **_MainActivity_** class to request location permission
```java
public class MainActivity extends AppCompatActivity implements PermissionsListener{

    private PermissionsManager permissionsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button =  findViewById(R.id.pushToMapViewScreen);
        Intent it = new Intent(this, MapViewExampleActivity.class);
        button.setOnClickListener(view -> startActivity(it));

        permissionsManager = new PermissionsManager(this);
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
        } else {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
```

Add navigate page button for the MainActivity xml file
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/pushToMapViewScreen"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="36dp"
        android:text="Start to VietMapAndroidMapScreen"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.494"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        />
</androidx.constraintlayout.widget.ConstraintLayout>
```

