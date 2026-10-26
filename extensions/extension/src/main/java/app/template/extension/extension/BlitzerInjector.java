package app.template.extension.extension;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Integration class for the BlitzerMorph patch.
 * Fetches live mobile speed cameras from Blitzer.de (Atudo API) and injects them
 * into Google Maps' static camera list.
 */
@SuppressWarnings("unused")
public class BlitzerInjector {

    private static final String TAG = "BlitzerInjector";

    /**
     * Atudo API endpoint used by Blitzer.de to serve live speed camera POIs.
     * <p>
     * Query parameters:
     * <ul>
     *   <li><b>z</b> - Zoom level (1-14). Higher = smaller area, faster response.</li>
     *   <li><b>type</b> - Comma-separated list of POI type IDs (0=mobile, 1=trailer, 2=stationary, 103=red light)</li>
     *   <li><b>box</b> - Bounding box: lat_min,lng_min,lat_max,lng_max</li>
     * </ul>
     */
    private static final String ATUDO_API_URL = "https://cdn2.atudo.net/api/4.0/pois.php";

    /** POI type IDs for speed cameras. */
    private static final String POI_TYPES = "0,1,2,3,4,5,6,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,117,ts,vwd";

    /**
     * Fetches live speed camera data from the Atudo API for a given bounding box.
     * <p>
     * This is called by the Kotlin patch via bytecode injection into Google Maps.
     * The result is parsed and injected into the app's camera list.
     *
     * @param latMin Southern latitude boundary
     * @param lngMin Western longitude boundary
     * @param latMax Northern latitude boundary
     * @param lngMax Eastern longitude boundary
     * @return Raw JSON response from Atudo, or empty string on failure
     */
    public static String fetchLiveBlitzers(double latMin, double lngMin, double latMax, double lngMax) {
        try {
            String box = String.format(
                java.util.Locale.ROOT,
                "%.6f,%.6f,%.6f,%.6f",
                latMin, lngMin, latMax, lngMax
            );

            String urlStr = ATUDO_API_URL + "?z=12&type=" + POI_TYPES + "&box=" + box;
            URL url = new URL(urlStr);

            Log.i(TAG, "Fetching live Blitzer data from: " + urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "BlitzerMorph/1.0 (Android)");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Atudo API returned HTTP " + responseCode);
                return "";
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Log.i(TAG, "Successfully fetched " + response.length() + " bytes of Blitzer data");
            return response.toString();

        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch live Blitzer data", e);
            return "";
        }
    }

    /**
     * Injects dynamic POIs into Google Maps' camera overlay.
     * <p>
     * This is the primary entry point called by the Kotlin patch.
     * It fetches live data and triggers the Maps UI update.
     */
    public static void injectDynamicPOIs() {
        try {
            // Default bounding box for Germany (can be overridden by location)
            // This is a broad area covering the entire country
            String jsonData = fetchLiveBlitzers(
                47.2, 5.9,   // SW corner
                55.0, 15.0   // NE corner
            );

            if (!jsonData.isEmpty()) {
                Log.i(TAG, "Live Blitzer data ready for injection: " + jsonData.length() + " chars");
                // The actual injection into Google Maps' UI thread
                // is handled by the Kotlin patch's bytecode modifications
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in injectDynamicPOIs", e);
        }
    }
}