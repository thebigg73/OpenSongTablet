package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

public class GuitarTabs implements SongParserInterface {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final MainActivityInterface mainActivityInterface;

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "GuitarTabs";

    // Song is effectively written in <pre> formatting with chords above lyrics.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is htmlentitied - i.e. " is shown as &quot;, ' is shown as &#039;

    public GuitarTabs(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    @Override
    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String htmlText) {
        if (htmlText == null || htmlText.isEmpty()) return newSong;
        try {

            Log.d(TAG,"htmlText:"+htmlText);
            // 1. Parse the document ONCE
            Document doc = Jsoup.parse(htmlText);
            newSong.setLyrics(getLyrics(doc));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return newSong;
    }

    private String getLyrics(Document doc) {
        // Target the <pre> tag inside the line-height font container
        Element preElem = doc.selectFirst("font[style*=\"line-height\"] pre");

        if (preElem != null) {
            Log.d(TAG, "preElem found: " + preElem.html());
        } else {
            // Fallback if the style attribute varies slightly
            preElem = doc.selectFirst("font > pre, pre");
            if (preElem!=null) {
                Log.d(TAG, "preElem fallback:" + preElem.html());
            } else {
                Log.d(TAG,"preElem still null");
            }
        }

        String openSongContent = "";

        if (preElem != null) {
            openSongContent = convertPreToOpenSong(preElem);
        }

        return openSongContent;
    }

    private String convertPreToOpenSong(Element preElement) {
        StringBuilder result = new StringBuilder();

        // Flatten the HTML structure inside <pre> into lines
        // GuitarTabs uses text nodes with newlines and <a class='ch'> tags for chords.
        List<String> rawLines = extractLinesFromNode(preElement);

        for (String rawLineHtml : rawLines) {
            // Process each line to separate chords and lyrics into OpenSong format
            String processedLine = formatLineToOpenSong(rawLineHtml);
            result.append(processedLine).append("\n");
        }

        return result.toString().trim();
    }

    private List<String> extractLinesFromNode(Node node) {
        // Reconstruct the HTML text and split by newlines while keeping tag structure intact,
        // or recursively build line strings. A clean way is converting HTML children to a line-based list.
        StringBuilder currentLine = new StringBuilder();
        List<String> lines = new ArrayList<>();

        for (Node child : node.childNodes()) {
            if (child instanceof TextNode) {
                String text = ((TextNode) child).getWholeText();
                String[] parts = text.split("\r?\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    currentLine.append(parts[i]);
                    if (i < parts.length - 1) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                }
            } else if (child instanceof Element) {
                Element elem = (Element) child;
                if (elem.tagName().equals("br")) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                } else {
                    // For <a> tags (chords) or other inline elements, keep their outer/inner representation
                    currentLine.append(elem.outerHtml());
                }
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }

    private String formatLineToOpenSong(String lineHtml) {
        // If the line contains no chord anchors, return it as a normal lyric or section header line
        if (!lineHtml.contains("class='ch'") && !lineHtml.contains("class=\"ch\"")) {
            // Strip any leftover HTML tags just in case
            return org.jsoup.Jsoup.parse(lineHtml).text();
        }

        // We parse the line fragment using Jsoup to easily iterate text and chord nodes
        Document lineDoc = org.jsoup.Jsoup.parseBodyFragment(lineHtml);
        StringBuilder chordLine = new StringBuilder(".");
        StringBuilder lyricLine = new StringBuilder();

        for (Node node : lineDoc.body().childNodes()) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).getWholeText();
                chordLine.append(text);
                lyricLine.append(text);
            } else if (node instanceof Element) {
                Element elem = (Element) node;
                if (elem.hasClass("ch") || elem.tagName().equals("a")) {
                    String chord = elem.text().trim();
                    chordLine.append(chord);

                    // Pad the lyric line with spaces to match the start of the chord
                    while (lyricLine.length() < chordLine.length() - 1) {
                        lyricLine.append(" ");
                    }
                } else {
                    String text = elem.text();
                    chordLine.append(text);
                    lyricLine.append(text);
                }
            }
        }

        // If chords and lyrics were mixed on the same line, OpenSong expects the chord line first (starting with '.')
        // followed by the lyric line.
        String chords = chordLine.toString().replaceAll("\\s+$", ""); // Trim trailing spaces
        String lyrics = lyricLine.toString();

        if (chords.length() > 1) {
            return chords + "\n" + lyrics;
        } else {
            return lyrics;
        }
    }

}
