package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HolyChordsProvider extends BaseSongProvider {

    private final String TAG = "HolyChordsProvider";

    public HolyChordsProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "HolyChords";
    }

    @Override
    protected String getSearchUrl(String query) throws Exception {
        //return "https://holychords.pro/search?name=" + URLEncoder.encode(query, "UTF-8");
        return "https://holychords.pro/search?name=" + URLEncoder.encode(query, "UTF-8") + "&is_page=1";
    }

    // Matches relative paths that are purely numeric song IDs (e.g., "/52538")
    private static final Pattern SONG_PATH_PATTERN = Pattern.compile("^/\\d+$");

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> songMatches = new ArrayList<>();

        String html = doc.html();
        int chunkSize = 3000;
        Log.d(TAG, "--- START HTML DUMP (Length: " + html.length() + ") ---");
        for (int i = 0; i < html.length(); i += chunkSize) {
            int end = Math.min(html.length(), i + chunkSize);
            Log.d(TAG, "Chunk " + (i / chunkSize) + ": " + html.substring(i, end));
        }
        Log.d(TAG, "--- END HTML DUMP ---");

        // Target each media-body container in the search results
        Elements mediaBodies = doc.select("div.media-body");

        for (Element mediaBody : mediaBodies) {
            // Find the song link inside the media-body
            Element link = mediaBody.selectFirst("a[href]");
            if (link == null) continue;

            String href = link.attr("href");

            // Verify the href points to a song ID path
            if (SONG_PATH_PATTERN.matcher(href).matches()) {
                String title = link.text().trim();
                if (title.isEmpty()) continue;

                // Extract artist from the text-muted sub-div
                String artist = "";
                Element artistElem = mediaBody.selectFirst("div.text-muted");
                if (artistElem != null) {
                    // .text() will safely strip out any nested inner spans (like the mob small span)
                    artist = artistElem.text().trim();
                }

                // Build absolute URL
                String songUrl = "https://holychords.pro" + href;

                // Create and add the match
                songMatches.add(new SongMatch(title, artist, songUrl, getName()));
            }
        }

        return songMatches;
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new HolyChords();
    }
}