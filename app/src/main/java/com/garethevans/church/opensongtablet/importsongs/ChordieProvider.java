package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ChordieProvider extends BaseSongProvider {

    private final String TAG = "ChordieProvider";

    public ChordieProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "Chordie";
    }

    @Override
    protected String getSearchUrl(String query) throws Exception {
        // Chordie search URL format
        return "https://www.chordie.com/result.php?q=" + URLEncoder.encode(query, "UTF-8");
    }

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> matches = new ArrayList<>();

        Elements songItems = doc.select("div.songList");
        Log.d(TAG, "parseSearchResults: Found " + songItems.size() + " songList elements.");

        for (Element item : songItems) {
            Element link = item.selectFirst("div.songListContent a[href]");
            if (link != null) {
                String url = link.absUrl("href");

                // Filter out artist-level index pages (e.g., /songartist/)
                if (url.contains("/songartist/") || !url.contains("chordie.com")) {
                    continue;
                }

                Element titleSpan = link.selectFirst("span[style*='font-size: 18px']");
                String title = titleSpan != null ? titleSpan.text().trim() : link.text().trim();

                Element artistSpan = link.selectFirst("span[style*='color: grey']");
                String artist = artistSpan != null ? artistSpan.text().trim() : "Unknown";

                double rating = 0.0;
                Element ratingEl = item.selectFirst(".rateStar div[style*='font-size: 17px']");
                if (ratingEl != null) {
                    try {
                        rating = Double.parseDouble(ratingEl.text().trim());
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Could not parse rating value: " + ratingEl.text());
                    }
                }

                if (title.isEmpty() || url.isEmpty()) {
                    continue;
                }

                Log.d(TAG, String.format("Found Valid Song Match -> Title: '%s' | Artist: '%s' | Rating: %.1f | URL: '%s'",
                        title, artist, rating, url));

                SongMatch match = new SongMatch(title, artist, url, getName());
                match.setRating(rating);
                match.setVotes(0);
                matches.add(match);
            }
        }

        // Sort by rating descending (highest rated first)
        matches.sort((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()));

        Log.d(TAG, "parseSearchResults: Successfully mapped " + matches.size() + " valid song matches for Chordie.");
        return matches;
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new Chordie(); // Adjust to match your Chordie parser implementation class
    }
}