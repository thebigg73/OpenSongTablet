package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BoiteachansonsProvider extends BaseSongProvider {

    public BoiteachansonsProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "La Boîte à chansons";
    }

    @Override
    protected String getSearchUrl(String query) throws Exception {
        return "https://www.boiteachansons.net/recherche?q=" + URLEncoder.encode(query, "UTF-8");
    }

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> matches = new ArrayList<>();
        for (Element item : doc.select(".search-results-item, .song-row, ul.search-list li")) {
            Element link = item.selectFirst("a[href]");
            if (link != null) {
                Element artistEl = item.selectFirst(".artist-name, .author");
                String artist = artistEl != null ? artistEl.text() : "Unknown";
                matches.add(new SongMatch(link.text(), artist, link.absUrl("href"), getName()));
            }
        }
        return matches;
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new Boiteachansons();
    }
}