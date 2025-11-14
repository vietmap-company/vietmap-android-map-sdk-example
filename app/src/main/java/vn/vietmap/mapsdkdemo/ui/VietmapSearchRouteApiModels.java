package vn.vietmap.mapsdkdemo.ui;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Retrofit interfaces & POJOs matching Vietmap autocomplete v4, place v4, and route v3 responses
// EXACT MATCH with official documentation:
// https://maps.vietmap.vn/docs/map-api/autocomplete-version/autocomplete-v4/
// https://maps.vietmap.vn/docs/map-api/place-v4/
// https://maps.vietmap.vn/docs/map-api/route-version/route-v3/

interface VietmapSearchService {

    // GET https://maps.vietmap.vn/api/autocomplete/v4
    // Required: apikey, text
    // Optional: focus (lat,lng), display_type, cityId, distId, wardId, circle_center, circle_radius, cats, layers
    @GET("/api/autocomplete/v4")
    Call<List<VietmapAutocompleteResponse>> autocomplete(
            @Query("apikey") String apiKey,
            @Query("text") String text,
            @Query("focus") String focus,              // "lat,lng" format
            @Query("display_type") Integer displayType, // default: 6 (both display and data_new)
            @Query("cityId") Integer cityId,
            @Query("distId") Integer distId,
            @Query("wardId") Integer wardId,
            @Query("circle_center") String circleCenter, // "lat,lng"
            @Query("circle_radius") Integer circleRadius, // meters
            @Query("cats") String cats,                // POI categories
            @Query("layers") String layers             // POI, ADDRESS, VILLAGE, WARD, DIST, CITY, STREET
    );

    // GET https://maps.vietmap.vn/api/place/v4
    // Required: refid, apikey
    // Optional: api-version
    @GET("/api/place/v4")
    Call<VietmapPlaceDetailResponse> placeDetail(
            @Query("apikey") String apiKey,
            @Query("refid") String refid,
            @Query("api-version") String apiVersion
    );
}

interface VietmapRouteService {

    // GET https://maps.vietmap.vn/api/route/v3
    // Required: point (multiple), vehicle, apikey
    // Optional (commonly used): api-version, calc_points, locale, instructions
    // We model origin and destination as a repeated query param: List<String> points (each "lon,lat").
    @GET("/api/route/v3")
    Call<VietmapRouteResponse> route(
            @Query("apikey") String apiKey,
            @Query("point") List<String> points,
            @Query("vehicle") String vehicle,
            @Query("api-version") String apiVersion,
            @Query("calc_points") Boolean calcPoints,
            @Query("instructions") Boolean instructions,
            @Query("locale") String locale
    );
}

// =======================
//  Autocomplete v4 models - EXACT MATCH with docs
// =======================
// Response is a direct array of autocomplete items (not wrapped in FeatureCollection)

class VietmapAutocompleteResponse {
    public String ref_id;           // Reference ID for place detail API
    public double distance;         // Distance from focus point
    public String address;          // Full address
    public String name;             // Place name
    public String display;          // Display text combining name + address
    public List<Boundary> boundaries; // Administrative boundaries
    public List<String> categories; // POI categories
    public List<EntryPoint> entry_points; // Entry points
    public DataNew data_new;        // Alternative/new data
    public DataOld data_old;        // Old data (if any)

    public static class Boundary {
        public int type;            // 0=city, 1=district, 2=ward
        public int id;              // Boundary ID
        public String name;         // Boundary name
        public String prefix;       // "Thành Phố", "Quận", "Phường", etc.
        public String full_name;    // Full name with prefix
    }

    public static class EntryPoint {
        // Entry point structure (if provided by API)
    }

    public static class DataNew {
        public String ref_id;
        public double distance;
        public String address;
        public String name;
        public String display;
        public List<Boundary> boundaries;
        public List<String> categories;
        public List<EntryPoint> entry_points;
        public Object data_new;
        public Object data_old;
    }

    public static class DataOld {
        // Old data structure (if provided by API)
    }
}

// =======================
//  Place v4 models - EXACT MATCH with docs
// =======================
// Response is a flat object with lat/lng coordinates directly

class VietmapPlaceDetailResponse {
    public String display;      // Full display text: "197 Đường Trần Phú,Phường Chợ Quán,Thành Phố Hồ Chí Minh"
    public String name;         // Place name (can be empty)
    public String hs_num;       // House number
    public String street;       // Street name
    public String address;      // Address without administrative boundaries
    public int city_id;         // City ID
    public String city;         // City name
    public int district_id;     // District ID
    public String district;     // District name
    public int ward_id;         // Ward ID
    public String ward;         // Ward name
    public double lat;          // Latitude
    public double lng;          // Longitude
}

// =======================
//  Route v3 models
// =======================

class VietmapRouteResponse {
    public List<Path> paths;

    public static class Path {
        public double distance;
        public double weight;
        public double time;
        public boolean transfers;
        public String points;
        public boolean points_encoded;
        public List<Double> bbox;
        public List<Instruction> instructions;
        public SnappedWaypoints snapped_waypoints;
    }

    public static class Instruction {
        public String text;
        public int street_ref;
        public int sign;
        public List<Integer> interval;
        public long time;
        public String street_name;
        public double distance;
        public double last_heading;
    }

    public static class SnappedWaypoints {
        public String type; // "MultiPoint"
        public List<List<Double>> coordinates;
    }
}
