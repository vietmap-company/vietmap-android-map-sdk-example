package vn.vietmap.mapsdkdemo.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.vietmap.mapsdkdemo.R;
import vn.vietmap.mapsdkdemo.utils.ApiKeyProvider;
import vn.vietmap.mapsdkdemo.utils.IconUtils;
import vn.vietmap.mapsdkdemo.utils.VietMapPolyline;
import vn.vietmap.mapsdkdemo.utils.VietMapTiles;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.geometry.LatLngBounds;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.annotations.Icon;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.style.layers.LineLayer;
import vn.vietmap.vietmapsdk.style.layers.Property;
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory;
import vn.vietmap.vietmapsdk.style.sources.GeoJsonOptions;
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource;

public class SearchRouteActivity extends AppCompatActivity {

    private MapView mapView;
    private VietMapGL vietMapGL;
    private VietmapSearchService searchService;
    private VietmapRouteService routeService;

    private EditText searchInput;
    private ListView searchResults;
    private android.widget.Button searchButton;
    private android.widget.Button clearButton;
    private android.widget.TextView infoText;
    private ArrayAdapter<String> searchAdapter;
    private List<VietmapAutocompleteResponse> currentSearchResults = new ArrayList<>();
    private LatLng origin;
    private LatLng destination;
    private Polyline currentRoutePolyline;

    private vn.vietmap.vietmapsdk.maps.Style style; // keep reference to the loaded style
    private static final String ROUTE_SOURCE_ID = "route_source";
    private static final String ROUTE_LAYER_ID = "route_layer";

    private Marker originMarker;
    private Marker destMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Vietmap SDK
        Vietmap.getInstance(this);

        setContentView(R.layout.activity_search_route);

