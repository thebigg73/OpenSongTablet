package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UkuTabsProvider extends BaseSongProvider {

    public UkuTabsProvider(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "UkuTabs";
    }

    @Override
    protected String getSearchUrl(String query) throws Exception {
        return "https://ukutabs.com/?s=" + URLEncoder.encode(query, "UTF-8");
    }

    @Override
    protected List<SongMatch> parseSearchResults(Document doc) throws Exception {
        List<SongMatch> matches = new ArrayList<>();
        // Select each song row element
        Elements rows = doc.select("li.uku-songrow");

        for (Element row : rows) {
            // Extract Artist
            Element artistElem = row.selectFirst("a.uku-songrow__artist");
            String artist = artistElem != null ? artistElem.text().trim() : "";

            // Extract Title and URL from the title anchor
            Element titleElem = row.selectFirst("a.uku-songrow__title");
            String title = "";
            String songUrl = "";

            if (titleElem != null) {
                // Use .text() to strip out HTML tags like span highlight spans cleanly
                title = titleElem.text().trim();
                songUrl = titleElem.attr("abs:href");
                if (songUrl.isEmpty()) {
                    songUrl = titleElem.attr("href"); // Fallback if absolute wasn't auto-resolved
                }
            }

            // Extract Difficulty (from title attribute of the diff icon/link)
            Element diffElem = row.selectFirst("a.uku-songrow__diff");
            String difficulty = diffElem != null ? diffElem.attr("title") : ""; // e.g., "Difficulty: Novice"

            // Extract Rating
            Element ratingNumElem = row.selectFirst("span.uku-songrow__rating-num");
            String ratingStr = ratingNumElem != null ? ratingNumElem.text().trim() : "–";
            float rating = 0f;
            try {
                if (!ratingStr.equals("–")) {
                    rating = Float.parseFloat(ratingStr);
                }
            } catch (NumberFormatException e) {
                rating = 0f;
            }

            SongMatch match = new SongMatch(title, artist, songUrl, getName());
            match.setRating(rating);

            matches.add(match);
        }

        return matches;
    }

    @Override
    protected SongParserInterface getParser(Context context) {
        return new UkuTabs();
    }
}