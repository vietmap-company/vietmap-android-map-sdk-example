package vn.vietmap.mapsdkdemo.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class ApiKeyProvider {

    private static final String META_API_KEY = "vn.vietmap.api_key";

    /**
     * Returns the API key by checking (in order):
     *  - AndroidManifest meta-data named "vn.vietmap.api_key"
     *  - BuildConfig field "VIETMAP_API_KEY" (if present)
     *  - null if none found
     */
    public static String getApiKey(Context context) {
        if (context == null) return null;
        // 1) Manifest meta-data
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai != null && ai.metaData != null) {
                Object v = ai.metaData.get(META_API_KEY);
                if (v instanceof String) {
                    String s = (String) v;
                    if (!s.isEmpty() && !s.equals("YOUR_API_KEY_HERE")) return s;
                }
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        // 2) BuildConfig fallback (guarded reflection to avoid compile dependency issues)
        try {
            Class<?> bc = Class.forName(context.getPackageName() + ".BuildConfig");
            try {
                java.lang.reflect.Field f = bc.getDeclaredField("VIETMAP_API_KEY");
                f.setAccessible(true);
                Object val = f.get(null);
                if (val instanceof String) {
                    String s = (String) val;
                    if (!s.isEmpty() && !s.equals("YOUR_API_KEY_HERE")) return s;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }
}

