package com.garethevans.church.opensongtablet.importsongs;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EChords implements SongParserInterface {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "EChords";

    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String webString) {
        // 1. Parse the document ONCE
        Document doc = Jsoup.parse(webString);

        // Get song metadata (which has almost everything in it)
        String metaData = getSongMetadata(doc);
        if (metaData!=null) {
            parseSongMetadata(mainActivityInterface, newSong, metaData);
        }

        return newSong;
    }

    private String getSongMetadata(Document doc) {
        // Select all script tags in the HTML document
        Elements scripts = doc.select("script");

        for (Element script : scripts) {
            String scriptText = script.html();
            if (scriptText.contains("SONG_METADATA")) {
                // Relaxed pattern: allows optional whitespace and doesn't require a trailing semicolon
                Pattern pattern = Pattern.compile("window\\.SONG_METADATA\\s*=\\s*(\\{[\\s\\S]*?\\})(?:\\s*;|$)", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(scriptText);

                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    public void parseSongMetadata(MainActivityInterface mainActivityInterface, Song newSong, String scriptContent) {
        // Example fields you asked about: TITLE, ARTIST_NAME, CHORDS_KEY, etc.
        // Replace ARTIST_NAME with ARTIST;
        scriptContent = scriptContent.replace("ARTIST_NAME","ARTIST");

        String title = extractJsonField(scriptContent, "NAME");         // e.g., "Love Me Do"
        newSong.setTitle(title);
        newSong.setFilename(title);
        newSong.setFiletype("XML");

        String artist = extractJsonField(scriptContent, "ARTIST"); // e.g., "The Beatles"
        newSong.setAuthor(artist);

        String key = extractJsonField(scriptContent, "CHORDS_KEY");     // e.g., "G"
        newSong.setKey(key);
        newSong.setKeyOriginal(key);

        String capo = extractCapoString(scriptContent);
        newSong.setCapo(capo);

        String rawJsonLyrics = extractChordsContentField(scriptContent); // raw HTML string
        String chordsContent = mainActivityInterface.getConvertTextSong().convertText(parseChordsContentJson(rawJsonLyrics));

        newSong.setLyrics(chordsContent);
    }

    private String extractJsonField(String jsonText, String fieldName) {
        // Matches patterns like: NAME: 'Love Me Do', or TITLE: 'love-me-do'
        Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*'([^']*)'");
        Matcher matcher = pattern.matcher(jsonText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    public String extractCapoString(String jsonText) {
        // Matches patterns like: CAPO: 0, or CAPO: 3
        Pattern pattern = Pattern.compile("CAPO\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(jsonText);

        if (matcher.find()) {
            try {
                String val = matcher.group(1);
                if (val!=null) {
                    int capoValue = Integer.parseInt(val);
                    // Return empty string if 0, otherwise return the integer as a string
                    return capoValue == 0 ? "" : String.valueOf(capoValue);
                }
            } catch (NumberFormatException e) {
                return "";
            }
        }
        return "";
    }

    public static String extractChordsContentField(String jsonBlock) {
        if (jsonBlock == null) return "";

        String keyMarker = "CHORDS_CONTENT:";
        int startIndex = jsonBlock.indexOf(keyMarker);
        if (startIndex == -1) return "";

        startIndex += keyMarker.length();

        // Skip whitespace to find the opening quote
        while (startIndex < jsonBlock.length() &&
                (jsonBlock.charAt(startIndex) == ' ' || jsonBlock.charAt(startIndex) == '\t' ||
                        jsonBlock.charAt(startIndex) == '\r' || jsonBlock.charAt(startIndex) == '\n')) {
            startIndex++;
        }

        if (startIndex >= jsonBlock.length() || jsonBlock.charAt(startIndex) != '"') {
            return "";
        }

        // Scan forward, counting backslashes to ensure we skip escaped quotes like \" or \\
        int endIndex = startIndex + 1;
        while (endIndex < jsonBlock.length()) {
            char c = jsonBlock.charAt(endIndex);
            if (c == '"') {
                // Count consecutive backslashes preceding this quote
                int backslashCount = 0;
                int checkIdx = endIndex - 1;
                while (checkIdx >= startIndex && jsonBlock.charAt(checkIdx) == '\\') {
                    backslashCount++;
                    checkIdx--;
                }
                // If the number of backslashes is even (or 0), this quote is unescaped (the true end)
                if (backslashCount % 2 == 0) {
                    break;
                }
            }
            endIndex++;
        }

        if (endIndex >= jsonBlock.length()) return "";

        return jsonBlock.substring(startIndex + 1, endIndex);
    }

    public String parseChordsContentJson(String rawJsonField) {
        if (rawJsonField == null) return "";

        // 1. Unescape the JSON string payload
        // 1. Unescape all JSON unicode, slashes, and quotes first
        String[] lines = getUnescapedStrings(rawJsonField);
        StringBuilder openSongOutput = new StringBuilder();

        for (String lineHtml : lines) {
            // Replace non-breaking space entities before parsing so spaces aren't dropped
            String processedHtml = lineHtml.replace("&nbsp;", " ").replace("\u00A0", " ");
            Document lineDoc = Jsoup.parseBodyFragment(processedHtml);

            Elements chordSpans = lineDoc.select("span[data-chord]");

            if (!chordSpans.isEmpty()) {
                // Reconstruct the chord/lyric line maintaining order
                StringBuilder lineBuilder = new StringBuilder(".");
                for (Node node : lineDoc.body().childNodes()) {
                    appendNodeContent(node, lineBuilder);
                }
                openSongOutput.append(lineBuilder.toString().replaceAll("[ \\t]+$", "")).append("\n");
            } else {
                // Standard line (lyrics, tabs, or headers like [Intro])
                String text = lineDoc.text().replace("\u00A0", " ");
                if (!text.trim().isEmpty()) {
                    if (text.contains("|") || text.trim().matches("^[EBGDAe].*")) {
                        openSongOutput.append(text).append("\n"); // Tab line
                    } else if (text.trim().startsWith("[")) {
                        openSongOutput.append(text).append("\n"); // Section header
                    } else {
                        openSongOutput.append(" ").append(text).append("\n"); // Lyric line
                    }
                }
            }
        }

        return openSongOutput.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }

    private String[] getUnescapedStrings(String rawJsonField) {
        String unescapedHtml = rawJsonField
                .replace("\\u0022", "\"")
                .replace("\\u003C", "<")
                .replace("\\u003E", ">")
                .replace("\\u0027","'")
                .replace("\\/", "/")      // <-- Fixes \/ so closing tags like <\/pre> become </pre>
                .replace("\\\"", "\"")
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\\\\", "\\")
                .replace("\\'", "'")
                .replace("\\r","\n");

        // 2. Split into individual lines from the unescaped text
        return unescapedHtml.split("\\r?\\n");
    }

    // Helper to recursively pull text or chord names in sequence
    private static void appendNodeContent(Node node, StringBuilder sb) {
        if (node instanceof TextNode) {
            sb.append(((TextNode) node).getWholeText());
        } else if (node instanceof Element) {
            Element el = (Element) node;
            if (el.hasAttr("data-chord")) {
                sb.append(el.attr("data-chord"));
            } else {
                for (Node child : el.childNodes()) {
                    appendNodeContent(child, sb);
                }
            }
        }
    }
}
