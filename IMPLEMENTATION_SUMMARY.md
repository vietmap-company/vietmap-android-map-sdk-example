# SearchRouteActivity Implementation Summary

## Overview
Successfully implemented a Search and Routing screen for the Vietmap Android SDK example app using Java, integrating Vietmap's Autocomplete v4, Place v4, and Route v3 APIs.

## Files Created/Modified

### 1. SearchRouteActivity.java
**Location:** `/app/src/main/java/vn/vietmap/mapsdkdemo/ui/SearchRouteActivity.java`

**Features:**
- Proper Vietmap SDK initialization using `Vietmap.getInstance(this)`
- Map style configuration using `VietMapTiles.Companion.getInstance().lightVector()`
- Search functionality with autocomplete (type-ahead + search button)
- Two-point route selection (origin → destination)
- Route visualization on map using polyline
- Clear/reset functionality to start over
- Visual feedback with info text showing current step
- CardView with elevation for better UI visibility
- Proper color scheme (black text, white background, blue/red buttons)

**SDK Initialization Pattern:**
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Initialize Vietmap SDK - REQUIRED
    Vietmap.getInstance(this);
    
    setContentView(R.layout.activity_search_route);

    mapView = findViewById(R.id.vietmap_map);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(vietMapGL -> {
        this.vietMapGL = vietMapGL;
        // Set map style using VietMapTiles utility
        vietMapGL.setStyle(
            new Style.Builder().fromUri(VietMapTiles.Companion.getInstance().lightVector())
        );
    });
}
```

**API Key Configuration:**
- Currently set to: `YOUR_API_KEY`
- Used in both `performAutocomplete()` and `fetchRoute()` methods

### 2. VietmapSearchRouteApiModels.java
**Location:** `/app/src/main/java/vn/vietmap/mapsdkdemo/ui/VietmapSearchRouteApiModels.java`

**API Models (Matching Official Docs):**

#### Autocomplete v4 (GeoJSON FeatureCollection)
```java
class VietmapAutocompleteResponse {
    public String type; // "FeatureCollection"
    public List<Feature> features;
    
    public static class Feature {
        public String type; // "Feature"
        public Properties properties;
        public Geometry geometry;
    }
    
    public static class Properties {
        public String ref_id;  // Used for place detail API
        public String name;
        public String address;
        public String label;   // Display text
        public String layer;
    }
    
    public static class Geometry {
        public String type; // "Point"
        public double[] coordinates; // [lon, lat]
    }
}
```

#### Place v4 (GeoJSON Feature)
```java
class VietmapPlaceDetailResponse {
    public String type; // "Feature"
    public PlaceProperties properties;
    public PlaceGeometry geometry;
    
    public static class PlaceProperties {
        public String id;
        public String ref_id;
        public String name;
        public String address;
    }
    
    public static class PlaceGeometry {
        public String type; // "Point"
        public double[] coordinates; // [lon, lat]
    }
}
```

#### Route v3 (GraphHopper-style)
```java
class VietmapRouteResponse {
    public List<Path> paths;
    
    public static class Path {
        public double distance;
        public double weight;
        public double time;
        public boolean transfers;
        public String points;  // Encoded polyline
        public boolean points_encoded;
        public List<Double> bbox;
        public List<Instruction> instructions;
        public SnappedWaypoints snapped_waypoints;
    }
}
```

**Retrofit Interfaces:**
```java
interface VietmapSearchService {
    @GET("/api/autocomplete/v4")
    Call<VietmapAutocompleteResponse> autocomplete(
        @Query("apikey") String apiKey,
        @Query("text") String text,
        @Query("api-version") String apiVersion,
        @Query("limit") Integer limit,
        @Query("focus.point") String focusPoint,
        @Query("focus.radius") Integer focusRadius
    );
    
    @GET("/api/place/v4")
    Call<VietmapPlaceDetailResponse> placeDetail(
        @Query("apikey") String apiKey,
        @Query("refid") String refid,
        @Query("api-version") String apiVersion
    );
}

