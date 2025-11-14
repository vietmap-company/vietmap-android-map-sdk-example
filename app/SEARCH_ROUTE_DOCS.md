Search & Routing screen — Setup & Implementation Guide

Overview
--------
This document explains how to configure the Vietmap API key for the example app, and describes how the Search & Routing screen (the new screen you added) is implemented. It covers where the API key is read from, how the app talks to Vietmap Autocomplete v4 / Place v4 / Route v3, the runtime flow, style/layer choices, UI behaviors and how to test and troubleshoot.

Checklist
---------
- [ ] Put your Vietmap API key in AndroidManifest meta-data or in BuildConfig (see sections below).
- [ ] Build & run the app.
- [ ] Open "Search/Routing" from the main menu and test search + route.

Where the app reads the API key (centralized provider)
----------------------------------------------------
New class: `vn.vietmap.mapsdkdemo.utils.ApiKeyProvider`
- Function: `ApiKeyProvider.getApiKey(Context)`
- Lookup order:
  1. AndroidManifest meta-data key `vn.vietmap.api_key` (recommended for local/dev).
  2. `BuildConfig.VIETMAP_API_KEY` (if you expose it via Gradle buildConfigField — useful for CI/flavors).
  3. returns `null` if neither exists.

If `getApiKey()` returns null, the app shows a Toast and logs a helpful error; the Search/Route screen will not attempt network calls without the key.

How to provide the API key
-------------------------
Choose either manifest meta-data or a build config field.

A) Manifest meta-data (quick, recommended for local dev)
1. Open `app/src/main/AndroidManifest.xml`.
2. Add this inside the `<application>` tag:

```xml
<meta-data
    android:name="vn.vietmap.api_key"
    android:value="YOUR_REAL_API_KEY_HERE" />
```

B) BuildConfig field (recommended for CI / different flavors)
1. Edit `app/build.gradle.kts` and add a `buildConfigField` into your `defaultConfig` or specific buildType/flavor. Example (Groovy/KTS adapted to your project):

KTS snippet (example):
```kotlin
android {
  defaultConfig {
    // ...
    buildConfigField("String", "VIETMAP_API_KEY", '"' + (project.findProperty("VIETMAP_API_KEY") ?: "") + '"')
  }
}
```
2. Add the real key to `~/.gradle/gradle.properties` or `gradle.properties` (but don't commit it):
```
VIETMAP_API_KEY=your_real_key_here
```
ApiKeyProvider tries BuildConfig.VIETMAP_API_KEY using reflection as a fallback if no manifest meta-data is found.

Files touched for API key centralization
--------------------------------------
- Added: `app/src/main/java/vn/vietmap/mapsdkdemo/utils/ApiKeyProvider.java`
- Edited: `app/src/main/java/vn/vietmap/mapsdkdemo/utils/VietMapTiles.kt` to build style URLs using the provider (now `VietMapTiles.instance.lightVector(context)` etc.).
- Edited: `SearchRouteActivity.java` to use `ApiKeyProvider.getApiKey(this)` for autocomplete/place/route calls.
- Also updated a few UI files to use `VietMapTiles` helpers (so style URLs include the centralized key): `VietmapGL.kt`, `VietmapScreen.kt`, `VietMapViewPager.kt`.

Note: Other files may still contain literal style URLs or "YOUR_API_KEY_HERE" placeholders; the provider centralizes key lookup so new calls should use `VietMapTiles` or `ApiKeyProvider` directly.

Search & Routing implementation — high-level flow
-----------------------------------------------
The Search & Routing screen follows the Vietmap APIs flow:

1) Autocomplete (Autocomplete v4)
- Endpoint: GET https://maps.vietmap.vn/api/autocomplete/v4
- Required params: `apikey`, `text`
- Optional / commonly used: `focus` ("lat,lng"), `display_type` (the app uses 6 to include both display & data_new), `cityId`, `distId`, `wardId`, `cats`, `layers`, `circle_center`, `circle_radius`.
- Retrofit interface: `VietmapSearchService.autocomplete(...)` (returns Call<List<VietmapAutocompleteResponse>>)
- Response mapping: `VietmapAutocompleteResponse` model with fields: `ref_id`, `display`, `name`, `data_new` (and nested `ref_id`), `boundaries`, `distance`, etc.
- Implementation details: `SearchRouteActivity.performAutocomplete()` calls `autocomplete(...)` and displays `display` text in the suggestion list.

