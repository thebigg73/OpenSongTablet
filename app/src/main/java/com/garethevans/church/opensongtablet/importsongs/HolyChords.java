package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HolyChords implements SongParserInterface {

    // This extracts the song from the HolyChords website

    private final String[] bitsToClear = new String[] {"<span class=\"chopds\">","<a class=\"tooltip\">",
            "<span class=\"tooltip-content\">","<span class=\"c\">","<span class=\"text\">\"","</a>",
            "</span>","-->","<!--"};

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "HolyChords";

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

        // Get the lyrics
        newSong.setLyrics(getLyrics(doc));
        Log.d(TAG,"lyrics:"+newSong.getLyrics());

        return newSong;
    }

    private String getTitle(Document doc) {
        Element titleElement = doc.selectFirst("h2.t-worship-leader__marquee__headline");
        String title = "";
        if (titleElement != null) {
            // Clone or isolate text nodes before any child elements (like spans) to drop the anti-copy warning text
            Element clonedTitle = titleElement.clone();
            clonedTitle.select("span").remove();
            title = clonedTitle.text().trim();
        }
        return title;
    }
    private String getAuthor(Document doc) {
        Element artistElement = doc.selectFirst("h5.t-worship-leader__marquee__headline a");
        String artist = "";
        if (artistElement != null) {
            artist = artistElement.text().trim();
        }
        return artist;
    }

    private String getLyrics(Document doc) {
        //<pre id="music_text"...>....</pre>

        StringBuilder openSongOutput = new StringBuilder();

        // Select each block container
        Elements blocks = doc.select("pre#music_text div.blocks");

        for (Element block : blocks) {
            // 1. Extract Section Header
            Element headerEl = block.selectFirst("b.videlit_line");
            if (headerEl != null) {
                String headerText = headerEl.text().replace(":", "").trim();
                openSongOutput.append("\n").append(formatSectionHeader(headerText)).append("\n");
            }

            // 2. Iterate through paired chord and lyric lines within the block
            Elements chopdsLines = block.select("span.chopds");
            for (Element chopds : chopdsLines) {
                // Find the immediately following sibling text span if it exists
                Element textSpan = chopds.nextElementSibling();
                if (textSpan == null || !textSpan.hasClass("text")) {
                    continue; // Skip if no matching lyric text line
                }

                // Extract chords with spacing layout
                String chordLine = buildChordLine(chopds, textSpan.text());
                String lyricLine = " " + textSpan.text();

                if (!chordLine.equals(".")) {
                    openSongOutput.append(chordLine).append("\n");
                }
                openSongOutput.append(lyricLine.replaceAll("[ \\t]+$", "")).append("\n");
            }
        }

        return openSongOutput.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static String buildChordLine(Element chopds, String lyricText) {
        StringBuilder chordLine = new StringBuilder(".");
        StringBuilder lineTracker = new StringBuilder();

        for (org.jsoup.nodes.Node node : chopds.childNodes()) {
            if (node instanceof org.jsoup.nodes.TextNode) {
                String textContent = ((org.jsoup.nodes.TextNode) node).getWholeText();

                // COMPRESSION FIX: Scale down large proportional spaces for monospaced rendering
                // We replace runs of 3 or more spaces with a compressed version (roughly halved)
                textContent = compressProportionalSpaces(textContent);

                lineTracker.append(textContent);
            } else if (node instanceof Element) {
                Element el = (Element) node;
                Element chordSpan = el.selectFirst("span.c");
                String chord = "";
                if (chordSpan != null) {
                    chord = chordSpan.ownText().trim();
                    if (chord.isEmpty()) {
                        chord = chordSpan.text().trim();
                    }
                }

                if (!chord.isEmpty()) {
                    boolean hasLeadingParen = lineTracker.toString().endsWith("(");
                    int targetIndex = lineTracker.length() + 1;
                    if (hasLeadingParen) {
                        targetIndex -= 1;
                    }

                    while (chordLine.length() < targetIndex) {
                        chordLine.append(" ");
                    }

                    if (hasLeadingParen) {
                        chordLine.append("(").append(chord).append(")");
                    } else {
                        chordLine.append(chord);
                    }

                    lineTracker.append(chord);
                } else {
                    lineTracker.append(el.text());
                }
            }
        }

        return chordLine.toString().replaceAll("[ \\t]+$", "");
    }

    // Helper to scale down web proportional spacing for monospaced fonts
    private static String compressProportionalSpaces(String rawText) {
        // If the text node is just spaces, compress it so it doesn't blow out in monospaced text
        if (rawText.matches("^\\s+$")) {
            int spaceCount = rawText.length();
            if (spaceCount > 4) {
                // Compress proportional spacing down by roughly 50-60% for monospaced view
                int compressedCount = Math.max(2, spaceCount / 2);
                return " ".repeat(compressedCount);
            }
        }
        return rawText;
    }

    private static String formatSectionHeader(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("приспів") || lower.contains("chorus")) {
            return "[C]";
        } else if (lower.contains("куплет") || lower.contains("verse")) {
            String num = text.replaceAll("\\D+", "");
            return "[V" + (num.isEmpty() ? "1" : num) + "]";
        }
        return "[" + text + "]";
    }


    private String getSubstring(String s, String startText, String endText) {
        int pos1=0, pos2=0;
        if (startText!=null) {
            pos1 = s.indexOf(startText) + startText.length();
        }
        if (endText!=null) {
            pos2 = s.indexOf(endText,pos1);
        }

        if (startText!=null && endText!=null && pos1>0 && pos2>pos1) {
            return s.substring(pos1,pos2);
        } else if (startText==null & endText!=null && pos2>0) {
            return s.substring(0,pos2);
        } else if (startText!=null && endText==null & pos1>0) {
            return s.substring(pos1);
        } else {
            return "";
        }
    }

    private String stripOutTags(String s) {
        for (String bit:bitsToClear) {
            s = s.replace(bit,"");
        }
        s = s.replaceAll("<(.*?)>", "");
        return s;
    }

    private String removeImageTags(String s) {
        boolean keepGoing = true;
        while (keepGoing) {
            int start = s.indexOf("<img src=\"");
            int end = s.indexOf(">",start);
            if (start>-1 && end>start) {
                String bitToRemove = s.substring(start,end+1);
                s = s.replace(bitToRemove,"");
            } else {
                keepGoing = false;
            }
        }
        return s;
    }
}