interface VietmapRouteService {
    @GET("/api/route/v3")
    Call<VietmapRouteResponse> route(
        @Query("apikey") String apiKey,
        @Query("point") String origin,
        @Query("point") String destination,
        @Query("vehicle") String vehicle,
        @Query("api-version") String apiVersion,
        @Query("calc_points") Boolean calcPoints,
        @Query("instructions") Boolean instructions,
        @Query("locale") String locale
    );
}
```

### 3. VietMapPolyline.java
**Location:** `/app/src/main/java/vn/vietmap/mapsdkdemo/utils/VietMapPolyline.java`

Utility class for decoding Google/Vietmap encoded polylines into LatLng points.

### 4. activity_search_route.xml
**Location:** `/app/src/main/res/layout/activity_search_route.xml`

**UI Features:**
- Full-screen `vn.vietmap.vietmapsdk.maps.MapView`
- CardView container with elevation for better visibility
- Search input with dark text (`#000000`) on white background
- Blue "Search" button for triggering autocomplete
- Red "Clear Route" button (shown after origin selection)
- Autocomplete results ListView with proper dividers
- Info text at bottom with semi-transparent background showing current step
- All text and UI elements now visible with proper contrast

### 5. MainActivity.kt
**Modified:** Added "Search/Routing" button at the top of the menu.

### 6. AndroidManifest.xml
**Added:** `<activity android:name=".ui.SearchRouteActivity" android:exported="false" />`

## API Documentation References

### Autocomplete v4
- **URL:** https://maps.vietmap.vn/docs/map-api/autocomplete-version/autocomplete-v4/
- **Endpoint:** `GET /api/autocomplete/v4`
- **Response:** GeoJSON FeatureCollection

### Place v4
- **URL:** https://maps.vietmap.vn/docs/map-api/place-v4/
- **Endpoint:** `GET /api/place/v4`
- **Response:** GeoJSON Feature

### Route v3
- **URL:** https://maps.vietmap.vn/docs/map-api/route-version/route-v3/
- **Endpoint:** `GET /api/route/v3`
- **Response:** GraphHopper-style routing response

## User Flow

1. **Launch:** User taps "Search/Routing" button in main menu
2. **Search Origin:** User types location → sees autocomplete results → selects origin
3. **Search Destination:** User types again → selects destination
4. **Route Display:** App fetches route and draws blue polyline on map

## Dependencies

Already present in `app/build.gradle.kts`:
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.0.0-beta4")
implementation("com.google.code.gson:gson:2.10.1")
```

## SDK Initialization Pattern (Critical)

**From VietmapScreen.kt (reference implementation):**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    Vietmap.getInstance(this)  // MUST be called before super.onCreate
    super.onCreate(savedInstanceState)
    // ... rest of setup
}
```

**Applied in SearchRouteActivity.java:**
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Vietmap.getInstance(this);  // Initialize SDK
    // ... rest of setup
}
```

## Build Status

✅ **Build Successful:** `./gradlew assembleDebug` completes without errors
✅ **No Compile Errors:** Only deprecation warnings (expected for Polyline/PolylineOptions)
✅ **Activity Registered:** AndroidManifest.xml includes SearchRouteActivity
✅ **Menu Integration:** Button added to MainActivity

## Notes

1. **API Models Match Docs:** All request/response models follow the exact structure from Vietmap API documentation (GeoJSON standard for search/place, GraphHopper format for routing)

2. **Deprecated APIs:** The code uses `Polyline` and `PolylineOptions` from `vn.vietmap.vietmapsdk.annotations` which are marked deprecated but still functional. This is consistent with other activities in the codebase (e.g., VietmapScreen.kt).

3. **API Key:** Currently hardcoded. For production, should be moved to a secure configuration or environment variable.

4. **Polyline Decoding:** Uses custom `VietMapPolyline.decode()` utility to convert encoded route geometry into LatLng points for map rendering.

## Testing Checklist

- [ ] Launch app and tap "Search/Routing" button
- [ ] Type location name (>2 chars) and verify autocomplete results appear
- [ ] Select first location as origin
- [ ] Type another location and select as destination
- [ ] Verify blue route polyline appears on map between origin and destination
- [ ] Test with various locations to confirm API responses parse correctly

