# **Tài liệu hướng dẫn cài đặt VietMap Navigation Android SDK**
## Mục lục
[1. Cấu hình gradle và AndroidManifest](/README.md#i-thêm-các-dependencies-vào-buildgradle-module-app)


###  **I**. Thêm các dependencies vào build.gradle module app

```gradle
    implementation 'com.github.vietmap-company:maps-sdk-android:1.0.0'
    implementation 'com.github.vietmap-company:vietmap-services-turf-android:1.0.2'
    implementation 'com.github.vietmap-company:vietmap-services-android:1.0.8'
    implementation 'com.squareup.picasso:picasso:2.8'
    implementation 'com.github.vietmap-company:vietmap-services-geojson-android:1.0.0'
    implementation group: 'com.squareup.okhttp3', name: 'okhttp', version: '3.2.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.google.code.gson:gson:2.10.1'
```
Cấu hình **jitpack repository** tại file **setting.gradle**
```gradle

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Thêm 2 dòng dưới đây vào repositories (tại file setting.gradle)
        maven { url 'https://plugins.gradle.org/m2' }
        maven { url 'https://jitpack.io' }
    }
}
```
Đối với các project cũ, thêm vào file **build.gradle tại module project**
```gradle
allprojects {
    repositories {
        google()
        maven { url "https://jitpack.io" }
    }
}
```
Chuyển **compileSdk** và **targetSdk** vể version **_33_**
```
compileSdk 33
```
```
targetSdk 33
```
Thêm các quyền sau vào **AndroidManifest.xml**
```xml
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```


### **II**. Tạo activity navigation để sử dụng sdk


Tạo một **activity** mới với tên **MapViewExampleActivity**

Tại file **xml** của **activity**, thêm đoạn code như sau
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MapViewExampleActivity">

    <com.mapbox.mapboxsdk.maps.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:maplibre_cameraZoom="1"

        android:visibility="visible"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintHeight_percent="1"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

Activity cần implements một số class Listener dưới đây để hứng event và xử lý trong quá trình sdk đang dẫn đường


```java
public class MapViewExampleActivity extends AppCompatActivity 
implements OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener {

}
```

>   - OnMapReadyCallback: Lắng nghe khi map init hoàn thành và gán style cho map
>   - MapboxMap.OnMapClickListener,MapboxMap.OnMapLongClickListener, MapboxMap.OnMoveListener: Lắng nghe các sự kiện của map

Khai báo các biến cần thiết

```java
    private MapView mapView;
    private MapboxMap mapboxMap;
    List<Point> polylineCoordinates = new ArrayList<>();
    List<Point> polygonCoordinates = new ArrayList<>();
    private LocationComponent locationComponent;
```
Tại hàm **onCreate**, bắt đầu khởi tạo màn hình dẫn đường
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_example);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Toast.makeText(MapViewExampleActivity.this, "Long click on map to place a marker", Toast.LENGTH_LONG).show();
    }
```
### Tại hàm **onMapReady**:
```java
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(YOUR_MAP_STYLE_GOES_HERE), style -> {
            enableLocationComponent(style);
            addPolygonLayer();
            addPolylineLayer();
            initMarker();
//            moveMapToLocation(10.753892, 106.672606, 14);

        });
        this.mapboxMap.setOnPolylineClickListener(polyline1 -> Toast.makeText(MapViewExampleActivity.this, "You clicked on polyline with id " + polyline1.getId(), Toast.LENGTH_LONG).show());

        this.mapboxMap.setOnPolygonClickListener(polygon1 -> Toast.makeText(MapViewExampleActivity.this, "You clicked on polygon with id " + polygon1.getId(), Toast.LENGTH_LONG).show());

        this.mapboxMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(MapViewExampleActivity.this, "You clicked on marker with location " + marker.getPosition().toString(), Toast.LENGTH_LONG).show();
            return false;
        });
        this.mapboxMap.addOnMapClickListener(this);
        this.mapboxMap.addOnMapLongClickListener(this);
    }
```

Tạo hàm **_enableLocationComponent_** để hiển thị vị trí hiện tại của thiết bị
```java
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

Hàm thêm **_polyline_**
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

        Style style = mapboxMap.getStyle();
        if (style != null) {
            style.addSource(new GeoJsonSource("polyline-source-id", lineString));
            style.addLayer(lineLayer);
        }
    }
```
Hàm thêm **_polygon_**
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
        Style style = mapboxMap.getStyle();
        if (style != null) {
            style.addSource(new GeoJsonSource("polygon-source-id", polygon));
            style.addLayer(fillLayer);
        }

    }
```
Hàm thêm **_marker_**
```java
    // Hàm init marker được tạo để thêm trước icon cho toàn bộ marker mang tên custom_marker
    // Các marker cùng tên khi bản đồ thu nhỏ lại sẽ tự động gom nhóm vào với nhau
    private void initMarker() {
        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker);
        mapboxMap.getStyle().addImage("custom_marker", iconBitmap);
    }
    // Hàm addMarker dùng để thêm các marker vào vị trí tuỳ chọn
    private void addMarker(LatLng position) {
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, mapboxMap.getStyle());
        SymbolOptions symbolOptions = new SymbolOptions()
                .withLatLng(position)
                .withIconImage("custom_marker")
                .withIconSize(0.3f);
        symbolManager.create(symbolOptions);
    }
```

Hàm thêm marker được gọi khi chạm giữ trên bản đồ
```java
@Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        addMarker(latLng);
        return false;
    }
```

Hàm move camera tới một vị trí bất kì
```java
    private void moveMapToLocation(double latitude, double longitude, Integer zoom) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }
```

Thêm các hàm sau để sdk hoạt động chính xác
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

Tại class **_MainActivity_**, thêm đoạn code sau để xin quyền vị trí trước khi vào trang bản đồ
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

Thêm nút chuyển trang cho file xml của MainActivity
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
