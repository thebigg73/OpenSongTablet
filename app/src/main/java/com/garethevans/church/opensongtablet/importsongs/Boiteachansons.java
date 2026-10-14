package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class Boiteachansons implements SongParserInterface {

    // Extracts from La boite a chansons

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Boitechansons";

    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String webString) {

        // 1. Parse the document ONCE

        Document doc = Jsoup.parse(webString);

        // Get the title
        String title = getTitle(doc);
        newSong.setTitle(title);
        newSong.setFilename(title);
        newSong.setFiletype("XML");
        Log.d(TAG,"title:"+title);

        // Get the author
        newSong.setAuthor(getAuthor(doc));
        Log.d(TAG,"author:"+newSong.getAuthor());

        // Get the key
        String key=getKey(doc);
        newSong.setKey(key);
        newSong.setKeyOriginal(key);
        Log.d(TAG,"key:"+key);

        // Get the lyrics
        newSong.setLyrics(getLyrics(doc));
        Log.d(TAG,"lyrics:"+newSong.getLyrics());

        return newSong;
    }

    private String getTitle(Document doc) {
        Element titleElement = doc.selectFirst("#dTitreChanson");
        String title = "";
        if (titleElement != null) {
            title = titleElement.text().trim();
        }
        return title;
    }
    private String getAuthor(Document doc) {
        Element artistElement = doc.selectFirst("div.dTitreNomArtiste a");
        String artist = "";
        if (artistElement != null) {
            artist = artistElement.text().trim();
        }
        return artist;
    }
    private String getKey(Document doc) {
        // Grab the data-tonique attribute from the first available tonality list item
        Element keyElement = doc.selectFirst("ul.ulTonalListe li.liTonalElmt");
        String key = "";

        if (keyElement != null) {
            key = keyElement.attr("data-tonique").trim();
        }
        return key;
    }
    private String getLyrics(Document doc) {
        StringBuilder openSongOutput = new StringBuilder();

        // Target the container holding the parsed rows/lines
        Element partitionDiv = doc.selectFirst("#divPartition");
        if (partitionDiv == null) return "";

        // Each line of lyrics/chords is wrapped in elements like div.pL or block nodes
        Elements lineElements = partitionDiv.select("div.pL, div.pLS");

        for (Element lineEl : lineElements) {
            if (lineEl.hasClass("pLS")) {
                // Pure lyric line with no chords
                openSongOutput.append(" ").append(lineEl.text()).append("\n");
                continue;
            }

            StringBuilder chordLine = new StringBuilder(".");
            StringBuilder lyricLine = new StringBuilder(" ");
            StringBuilder textTracker = new StringBuilder();
            boolean hasChords = false;

            for (Node node : lineEl.childNodes()) {
                if (node instanceof TextNode) {
                    String text = ((TextNode) node).getWholeText();
                    textTracker.append(text);
                    lyricLine.append(text);
                } else if (node instanceof Element) {
                    Element el = (Element) node;

                    // Check if this element contains a chord span
                    Element chordChild = el.selectFirst("span.a");
                    if (chordChild != null && chordChild.hasAttr("data-a")) {
                        hasChords = true;
                        // Use data-a attribute (e.g., "C#m", "B", "A") for the clean chord name
                        String chord = chordChild.attr("data-a").trim();

                        int targetIndex = textTracker.length() + 1; // +1 for '.' prefix offset
                        while (chordLine.length() < targetIndex) {
                            chordLine.append(" ");
                        }
                        chordLine.append(chord);
                    } else {
                        // Regular inline formatting element
                        String elText = el.text();
                        textTracker.append(elText);
                        lyricLine.append(elText);
                    }
                }
            }

            if (hasChords && chordLine.length() > 1) {
                openSongOutput.append(chordLine.toString().replaceAll("[ \\t]+$", "")).append("\n");
            }
            openSongOutput.append(lyricLine.toString().replaceAll("[ \\t]+$", "")).append("\n");
        }

        return openSongOutput.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }
}