        mapView = findViewById(R.id.vietmap_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(vietMapGL -> {
            this.vietMapGL = vietMapGL;
            // Set map style using VietMapTiles utility and capture the loaded style
            vietMapGL.setStyle(
                new Style.Builder().fromUri(VietMapTiles.Companion.getInstance().lightVector(getApplicationContext())),
                loadedStyle -> {
                    this.style = loadedStyle;

                    // Move center on init to 16.385593, 107.515788 with zoom level 5
                    try {
                        vietMapGL.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.385593, 107.515788), 5.0));
                    } catch (Exception ex) {
                        Log.w("SearchRouteActivity", "Failed to move initial camera", ex);
                    }
                }
            );
        });

        searchInput = findViewById(R.id.search_input);
        searchResults = findViewById(R.id.search_results);
        searchButton = findViewById(R.id.search_button);
        clearButton = findViewById(R.id.clear_button);
        infoText = findViewById(R.id.info_text);

        setupRetrofit();
        setupSearch();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.vietmap.vn")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // use interfaces defined in VietmapSearchRouteApiModels.java
        searchService = retrofit.create(VietmapSearchService.class);
        routeService = retrofit.create(VietmapRouteService.class);
    }

    private void setupSearch() {
        // Use custom adapter with dark text and light background for visibility
        searchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>()) {
            @NonNull
            @Override
            public android.view.View getView(int position, android.view.View convertView, @NonNull android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(android.graphics.Color.BLACK);
                textView.setTextSize(14);
                textView.setPadding(16, 12, 16, 12);
                view.setBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"));
                return view;
            }
        };
        searchResults.setAdapter(searchAdapter);

        // ensure list has a visible background and elevation so it appears above the map
        searchResults.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
        searchResults.setDividerHeight(1);

        // Search button click
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (query.length() > 2) {
                performAutocomplete(query);
            } else {
                Toast.makeText(this, "Please enter at least 3 characters", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear button click
        clearButton.setOnClickListener(v -> {
            clearRoute();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    performAutocomplete(s.toString());
                } else {
                    searchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        searchResults.setOnItemClickListener((parent, view, position, id) -> {
            VietmapAutocompleteResponse selectedItem = currentSearchResults.get(position);
            handleSearchResultSelection(selectedItem);
        });
    }

    private void clearRoute() {
        origin = null;
        destination = null;
        // Remove route layer/source if present
        if (style != null) {
            try {
                if (style.getLayer(ROUTE_LAYER_ID) != null) {
                    style.removeLayer(ROUTE_LAYER_ID);
                }
            } catch (Exception ignored) {}
            try {
                if (style.getSource(ROUTE_SOURCE_ID) != null) {
                    style.removeSource(ROUTE_SOURCE_ID);
                }
            } catch (Exception ignored) {}
        }
        // also clear any annotation-based polyline if still present
        if (currentRoutePolyline != null && vietMapGL != null) {
            try { vietMapGL.removePolyline(currentRoutePolyline); } catch (Exception ignored) {}
            currentRoutePolyline = null;
        }
        // remove origin/destination markers
        if (originMarker != null && vietMapGL != null) {
            try { vietMapGL.removeMarker(originMarker); } catch (Exception ignored) {}
            originMarker = null;
        }
        if (destMarker != null && vietMapGL != null) {
            try { vietMapGL.removeMarker(destMarker); } catch (Exception ignored) {}
            destMarker = null;
        }

        // restore search form visibility
        searchInput.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        searchResults.setVisibility(View.GONE);

        searchInput.setText("");
        clearButton.setVisibility(View.GONE);
        infoText.setText("Select origin first, then destination");
        infoText.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Route cleared", Toast.LENGTH_SHORT).show();
    }

    private void updateInfoText() {
        if (origin == null) {
            infoText.setText("Select origin first");
            infoText.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.GONE);
        } else if (destination == null) {
            infoText.setText("Now select destination");
            infoText.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);
        } else {
            infoText.setText("Route displayed");
            infoText.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    private void performAutocomplete(String query) {
        String apiKey = ApiKeyProvider.getApiKey(this);
        if (apiKey == null) {
            Toast.makeText(this, "API key not configured", Toast.LENGTH_SHORT).show();
            Log.e("SearchRouteActivity", "API key missing - please configure vn.vietmap.api_key in AndroidManifest or BuildConfig.VIETMAP_API_KEY");
            return;
        }

        // Call autocomplete v4 API with correct parameters matching docs
        // focus: "lat,lng", display_type: 6 (show both display and data_new)
        searchService.autocomplete(
                apiKey,
                query,
                null,  // focus - can set to user location "10.758867,106.675566"
                6,     // display_type: 6 shows both display and data_new
                null,  // cityId
                null,  // distId
                null,  // wardId
                null,  // circle_center
                null,  // circle_radius
                null,  // cats
                null   // layers
        ).enqueue(new Callback<List<VietmapAutocompleteResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<VietmapAutocompleteResponse>> call,
                                   @NonNull Response<List<VietmapAutocompleteResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentSearchResults = response.body();
                    List<String> displayNames = currentSearchResults.stream()
                            .map(item -> item.display != null ? item.display : item.name)
                            .collect(Collectors.toList());
                    searchAdapter.clear();
                    searchAdapter.addAll(displayNames);
                    searchAdapter.notifyDataSetChanged();
                    searchResults.setVisibility(View.VISIBLE);
                } else {
                    searchResults.setVisibility(View.GONE);
                    Toast.makeText(SearchRouteActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VietmapAutocompleteResponse>> call, @NonNull Throwable t) {
                Log.e("SearchRouteActivity", "Autocomplete failed", t);
                Toast.makeText(SearchRouteActivity.this, "Search error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                searchResults.setVisibility(View.GONE);
            }
        });
    }

    private void handleSearchResultSelection(VietmapAutocompleteResponse item) {
        String displayName = item.display != null ? item.display : item.name;
        if (displayName != null) searchInput.setText(displayName);
        searchResults.setVisibility(View.GONE);

        String refId = null;
        // Prefer data_new.ref_id if present (per docs), otherwise top-level ref_id
        if (item.data_new != null && item.data_new.ref_id != null && !item.data_new.ref_id.isEmpty()) {
            refId = item.data_new.ref_id;
        } else if (item.ref_id != null && !item.ref_id.isEmpty()) {
            refId = item.ref_id;
        }

        if (refId == null) {
            Toast.makeText(this, "No reference ID for this result", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call place detail API to get coordinates
        fetchPlaceDetails(refId, displayName);
    }

    private void fetchPlaceDetails(String refId, String displayName) {
        String apiKey = ApiKeyProvider.getApiKey(this);
        if (apiKey == null) {
            Toast.makeText(this, "API key not configured", Toast.LENGTH_SHORT).show();
            Log.e("SearchRouteActivity", "API key missing - please configure vn.vietmap.api_key in AndroidManifest or BuildConfig.VIETMAP_API_KEY");
            return;
        }

        searchService.placeDetail(apiKey, refId, null)
                .enqueue(new Callback<VietmapPlaceDetailResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VietmapPlaceDetailResponse> call,
                                           @NonNull Response<VietmapPlaceDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            VietmapPlaceDetailResponse place = response.body();

                            // Place v4 returns lat/lng directly
                            double lat = place.lat;
                            double lon = place.lng;
                            LatLng selectedLatLng = new LatLng(lat, lon);

                            if (origin == null) {
                                origin = selectedLatLng;
                                Toast.makeText(SearchRouteActivity.this,
                                    "Origin selected: " + displayName, Toast.LENGTH_SHORT).show();
                                searchInput.setText("");
                                updateInfoText();
                            } else {
                                destination = selectedLatLng;
                                Toast.makeText(SearchRouteActivity.this,
                                    "Destination selected. Fetching route...", Toast.LENGTH_SHORT).show();
                                updateInfoText();
                                fetchRoute();
                            }
                        } else {
                            Toast.makeText(SearchRouteActivity.this,
                                "Could not get coordinates for this location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VietmapPlaceDetailResponse> call, @NonNull Throwable t) {
                        Log.e("SearchRouteActivity", "Place detail failed", t);
                        Toast.makeText(SearchRouteActivity.this,
                            "Failed to get location details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchRoute() {
        if (origin == null || destination == null) {
            return;
        }

        String apiKey = ApiKeyProvider.getApiKey(this);
        if (apiKey == null) {
            Toast.makeText(this, "API key not configured", Toast.LENGTH_SHORT).show();
            Log.e("SearchRouteActivity", "API key missing - please configure vn.vietmap.api_key in AndroidManifest or BuildConfig.VIETMAP_API_KEY");
            return;
        }
        // Vietmap expects points as lat,lon (user requested format) and as repeated query params
        String originPoint = origin.getLatitude() + "," + origin.getLongitude();
        String destPoint = destination.getLatitude() + "," + destination.getLongitude();

        List<String> points = new ArrayList<>();
        points.add(originPoint);
        points.add(destPoint);

        routeService.route(apiKey, points, "car", "1.1", true, true, "en")
                .enqueue(new Callback<VietmapRouteResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VietmapRouteResponse> call, @NonNull Response<VietmapRouteResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().paths != null && !response.body().paths.isEmpty()) {
                            String encodedPolyline = response.body().paths.get(0).points;
                            drawRoute(encodedPolyline);
                        } else {
                            Toast.makeText(SearchRouteActivity.this, "Failed to get route", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VietmapRouteResponse> call, @NonNull Throwable t) {
                        Log.e("SearchRouteActivity", "Route fetch failed", t);
                        Toast.makeText(SearchRouteActivity.this, "Route fetch error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawRoute(String encodedPolyline) {
        if (vietMapGL == null || encodedPolyline == null || encodedPolyline.isEmpty()) {
            return;
        }

        // remove any existing route layer/source first
        if (style != null) {
            try {
                if (style.getLayer(ROUTE_LAYER_ID) != null) style.removeLayer(ROUTE_LAYER_ID);
            } catch (Exception ignored) {}
            try {
                if (style.getSource(ROUTE_SOURCE_ID) != null) style.removeSource(ROUTE_SOURCE_ID);
            } catch (Exception ignored) {}
        }

        // Still keep annotation removal for backward compatibility
        if (currentRoutePolyline != null) {
            try { vietMapGL.removePolyline(currentRoutePolyline); } catch (Exception ignored) {}
            currentRoutePolyline = null;
        }

        List<LatLng> decodedPath = VietMapPolyline.decode(encodedPolyline);
        if (!decodedPath.isEmpty()) {
            // Build GeoJSON LineString using com.mapbox.geojson Points (lon, lat)
            List<Point> geoPoints = new ArrayList<>();
            for (LatLng p : decodedPath) {
                // Note: com.mapbox.geojson.Point takes (lon, lat)
                geoPoints.add(Point.fromLngLat(p.getLongitude(), p.getLatitude()));
            }
            LineString lineString = LineString.fromLngLats(geoPoints);
            Feature feature = Feature.fromGeometry(lineString);
            FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);

            if (style != null) {
                try {
                    GeoJsonSource routeSource = new GeoJsonSource(ROUTE_SOURCE_ID, featureCollection, new GeoJsonOptions().withLineMetrics(true));
                    style.addSource(routeSource);

                    LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);
                    routeLayer.withProperties(
                            PropertyFactory.lineColor(Color.parseColor("#1976D2")),
                            PropertyFactory.lineWidth(6.0f),
                            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                    );

                    // Try to add below a label layer if present; fallback to vmadmin_province
                    String belowLayer = "vmadmin_province";
                    try {
                        if (style.getLayer("vmadmin_label") != null) {
                            belowLayer = "vmadmin_label";
                        }
                    } catch (Exception ignored) {}

                    style.addLayerBelow(routeLayer, belowLayer);
                } catch (Exception ex) {
                    Log.w("SearchRouteActivity", "Failed to add route layer to style", ex);
                }
            } else {
                // If style not available, fallback to drawing annotation polyline so user still sees route
                try {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(decodedPath)
                            .color(Color.BLUE);
                    currentRoutePolyline = vietMapGL.addPolyline(polylineOptions);
                } catch (Exception ex) {
                    Log.w("SearchRouteActivity", "Failed to add annotation polyline", ex);
                }
            }

            // Hide keyboard on route draw success
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }
                searchInput.clearFocus();
            } catch (Exception ex) {
                Log.w("SearchRouteActivity", "Failed to hide keyboard", ex);
            }

            // Hide suggestion form (keep clear button visible) and show origin/destination markers
            try {
                searchInput.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
                searchResults.setVisibility(View.GONE);
                clearButton.setVisibility(View.VISIBLE);

                // add markers for origin and destination
                if (vietMapGL != null) {
                    // remove previous markers if any
                    if (originMarker != null) {
                        try { vietMapGL.removeMarker(originMarker); } catch (Exception ignored) {}
                        originMarker = null;
                    }
                    if (destMarker != null) {
                        try { vietMapGL.removeMarker(destMarker); } catch (Exception ignored) {}
                        destMarker = null;
                    }

                    if (origin != null) {
                        Icon originIcon = new IconUtils().drawableToIcon(this, R.drawable.ic_my_location, ResourcesCompat.getColor(getResources(), R.color.blueAccent, getTheme()));
                        originMarker = vietMapGL.addMarker(new MarkerOptions().position(origin).title("Origin").icon(originIcon));
                    }
                    if (destination != null) {
                        Icon destIcon = new IconUtils().drawableToIcon(this, R.drawable.ic_my_location, ResourcesCompat.getColor(getResources(), R.color.blueAccent, getTheme()));
                        destMarker = vietMapGL.addMarker(new MarkerOptions().position(destination).title("Destination").icon(destIcon));
                    }
                }
            } catch (Exception ex) {
                Log.w("SearchRouteActivity", "Failed to show markers or hide suggestion form", ex);
            }

            // Zoom to bounding box of the route
            try {
                if (decodedPath.size() == 1) {
                    LatLng single = decodedPath.get(0);
                    vietMapGL.animateCamera(CameraUpdateFactory.newLatLngZoom(single, 14.0));
                } else {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng p : decodedPath) {
                        builder.include(p);
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // px
                    vietMapGL.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1000);
                }
            } catch (Exception ex) {
                Log.w("SearchRouteActivity", "Failed to animate camera to route bounds", ex);
            }
        }
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