2) Place detail lookup (Place v4)
- Endpoint: GET https://maps.vietmap.vn/api/place/v4
- Required params: `apikey`, `refid` (e.g. `data_new.ref_id` or `ref_id` from autocomplete item)
- Response mapping: `VietmapPlaceDetailResponse` model with `lat` and `lng` fields (and address fields). The activity calls `searchService.placeDetail(apiKey, refId, null)`.
- Implementation details: `SearchRouteActivity.fetchPlaceDetails(refId)` parses `lat`/`lng` into the app LatLng model and stores selected point as origin or destination.

3) Routing (Route v3)
- Endpoint: GET https://maps.vietmap.vn/api/route/v3
- Important: The API expects repeated `point` query parameters. The code sends points as lat,lng strings (latitude first) according to your requirement, e.g. `point=16.0,107.0&point=10.0,106.0`.
- Retrofit interface: `VietmapRouteService.route(String apikey, List<String> points, String vehicle, String apiVersion, Boolean calcPoints, Boolean instructions, String locale)` — Retrofit will emit repeated `point` params for a List<String>.
- Response mapping: `VietmapRouteResponse` containing `paths`, where `paths[0].points` is an encoded polyline string.
- Implementation details: `SearchRouteActivity.fetchRoute()` builds the `List<String>` points (latitude first) and calls the route service. After response, it extracts `paths.get(0).points` and passes it to the polyline decode/display.

Polyline decoding, drawing and camera
------------------------------------
- Polyline decoding: `VietMapPolyline.decode(encoded)` decodes the encoded polyline into a List<vn.vietmap.vietmapsdk.geometry.LatLng>.
- Drawing the route:
  - Preferred: The app creates a GeoJSON LineString and adds a `GeoJsonSource` + `LineLayer` to the map `style` so it is rendered as a style layer.
  - Layer order: The layer is added below the label layer if available (the implementation tries `vmadmin_label`, falling back to `vmadmin_province`) so the route is drawn under map text (as you requested).
  - Fallback: If the style isn't available for any reason the code draws a legacy annotation polyline so users still see the route.
- Camera: After adding the route the app computes a LatLngBounds from the decoded polyline points and animates the camera to fit with padding. For a single-point route it performs a zoom to that point instead.

UI behaviors implemented
-----------------------
- Search input and results list
  - The search input triggers autocomplete after 3 characters.
  - Results appear in the ListView with a dark text color and visible background so results are readable on top of the map.
- Selection flow
  - First selection becomes `origin` (clears input and asks user to select destination).
  - Second selection becomes `destination` and triggers route fetch and drawing.
- Keyboard
  - On successful route draw, the soft keyboard is hidden and the search input loses focus.
- Clear route
  - `Clear Route` removes the route layer & source from the style and clears any annotation polyline. UI resets to origin selection state.
- Initial camera
  - The map is centered on `LatLng(16.385593, 107.515788)` with zoom `5.0` during initialization.

Key files involved (quick map)
-----------------------------
- `app/src/main/java/vn/vietmap/mapsdkdemo/ui/SearchRouteActivity.java`
  - Main activity for search & routing UI.
  - Implements the full network flow + UI logic described above.
- `app/src/main/java/vn/vietmap/mapsdkdemo/ui/VietmapSearchRouteApiModels.java`
  - Retrofit interfaces (`VietmapSearchService`, `VietmapRouteService`) and request/response POJOs for Autocomplete v4, Place v4 and Route v3.
- `app/src/main/java/vn/vietmap/mapsdkdemo/utils/VietMapPolyline.java`
  - Encoded polyline decoder used to build a LatLng path for drawing.
- `app/src/main/java/vn/vietmap/mapsdkdemo/utils/ApiKeyProvider.java`
  - Newly added API key centralized lookup.
