package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UltimateGuitarProvider extends BaseSongProvider {

    private final String TAG = "UltimateGuitarProvider";
    public UltimateGuitarProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "UltimateGuitar";
    }

    @Override
    protected String getSearchUrl(String query) throws Exception {
        // Pass type=300 to restrict the server-side search strictly to Chords
        // Pass type=300 to restrict the server-side search strictly to Chords
        return "https://www.ultimate-guitar.com/search.php?search_type=title&value=" +
                URLEncoder.encode(query, "UTF-8") + "&type=300";
    }

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> matches = new ArrayList<>();
        Element storeDiv = doc.selectFirst(".js-store");

        if (storeDiv != null) {
            String jsonContent = storeDiv.attr("data-content");
            try {
                JSONObject json = new JSONObject(jsonContent);
                JSONObject data = json.getJSONObject("store").getJSONObject("page").getJSONObject("data");
                JSONArray results = data.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    // Check for song_name and either tab_url or url
                    if (item.has("song_name") && (item.has("tab_url") || item.has("url"))) {
                        String title = item.getString("song_name");
                        String artist = item.optString("artist_name", "Unknown");
                        String url = item.optString("tab_url", item.optString("url", ""));
                        String type = item.optString("type", "");

                        // Extract rating and votes/count
                        double rating = item.optDouble("rating", 0.0);
                        int votes = item.optInt("votes", 0); // or try "rating_votes" if votes is 0

                        if ("Chords".equalsIgnoreCase(type)) {
                            // Example: If your SongMatch constructor or model supports rating/votes,
                            // you can pass them along here, or log them to test:

                            SongMatch match = new SongMatch(title, artist, url, getName());
                            match.setRating(rating);
                            match.setVotes(votes);

                            matches.add(match);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSON payload", e);
            }
        } else {
            Log.e(TAG, "Could not find .js-store element in document!");
        }
        // 2. Sort the list: Highest votes first, then highest rating as a tie-breaker
        matches.sort((m1, m2) -> {
            // Compare votes descending (m2 compared to m1)
            int voteComparison = Integer.compare(m2.getVotes(), m1.getVotes());
            if (voteComparison != 0) {
                return voteComparison;
            }
            // If votes are equal, compare rating descending
            return Double.compare(m2.getRating(), m1.getRating());
        });

        return matches;
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new UltimateGuitar(context); // Passes the context right in!
    }
}