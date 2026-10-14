package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class UkuTabs implements SongParserInterface {

    // This is triggered from the import online option from UkuTabs

    private final String TAG = "UkuTabs";

    private final String[] bitsToClear = new String[] {"</a>","<strong>","</strong>",
    "<a target=\"_blank\" class=\"ukutabschord\" href=\"https://ukuchords.com/\">",};

    /*public Song processContentWebView(MainActivityInterface mainActivityInterface, Song newSong, String htmlText) {
        if (htmlText == null || htmlText.isEmpty()) return newSong;
        try {

            // 1. Parse the document ONCE
            Document doc = Jsoup.parse(htmlText);

            // 2. Pass the Document to your modular getter methods
            newSong.setKey(getKey(doc));
            newSong.setCapo(getCapo(doc));
            newSong.setLyrics(getLyrics(doc));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return newSong;
    }*/

    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String webString) {
        if (webString == null || webString.isEmpty()) return newSong;
        try {

            // 1. Parse the document ONCE
            Document doc = Jsoup.parse(webString);

            // Get the metadata (Title, Author)
            getMetaData(mainActivityInterface, newSong, doc);

            // Get the lyrics
            newSong.setLyrics(getLyrics(doc));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newSong;
    }

    private void getMetaData(MainActivityInterface mainActivityInterface, Song newSong, Document doc) {
        // Select all JSON-LD script tags on the page
        Elements scriptTags = doc.select("script[type=application/ld+json]");

        for (Element script : scriptTags) {
            try {
                String jsonData = script.html();
                JSONObject json = new JSONObject(jsonData);

                // Check if this block contains music/composition data or the "byArtist" key
                if (isMusicSchema(json)) {
                    String title = json.optString("name", "Unknown");

                    newSong.setTitle(title);
                    newSong.setFilename(title);
                    newSong.setFiletype("XML");

                    String artist = "";
                    if (json.has("byArtist")) {
                        JSONObject artistObj = json.getJSONObject("byArtist");
                        artist = artistObj.optString("name", "");
                    }
                    newSong.setAuthor(artist);

                    break; // Found the target block, exit loop
                }
            } catch (Exception e) {
                // Skip blocks that don't match or fail JSON parsing (e.g., Breadcrumbs)
            }
        }
    }


    private boolean isMusicSchema(JSONObject json) {
        if (json.has("byArtist")) {
            return true;
        }
        if (json.has("@type")) {
            try {
                Object type = json.get("@type");
                if (type instanceof JSONArray) {
                    JSONArray arr = (JSONArray) type;
                    for (int i = 0; i < arr.length(); i++) {
                        String t = arr.getString(i);
                        if (t.equals("MusicRecording") || t.equals("MusicComposition")) {
                            return true;
                        }
                    }
                } else if (type instanceof String) {
                    String t = (String) type;
                    return t.equals("MusicRecording") || t.equals("MusicComposition");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String getLyrics(Document mainDoc) {
        // Target the standard pre block (avoiding uku-chunked)
        Element preTag = mainDoc.selectFirst("pre.qoate-code:not(#uku-chunked)");
        if (preTag == null) {
            preTag = mainDoc.selectFirst("div.post-content pre"); // Fallback
        }
        if (preTag == null) return "";

        // Convert HTML line breaks to real newlines while keeping child nodes intact
        String html = preTag.html().replaceAll("(?i)<br\\s*/?>", "\n");
        Document doc = Jsoup.parse("<body>" + html + "</body>");
        Element body = doc.body();

        StringBuilder openSongOutput = new StringBuilder();

        StringBuilder lineChord = new StringBuilder(".");
        StringBuilder lineLyric = new StringBuilder(" ");
        StringBuilder textTracker = new StringBuilder();
        boolean hasChordsOnLine = false;

        for (Node node : body.childNodes()) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).getWholeText();
                String[] parts = text.split("\r?\n", -1);

                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        // Flush the completed line when we hit a newline
                        flushLine(openSongOutput, lineChord, lineLyric, hasChordsOnLine);
                        lineChord = new StringBuilder(".");
                        lineLyric = new StringBuilder(" ");
                        textTracker.setLength(0);
                        hasChordsOnLine = false;
                    }
                    textTracker.append(parts[i]);
                    lineLyric.append(parts[i]);
                }
            } else if (node instanceof Element) {
                Element el = (Element) node;
                if (el.tagName().equalsIgnoreCase("a") && el.hasClass("ukutabschord")) {
                    hasChordsOnLine = true;
                    String chord = el.text().trim();
                    // +1 accounts for the '.' prefix offset on the chord line
                    int targetIndex = textTracker.length() + 1;

                    while (lineChord.length() < targetIndex) {
                        lineChord.append(" ");
                    }
                    lineChord.append(chord);
                } else {
                    // Handles elements like <strong> for section headers (e.g., Chorus:)
                    String elText = el.text();
                    textTracker.append(elText);
                    lineLyric.append(elText);
                }
            }
        }
        // Flush the final line
        flushLine(openSongOutput, lineChord, lineLyric, hasChordsOnLine);

        return openSongOutput.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static void flushLine(StringBuilder output, StringBuilder chordLine, StringBuilder lyricLine, boolean hasChords) {
        String lyricStr = lyricLine.toString().replaceAll("[ \\t]+$", "");
        String chordStr = chordLine.toString().replaceAll("[ \\t]+$", "");

        String trimmedText = lyricStr.trim();

        // Check if the line is a section header (e.g., Chorus:, Verse 1:)
        if (trimmedText.matches("(?i)^(Chorus|Verse\\s*\\d*|Intro|Outro|Bridge|Tag).*")) {
            output.append("\n").append(formatSectionHeader(trimmedText)).append("\n");
            return;
        }

        if (!trimmedText.isEmpty() || hasChords) {
            // Only output the chord line if it actually contains chords (length > 1 because of '.')
            if (hasChords && chordStr.length() > 1) {
                output.append(chordStr).append("\n");
            }
            if (!trimmedText.isEmpty()) {
                output.append(lyricStr).append("\n");
            }
        }
    }

    private static String formatSectionHeader(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("verse")) {
            String num = text.replaceAll("\\D+", "");
            return "[V" + (num.isEmpty() ? "1" : num) + "]";
        } else if (lower.contains("chorus")) {
            return "[C]";
        } else if (lower.contains("intro")) {
            return "[Intro]";
        } else if (lower.contains("outro")) {
            return "[Outro]";
        } else if (lower.contains("bridge")) {
            return "[B]";
        }
        return "[" + text.replace(":", "").trim() + "]";
    }
}
