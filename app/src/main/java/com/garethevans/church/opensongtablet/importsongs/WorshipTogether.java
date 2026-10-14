package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorshipTogether implements SongParserInterface {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "WorshipTogether";

    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String s) {
        // From Worship Together

        Document doc = Jsoup.parse(s);

        // Get the title/filename
        String title = getTitle(doc);
        newSong.setTitle(title);
        newSong.setFilename(title);
        newSong.setFiletype("XML");
        Log.d(TAG, "title:" + title);

        // Now the author
        String author = getAuthor(doc);
        newSong.setAuthor(author);
        newSong.setCopyright(author);
        Log.d(TAG, "author:" + author);

        // Get the key
        String key = getKey(doc);
        newSong.setKey(key);
        newSong.setKeyOriginal(key);
        Log.d(TAG, "key:" + newSong.getKey());

        // Get the themes/tags
        String themes = getThemes(doc);
        newSong.setTheme(themes);
        Log.d(TAG, "themes:" + themes);

        // Get the tempo and CCLI
        getTempoAndCCLI(doc, newSong);

        // Now get the lyrics
        newSong.setLyrics(getLyrics(doc));
        Log.d(TAG,"lyrics:"+newSong.getLyrics());

        return newSong;
    }

    private String getTitle(Document doc) {
        Element titleMeta = doc.selectFirst("meta[property=\"cludo:englishSongTitle\"]");
        return (titleMeta != null) ? titleMeta.attr("content") : "Unknown";
    }

    private String getAuthor(Document doc) {
        Elements writerElements = doc.select("meta[property=\"cludo:writer\"]");
        List<String> authors = new ArrayList<>();
        for (Element writer : writerElements) {
            authors.add(writer.attr("content"));
        }
        return String.join(", ", authors);
    }

    private String getKey(Document doc) {
        Element keyMeta = doc.selectFirst("meta[property=\"cludo:originalKey\"]");
        return (keyMeta != null) ? keyMeta.attr("content") : "";
    }

    private String getThemes(Document doc) {
        Elements themesElements = doc.select("meta[property=\"cludo:theme\"]");
        List<String> themes = new ArrayList<>();
        for (Element theme : themesElements) {
            themes.add(theme.attr("content"));
        }
        return String.join(";", themes);
    }

    private void getTempoAndCCLI(Document doc, Song newSong) {
        // Select all detail divs
        Elements details = doc.select("div.detail");

        boolean foundBPM = false;
        boolean foundCCLI = false;
        for (Element detail : details) {
            Element span = detail.selectFirst("span");
            // Check if this detail block is the BPM section
            if (span != null && span.text().trim().equalsIgnoreCase("BPM:")) {
                Element p = detail.selectFirst("p");
                if (p != null) {
                    // Make sure it only contains digits
                    String content = p.text().trim().replaceAll("\\D", "");
                    if (!content.isEmpty()) {
                        int bpm = Integer.parseInt(content);
                        if (bpm < 40) {
                            bpm = 40;
                        } else if (bpm > 300) {
                            bpm = 300;
                        }
                        newSong.setTempo(String.valueOf(bpm));
                        Log.d(TAG, "tempo:" + newSong.getTempo());
                        foundBPM = true;
                    }
                }
            } else if (span != null && span.text().trim().equalsIgnoreCase("CCLI #:")) {
                Element p = detail.selectFirst("p");
                if (p != null) {
                    // Make sure it only contains digits
                    newSong.setCcli(p.text().trim());
                    Log.d(TAG, "ccli:" + newSong.getCcli());
                    foundCCLI = true;
                }
            }
            if (foundBPM && foundCCLI) {
                // Quick exit
                break;
            }
        }
    }

    private String getLyrics(Document doc) {
        StringBuilder openSongOutput = new StringBuilder();

        Elements lines = doc.select(".chordProContainer .chord-pro-line");

        for (Element line : lines) {
            Elements segments = line.select(".chord-pro-segment");

            // 1. Check if this line is a section header
            if (segments.size() == 1) {
                Element chordNote = segments.first().selectFirst(".chord-pro-note");
                Element lyricDiv = segments.first().selectFirst(".chord-pro-lyric");
                String chordText = chordNote != null ? chordNote.text() : "";
                String lyricText = lyricDiv != null ? lyricDiv.text() : "";

                if ((chordText.isEmpty() || chordText.equals("???")) && isSectionHeader(lyricText)) {
                    openSongOutput.append("\n").append(formatSectionHeader(lyricText)).append("\n");
                    continue;
                }
            }

            // 2. Process standard lines
            StringBuilder chordLine = new StringBuilder(".");
            StringBuilder lyricLine = new StringBuilder(" ");
            boolean hasActualChords = false;

            for (Element segment : segments) {
                Element chordNote = segment.selectFirst(".chord-pro-note");
                Element lyricDiv = segment.selectFirst(".chord-pro-lyric");

                // Instead of lyricDiv.text(), iterate over child text nodes to preserve exact whitespace
                String lyric = "";
                if (lyricDiv != null) {
                    StringBuilder lyricBuilder = new StringBuilder();
                    for (org.jsoup.nodes.Node childNode : lyricDiv.childNodes()) {
                        if (childNode instanceof org.jsoup.nodes.TextNode) {
                            lyricBuilder.append(((org.jsoup.nodes.TextNode) childNode).getWholeText());
                        } else {
                            lyricBuilder.append(childNode.outerHtml());
                        }
                    }
                    lyric = lyricBuilder.toString().replace("\u00A0", " ");
                }
                String chord = "";
                if (chordNote != null) {
                    chord = chordNote.text().replace("\u00A0", " ");
                }

                // The current length of the lyric line before adding this segment's text
                int targetIndex = lyricLine.length();

                if (!chord.isEmpty() && !chord.equals("???")) {
                    hasActualChords = true;
                    // Pad the chord line with spaces until it reaches the exact index of the lyric
                    while (chordLine.length() < targetIndex) {
                        chordLine.append(" ");
                    }
                    chordLine.append(chord);
                }

                lyricLine.append(lyric);
            }

            // Format and append output lines
            String trimmedLyrics = lyricLine.toString();
            // String trimmedLyrics = lyricLine.toString().replaceAll("[ \\t]+$", " ");
            if (!trimmedLyrics.trim().isEmpty() || hasActualChords) {
                if (hasActualChords) {
                    openSongOutput.append(chordLine.toString().replaceAll("[ \\t]+$", " ")).append("\n");
                }
                openSongOutput.append(trimmedLyrics).append("\n");
            }
        }

        return openSongOutput.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static boolean isSectionHeader(String text) {
        String lower = text.toLowerCase();
        return lower.equals("intro") || lower.equals("chorus") ||
                lower.startsWith("verse") || lower.equals("bridge") ||
                lower.equals("tag") || lower.equals("outro");
    }

    private static String formatSectionHeader(String text) {
        // Standardizes to OpenSong block notation like [Verse 1], [Chorus], etc.
        String lower = text.toLowerCase();
        if (lower.startsWith("verse")) {
            String num = text.replaceAll("\\D+", "");
            return "[V" + (num.isEmpty() ? "" : num) + "]";
        } else if (lower.equals("chorus")) {
            return "[C]";
        } else if (lower.equals("intro")) {
            return "[Intro]";
        } else if (lower.equals("outro")) {
            return "[Outro]";
        } else if (lower.equals("bridge")) {
            return "[B]";
        }
        return "[" + text + "]";
    }
}
