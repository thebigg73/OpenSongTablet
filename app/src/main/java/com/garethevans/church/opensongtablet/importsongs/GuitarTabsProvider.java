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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuitarTabsProvider extends BaseSongProvider {

    private final String TAG = "GuitarTabsProvider";
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String BASE_URL = "https://www.guitartabs.cc";

    public GuitarTabsProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "GuitarTabs";
    }

    // Regex to match text inside parentheses: e.g., "(Proclaimers)"
    private final Pattern PARENTHESIS_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    // Not used directly since we override searchSongs, but required by BaseSongProvider
    @Override
    protected String getSearchUrl(String query) throws Exception {
        // If the search includes an artist in parentheses, then extract it
        // 1. Parse query to extract artist and song title
        ParsedQuery parsed = parseQuery(query);

        // 2. Build the GuitarTabs search URL with dedicated band and song parameters
        StringBuilder urlBuilder = new StringBuilder("https://www.guitartabs.cc/search.php?tabtype=any");
        if (!parsed.song.isEmpty()) {
            urlBuilder.append("&song=").append(URLEncoder.encode(parsed.song, "UTF-8"));
        }
        if (!parsed.artist.isEmpty()) {
            urlBuilder.append("&band=").append(URLEncoder.encode(parsed.artist, "UTF-8"));
        }
        return urlBuilder.toString();
    }

    protected List<SongMatch> parseSearchResults(Document doc) {
        List<SongMatch> songMatches = new ArrayList<>();

        // Parse rows from the search results table
        Elements rows = doc.select("table.tabslist tr, tr");
        Log.d(TAG, "Found " + rows.size() + " total rows.");

        for (Element row : rows) {
            if (row.selectFirst("th") != null) continue; // Skip header

            Elements cols = row.select("td");
            if (cols.size() < 3) continue;

            String artist = "";
            String title = "";
            String href = "";
            float rating = 0f;

            // Layout A: Full search results table (5+ columns)
            if (cols.size() >= 5) {
                Element bandLink = cols.get(2).selectFirst("a");
                if (bandLink != null) artist = bandLink.text().trim();

                Element songLink = cols.get(3).selectFirst("a");
                if (songLink != null) {
                    title = songLink.text().trim();
                    href = songLink.attr("href");
                }

                Element ratingDiv = cols.get(4).selectFirst("div.nrating");
                if (ratingDiv != null) {
                    rating = parseRatingFromStyle(ratingDiv.attr("style"));
                }
            }
            // Layout B: Compact/Artist-page table (3 columns, artist in URL)
            else if (cols.size() >= 3) {
                Element songLink = cols.get(1).selectFirst("a");
                if (songLink != null) {
                    title = songLink.text().trim();
                    href = songLink.attr("href");

                    // Extract artist slug from URL like "/tabs/b/bon_jovi/always_crd_ver_5.html"
                    artist = extractArtistFromUrl(href);
                }

                Element ratingDiv = cols.get(2).selectFirst("div.nrating");
                if (ratingDiv != null) {
                    rating = parseRatingFromStyle(ratingDiv.attr("style"));
                }
            }

            if (title.isEmpty() || href.isEmpty()) continue;

            String songUrl = href.startsWith("http") ? href : BASE_URL + (href.startsWith("/") ? "" : "/") + href;
            SongMatch songMatch = new SongMatch(title,artist,songUrl,getName());
            songMatch.setRating(rating);
            songMatches.add(songMatch);
        }

        return songMatches;
    }

    private String extractArtistFromUrl(String href) {
        try {
            // Example: /tabs/b/bon_jovi/always_crd_ver_5.html -> segments: ["", "tabs", "b", "bon_jovi", "always..."]
            String[] segments = href.split("/");
            if (segments.length >= 4) {
                String slug = segments[3]; // e.g., "bon_jovi"
                String[] words = slug.replace("_", " ").split(" ");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        sb.append(Character.toUpperCase(word.charAt(0)))
                                .append(word.substring(1))
                                .append(" ");
                    }
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to extract artist from URL: " + href, e);
        }
        return "";
    }

    private float parseRatingFromStyle(String style) {
        try {
            if (style != null && style.contains("background-position:")) {
                String posPart = style.split("background-position:")[1].split(";")[0].trim();
                String xValStr = posPart.split(" ")[0].replace("px", "").trim();
                float xPos = Math.abs(Float.parseFloat(xValStr));

                // Each star step is roughly 15px. 0px = 5 stars, -60px = 1 star, etc.
                // Formula maps 0px -> 5.0, max offset -> lower stars
                float maxOffset = 60.0f;
                float normalized = Math.max(0f, 1f - (xPos / maxOffset));
                return Math.round((normalized * 5.0f) * 10.0f) / 10.0f; // Rounded to 1 decimal
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse rating: " + style, e);
        }
        return 0f;
    }

    /**
     * Helper class to hold separated artist and song components.
     */
    private static class ParsedQuery {
        String song;
        String artist;

        ParsedQuery(String song, String artist) {
            this.song = song;
            this.artist = artist;
        }
    }

    /**
     * Extracts text inside parentheses for artist and treats the rest as the song title.
     * Handles both "Song (Artist)" and "(Artist) Song".
     */
    private ParsedQuery parseQuery(String rawQuery) {
        if (rawQuery == null) return new ParsedQuery("", "");

        String query = rawQuery.trim();
        Matcher matcher = PARENTHESIS_PATTERN.matcher(query);

        if (matcher.find()) {
            try {
                String artist = Objects.requireNonNull(matcher.group(1)).trim();
                // Remove the matched parenthesis group from the query to isolate the song title
                String song = query.replace(Objects.requireNonNull(matcher.group(0)), "").trim();
                return new ParsedQuery(song, artist);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If no parentheses are provided, treat the whole thing as the song query
        return new ParsedQuery(query, "");
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new GuitarTabs(context); // Passes the context right in!
    }
}