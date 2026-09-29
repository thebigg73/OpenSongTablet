package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.interfaces.SongProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WorshipTogetherProvider extends BaseSongProvider {

    private final String TAG = "WorshipTogetherProvider";

    public WorshipTogetherProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "WorshipTogether";
    }

    // Not used directly since we override searchSongs, but required by BaseSongProvider
    @Override
    protected String getSearchUrl(String query) throws Exception {
        // Standard URL format used by Worship Together's search
        return "https://www.worshiptogether.com/search-results/?cludoquery=" + URLEncoder.encode(query, "UTF-8");
    }

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> matches = new ArrayList<>();

        // Select each result item block from Cludo's HTML structure
        for (org.jsoup.nodes.Element item : doc.select("li.search-results-item")) {
            org.jsoup.nodes.Element link = item.selectFirst("a[href]");
            if (link != null) {
                String url = link.attr("abs:href");
                if (url.isEmpty()) {
                    url = link.attr("href"); // Fallback if absolute conversion needs a base
                }

                // Extract the clean song title from the <h2> tag or link attribute
                org.jsoup.nodes.Element h2 = item.selectFirst("h2");
                String title = "";
                String artist = "Unknown";

                if (h2 != null) {
                    // The h2 text usually contains "Title - Artist1, Artist2..."
                    String fullText = h2.text();
                    if (fullText.contains("-")) {
                        String[] parts = fullText.split("-", 2);
                        title = parts[0].trim();
                        artist = parts[1].trim();
                    } else {
                        title = fullText.trim();
                    }
                } else {
                    title = link.attr("data-cludo-title");
                }

                if (url.startsWith("/")) {
                    url = "https://www.worshiptogether.com" + url;
                }

                if (!title.isEmpty() && !url.isEmpty()) {
                    matches.add(new SongMatch(title, artist, url, getName()));
                }
            }
        }

        Log.d(TAG, "Successfully parsed matches count: " + matches.size());
        return matches;
    }

    @Override
    public void searchSongs(String query, SongProvider.OnSearchResultsListener listener) {
        executor.execute(() -> {
            final List<SongMatch> matches = new ArrayList<>();
            try {
                OkHttpClient client = new OkHttpClient();

                // Build the exact JSON payload captured from the browser
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("ResponseType", "JsonHtml");
                jsonBody.put("Template", "SearchContent");
                jsonBody.put("page", 1);
                jsonBody.put("query", query);
                jsonBody.put("text", "");
                jsonBody.put("enableRelatedSearches", false);
                jsonBody.put("applyMultiLevelFacets", true);

                jsonBody.put("facets", new JSONObject()
                        .put("Category", new JSONArray())
                        .put("Tempo", new JSONArray())
                        .put("Theme", new JSONArray())
                        .put("RecommendedKey", new JSONArray())
                        .put("Ministry", new JSONArray())
                        .put("Writer", new JSONArray())
                        .put("OriginalKey", new JSONArray()));
                jsonBody.put("filters", new JSONObject());
                jsonBody.put("traits", new JSONArray());
                jsonBody.put("sort", new JSONObject());
                jsonBody.put("rangeFacets", new JSONObject());
                jsonBody.put("perPage", JSONObject.NULL);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

                String url = "https://api-us1.cludo.com/api/v3/10000995/10001572/search";

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json;charset=UTF-8")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "SiteKey MTAwMDA5OTU6MTAwMDE1NzI6U2VhcmNoS2V5")
                        .addHeader("Origin", "https://www.worshiptogether.com")
                        .addHeader("Referer", "https://www.worshiptogether.com/")
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/153.0.0.0 Safari/537.36")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    Log.d(TAG, "Response Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponseString = response.body().string();

                        // --- LOG THE ENTIRE JSON RESPONSE ---
                        Log.d(TAG, "Full Cludo JSON: " + jsonResponseString);
                        // ------------------------------------

                        // Parse Cludo's JSON response wrapper
                        JSONObject rootObject = new JSONObject(jsonResponseString);
                        String htmlContent = rootObject.optString("SearchResult", "");
                        if (htmlContent.isEmpty()) {
                            htmlContent = rootObject.optString("ResultHtml", "");
                        }

                        // --- ADD THIS LOG TO INSPECT CLUDO'S HTML ---
                        Log.d(TAG, "Cludo HTML Payload: " + (htmlContent.length() > 500 ? htmlContent.substring(0, 500) : htmlContent));
                        // ------------------------------------------

                        if (!htmlContent.isEmpty()) {
                            // Parse the HTML returned inside Cludo's JSON response using Jsoup
                            Document doc = Jsoup.parse(htmlContent);
                            List<SongMatch> results = parseSearchResults(doc);
                            if (results != null) {
                                matches.addAll(results);
                            }
                        }
                    } else {
                        Log.e(TAG, "Request failed with code: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "WorshipTogether Search error", e);
            }
            handler.post(() -> listener.onResultsLoaded(matches));
        });
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new WorshipTogether();
    }
}