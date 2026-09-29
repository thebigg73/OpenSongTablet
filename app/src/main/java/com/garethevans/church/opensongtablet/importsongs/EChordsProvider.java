package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EChordsProvider extends BaseSongProvider {

    public EChordsProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "eChords";
    }

    @Override
    protected String getSearchUrl(String query) throws Exception {
        return "https://www.e-chords.com/search?q=" + URLEncoder.encode(query, "UTF-8");
    }

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> matches = new ArrayList<>();

        // Select search result rows/items from e-chords pages
        for (Element item : doc.select(".search-result, .song_row, tr.row")) {
            Element link = item.selectFirst("a[href]");
            if (link != null) {
                String title = link.text();
                String url = link.absUrl("href");
                Element artistEl = item.selectFirst(".artist, .band_name");
                String artist = artistEl != null ? artistEl.text() : "Unknown";

                if (!title.isEmpty() && !url.isEmpty()) {
                    matches.add(new SongMatch(title, artist, url, getName()));
                }
            }
        }
        return matches;
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new EChords();
    }
}