- `app/src/main/java/vn/vietmap/mapsdkdemo/utils/VietMapTiles.kt`
  - Central tile/style URL helper now building URLs with the centralized API key.
- Layout: `app/src/main/res/layout/activity_search_route.xml`
  - The UI layout for the Search & Routing screen (card with search field, results list, buttons).

How the Retrofit calls are wired
--------------------------------
- A `Retrofit` instance is created with base URL `https://maps.vietmap.vn` and `GsonConverterFactory`.
- `searchService = retrofit.create(VietmapSearchService.class)`
- `routeService = retrofit.create(VietmapRouteService.class)`
- The typed interfaces and the models are defined in `VietmapSearchRouteApiModels.java` and match the Vietmap docs field names (e.g., `ref_id`, `display`, `lat`, `lng`, `points`).

Configuration & quick run
-------------------------
1) Put your API key in one of the supported places (Manifest meta-data or BuildConfig as shown above).
2) Build and install the app (example commands):

```bash
# from the project root (zsh)
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

3) Launch the app and open "Search/Routing".

Expected runtime behavior
-------------------------
- When typing into the search box, suggestions appear (autocomplete v4). If nothing appears, check network logs and make sure the API key is correctly configured.
- Selecting two locations draws the route on the map under text labels, and the camera zooms to fit the route; keyboard is hidden on success.

Troubleshooting
---------------
- API key missing or wrong
  - Symptom: Toast "API key not configured" or no tiles / 401 responses in network logs.
  - Fix: Ensure `vn.vietmap.api_key` is set in AndroidManifest or `VIETMAP_API_KEY` is present at build time. Check logcat for the error message.

- No autocomplete results
  - Symptom: suggestions empty.
  - Fix: Confirm the search query is sent (network logging) and the API key is valid. Try setting `focus` param to a lat,lng near your query if the results are too broad.

- Place detail returns no coordinates
  - Symptom: place response missing lat/lng.
  - Fix: verify the `ref_id` used is the one returned by Autocomplete (prefer `data_new.ref_id` when present). Check the place v4 response for lat/lng.

- Route does not draw or is behind labels
  - Symptom: route not visible or overlapped by other layers.
  - Fix: Route is added as a style LineLayer below the map label layer (attempts `vmadmin_label`, falls back to `vmadmin_province`). If your style uses different label layer ids change the anchor string in `SearchRouteActivity` where `belowLayer` is computed.

- Logs & network debugging
  - Use Android Studio's Logcat to view logs printed by the activity (log tags use `SearchRouteActivity`).
  - Use a proxy tool (Charles, mitmproxy) if you want to inspect raw HTTP traffic; make sure the emulator/device trusts the proxy.

Customizing behavior
--------------------
- Change route appearance (color/width) in `SearchRouteActivity.drawRoute()` when creating the `LineLayer` (modify `PropertyFactory.lineColor(...)` and `lineWidth`).
- Change camera padding or animation duration in the same method (padding currently 100 px, duration 1000 ms).
- Change autocomplete `display_type` argument in `performAutocomplete()` if you want different display/data behavior.

Next improvements (suggestions)
------------------------------
- Extract strings into `strings.xml` for i18n instead of in-code string literals.
- Move the API key to Android Gradle `buildConfigField` and keep it out of source control via `gradle.properties` or CI secrets.
- Add persistent origin/destination markers with styles (different icons) and a small info card showing route distance/time.
- Show textual driving instructions parsed from `VietmapRouteResponse.paths[0].instructions`.

If you want, I can now:
- Update the README in the repo (add the same content to `app/SEARCH_ROUTE_DOCS.md`) — I've already created it in the project root inside `app/`.
- Replace any remaining `YOUR_API_KEY_HERE` literals across the project to use `VietMapTiles` or `ApiKeyProvider` uniformly.

Tell me which follow-up to do (update manifest with example meta-data, add Gradle buildConfig snippet to your `build.gradle.kts`, sweep the repo for remaining placeholders and update them, or implement markers/instruction UI).
