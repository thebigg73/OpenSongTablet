package com.garethevans.church.opensongtablet.importsongs;

/*
This is used to process incoming content from the online web searches
Ultimate Guitar
Chordie
SongSelect
WorshipTogether
UkuTabs
HolyChords

It returns a song object
*/


import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class WebsiteProcessing {

    private String cutdownContent;

    // Deal with Ultimate Guitar
    private Song fixUGContent(MainActivityInterface mainActivityInterface, Song newSong, String content) {

        return newSong;
        /*// Get the title
        newSong.setTitle(getUGTitle(mainActivityInterface,content));
        // Get the author
        newSong.setAuthor(getAuthorUG(mainActivityInterface,content));

        String pageauthor = getPageAuthorUG(pagetitle);
        pagetitle = getShortenedPageTitleUG(pagetitle);

        String[] tl = resultposted.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String t:tl) {
            Log.d("FindNewSongs",t);
            sb.append(t).append("NEW_LINE_OS");
        }
        resultposted = sb.toString();

        // Shorten down what we need
        if (resultposted.contains("<div class=\"js-store\"")) {
            resultposted = resultposted.substring(resultposted.indexOf("<div class=\"js-store\""));
        }

        if (resultposted.contains("</div")) {
            resultposted = resultposted.substring(0,resultposted.indexOf("</div>"));
        }

        resultposted = resultposted.replace("\\r\\n","NEW_LINE_OS");

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        String title_resultposted;
        String filenametosave;

        // Get the content we need from the rest of the page
        resultposted = getContentUG(resultposted);

        // Get the title and filename
        title_resultposted = getTitleUG(resultposted,pagetitle);
        filenametosave = title_resultposted;

        // Get the author
        authorname = getAuthorUG(resultposted,pageauthor);

        // Get the lyrics and chords
        newtext = getLyricsUG(resultposted);

        // Get the key


        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }*/
    }
    /*private String getUGTitle(MainActivityInterface mainActivityInterface,String s) {
        // This is done in a few steps.
        // First get the title of the webpage
        int startpos = s.indexOf("<title");
        startpos = s.indexOf(">",startpos);
        int endpos = s.indexOf("</title",startpos);
        if (startpos>-1 && endpos>startpos) {
            s = s.substring(startpos+1,endpos);
            s = stripExtraUG(s);
            if (s.contains("By")) {
                s = s.substring(0,s.indexOf("By"));
                s = s.trim();
            }
            return mainActivityInterface.getProcessSong().parseToHTMLEntities(s);
        } else {
            return "";
        }
    }*/



/*
    private String getContentUG(String s) {
        int startpos = s.indexOf("&quot;content&quot;:");
        int endpos = s.indexOf("[/tab]&quot;");
        if (startpos>-1 && endpos>-1 && endpos>-startpos) {
            s = s.substring(startpos,endpos);
        }
        return s;
    }
*/



/*
    private String getTitleUG(String s,String pagetitle) {
        int startpos = s.indexOf("Song:");
        int endpos = s.indexOf("NEW_LINE_OS",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            s = s.substring(startpos + 5, endpos);
            s = stripExtraUG(s);
            return s;
        } else if (pagetitle==null || pagetitle.equals("")){
            try {
                return FullscreenActivity.phrasetosearchfor;
            } catch (Exception e) {
                return "UG song";
            }
        } else {
            return pagetitle;
        }
    }
*/
/*
    private String getAuthorUG(String s,String pageauthor) {
        int startpos = s.indexOf("Artist:");
        int endpos = s.indexOf("NEW_LINE_OS",startpos);
        if (startpos > -1 && endpos > -1 && endpos > startpos) {
            s = s.substring(startpos+7, endpos);
            s = stripExtraUG(s);
            return s;
        } else if (pageauthor==null) {
            return "";
        } else {
            return pageauthor;
        }
    }
*/

 /*   private String getLyricsUG(String s) {
        s = s.replace("&quot;", "");
        String[] lines = s.split("NEW_LINE_OS");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.contains("ultimate-guitar.com") && line.contains("{") &&
                    (line.contains(":true") || line.contains(":false")) && line.contains("content:")) {
                // No artist/author tags, so strip out div code line
                line = line.substring(line.indexOf("content:")+8);
            }
            line = line.replace("[tab]", "");
            line = line.replace("[/tab]", "");
            if (line.contains("[ch]")) {
                // Chord line
                line = line.replace("[ch]", "");
                line = line.replace("[/ch]", "");
                line = "." + line;
            } else {
                if (!line.startsWith("[") && !line.startsWith(" ")) {
                    line = " " + line;
                }
            }
            if (line.contains("tab_access_type:")) {
                int upto = line.indexOf("tab_access_type:");
                if (upto>0) {
                    line = line.substring(0,upto);
                    if (line.endsWith(",") && line.length()>1) {
                        line = line.substring(0,line.length()-1);
                    }
                } else {
                    line = "";
                }
            }
            stringBuilder.append(line).append("\n");
        }

        String string = stringBuilder.toString();
        string = PopUpEditSongFragment.parseToHTMLEntities(string);


        return string;
    }
*/
    /*
    private String stripExtraUG(String s) {
        s = s.replace(" @ Ultimate-Guitar.Com", "");
        s = s.replace("&amp;","&");
        s = s.replace("&","&amp;");
        s = s.replace("\r", "");
        s = s.replace("\n", "");
        s = s.replace("(Chords)","");
        s = PopUpEditSongFragment.parseToHTMLEntities(s);
        s = s.trim();
        return s;
    }
*/


/*
    private void fixUkutabsContent(String resultposted) {
        // From UkuTabs.com
        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default
        String title_resultposted;
        String filenametosave = "UkuTabs Song";
        authorname = "";
        newtext = "";

        int start;
        int end;

        resultposted = resultposted.replace("&quot;","'");
        resultposted = resultposted.replace("&amp;","&");
        resultposted = resultposted.replace("&#39;","'");
        resultposted = resultposted.replace("&#039;","'");

        if (resultposted.contains("<title>") && resultposted.contains("</title>")) {
            start = resultposted.indexOf("<title>") + 7;
            end = resultposted.indexOf("</title>");
            if (start>-1 && end>-1 && end>start) {
                String meta = resultposted.substring(start, end);
                if (meta.contains(" by ")) {
                    String[] bits = meta.split(" by ");
                    if (bits[0]!=null) {
                        if (bits[0].startsWith("'")) {
                            bits[0] = bits[0].substring(1);
                        }
                        if (bits[0].endsWith("'")) {
                            bits[0] = bits[0].substring(0,bits[0].length()-1);
                        }
                        filenametosave = bits[0];
                    }
                    if (bits[1]!=null) {
                        if (bits[1].startsWith("'")) {
                            bits[1] = bits[1].substring(1);
                        }
                        if (bits[1].endsWith("'")) {
                            bits[1] = bits[1].substring(0,bits[1].indexOf("'")-1);
                        }
                        authorname = bits[1];
                    }
                }
            }
        }

        // The meta stuff begins after the last typeof="v:Breadcrumb" text
        if (resultposted.contains("v:Breadcrumb") && resultposted.contains("post-meta")) {
            start = resultposted.lastIndexOf("v:Breadcrumb");
            end = resultposted.indexOf("post-meta",start);
            if (start>-1 && end>-1 && end>start) {
                String metadata = resultposted.substring(start,end);

                // Remove the rubbish and get the author
                start = metadata.indexOf("v:title");
                end = metadata.indexOf("</a>", start);
                if (start>-1 && end>-1 && end>start) {
                    metadata = metadata.substring(start);
                    start = metadata.indexOf(">") + 1;
                    authorname = metadata.substring(start, end);
                }

                // Remove the rubbish and get the title
                start = metadata.indexOf("breadcrumb_last\">");
                end = metadata.indexOf("</strong>",start);
                if (start>-1 && end>-1 && end>start) {
                    start = start + 17;
                    title_resultposted = metadata.substring(start, end);
                    title_resultposted = title_resultposted.replace("&apos;","'");
                    filenametosave = title_resultposted;
                }
            }
        }

        // Now try to extract the lyrics
        start = resultposted.indexOf("<pre class=\"qoate-code\">");
        end = resultposted.indexOf("</pre>");
        String templyrics = "";
        if (start>-1 && end>-1 && end>start) {
            templyrics = resultposted.substring(start + 24, end);
        }

        StringBuilder sb = new StringBuilder();

        // Split the lyrics into lines
        String[] lines = templyrics.split("\n");
        for (String l:lines) {

            // Remove the stuff we don't want
            l = l.replace("<span>","");
            l = l.replace("</span>","");

            // Try to sort the tags
            l = l.replace("<strong>","[");
            l = l.replace("</strong>","]");
            l = l.replace("]:","]");

            // Identify the chord lines
            boolean chordline = false;
            if (l.contains("<a")) {
                chordline = true;
            }

            // Remove any hyperlinks
            while (l.contains("<a")) {
                start = l.indexOf("<a");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }
            while (l.contains("</a>")) {
                l = l.replace("</a>","");
            }
            while (l.contains("<a>")) {
                l = l.replace("<a>","");
            }

            if (chordline) {
                l = "." + l;
            }

            // If we have tags and chords, split them
            if (l.startsWith(".") && l.contains("[") && l.contains("]")) {
                l = l.replace(".[", "[");
                l = l.replace("]","]\n.");
            }

            // Remove italics
            while (l.contains("<i ")) {
                start = l.indexOf("<i ");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }
            while (l.contains("</i>")) {
                l = l.replace("</i>","");
            }

            // Remove images
            while (l.contains("<img ")) {
                start = l.indexOf("<img ");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }

            l = l.replaceAll("<(.*?)>", "");
            l = l.replaceAll("<(.*?)\n", "");
            l = l.replaceFirst("(.*?)>", "");
            l = l.replaceAll("&nbsp;", " ");
            l = l.replaceAll("&amp;", "&");

            l = l.replace(">","");
            l = l.replace("<","");

            // Add a blank space to the beginning of lyrics lines
            if (!l.startsWith(".") && !l.startsWith("[")) {
                l = " " + l;
            }

            sb.append(l).append("\n");
        }

        newtext = TextUtils.htmlEncode(sb.toString());

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }
*/

  /*  private void fixHolyChordsContent(String resultposted) {
        // from holychords.com
        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted = "HolyChords Song";
        String filenametosave;
        authorname = "";
        int startpos;
        int endpos;

        // Try to get the best title
        // First up, use the page title
        if (resultposted.contains("<title>") && resultposted.contains("</title>")) {
            startpos = resultposted.indexOf("<title>") + 7;
            endpos = resultposted.indexOf("</title>");
            if (endpos>startpos) {
                title_resultposted = resultposted.substring(startpos,endpos);
                authorname = title_resultposted;
            }
        }

        // Fix author and title (if it was copied as the title);
        String text = authorname.replace("|","___");
        String[] titlebits = text.split("___");
        if (titlebits.length>1) {
            title_resultposted = titlebits[0].trim();
            authorname = titlebits[1].trim();
        }

        // If there is the title tag, use this instead
        startpos = resultposted.indexOf("<meta property=\"og:site_name\" content=\"") + 39;
        endpos = resultposted.indexOf(">",startpos);
        if (startpos>-1 && endpos>-1 && endpos>startpos) {
            title_resultposted = resultposted.substring(startpos,endpos);
            title_resultposted = title_resultposted.replace("/","");
            title_resultposted = title_resultposted.replace("/","");
            title_resultposted = title_resultposted.replace("\"","");
            title_resultposted = title_resultposted.trim();
        }

        filenametosave = title_resultposted;

        // Everything is found inside the <pre  and </pre> tags
        startpos = resultposted.indexOf("<pre");
        startpos = resultposted.indexOf(">",startpos) + 1;
        // Remove everything before this
        resultposted = resultposted.substring(startpos);
        // Get everything in the <pre> section
        endpos = resultposted.indexOf("</pre");
        if (endpos>0) {
            resultposted = resultposted.substring(0,endpos);
        }

        newtext = resultposted.replace("<br>","\n");
        newtext = newtext.replace("<br />","");

        newtext = PopUpEditSongFragment.parseToHTMLEntities(newtext);

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }
*/

    // Song Select Code
 /*   private String extractSongSelectChordPro(String s, String temptitle) {
        // Get the title
        String title = getTitleSongSelectChordPro(s, temptitle);

        // Extract the key
        String key = getKeySongSelectChordPro(s);

        // Extract the author
        String author = getAuthorSongSelectChordPro(s);

        // Extract the tempo and time signature
        String tempo = getTempoSongSelectChordPro(s);
        String timesig = getTimeSigSongSelectChordPro(s);

        // Extract the CCLI song number
        String ccli = getCCLISongSelectChordPro(s);

        // Extract the Copyright info
        String copyright = getCopyrightSongSelectChordPro(s);

        // Extract the lyrics
        String lyrics =  getLyricsSongSelectChordPro(s);

        // Return the ChordPro version of the song
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + key + tempo + timesig + "\n" + lyrics;
        }
    }*/
    /*private String getTitleSongSelectChordPro(String s, String temptitle) {
        // Extract the title
        int start = s.indexOf("<span class=\"cproTitle\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String t = s.substring(start+24,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                t = Html.fromHtml(t, 0).toString();
            } else {
                t = Html.fromHtml(t).toString();
            }
            filename = t;
            return "{title:" + t + "}\n";
        } else {
            return temptitle;
        }
    }*/
    /*private String getKeySongSelectChordPro(String s) {

        String[] lines = s.split("\n");
        for (String l:lines) {
            Log.d("FindNewSong", l);
        }
        int start = s.indexOf("<code class=\"cproSongKey\"");
        int end = s.indexOf("</code></span>",start);
        if (start>-1 && end>-1 && end>start) {
            // Fine tine the start
            int newstart = s.indexOf(">",start);
            if (newstart<0) {
                newstart = start;
            }
            return "{key:" + s.substring(newstart+1,end).trim() + "}\n";
        } else {
            return "";
        }
    }*/
    /*private String getAuthorSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproAuthors\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String a = s.substring(start+26,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                a = Html.fromHtml(a, 0).toString();
            } else {
                a = Html.fromHtml(a).toString();
            }
            return "{artist:" + a + "}\n";
        } else {
            return "";
        }
    }*/
    /*private String getCCLISongSelectChordPro(String s) {
        int start = s.indexOf("CCLI Song #");
        int end = s.indexOf("</p>",start);
        if (start>-1 && end>-1 && end>start) {
            return "{ccli:" + s.substring(start+11,end).trim() + "}\n";
        } else {
            return "";
        }
    }*/
    /*private String getCopyrightSongSelectChordPro(String s) {
        int start = s.indexOf("<ul class=\"copyright\">");
        start = s.indexOf("<li>",start);
        int end = s.indexOf("</li>",start);
        if (start>-1 && end>-1 && end>start) {
            return "{copyright:" + s.substring(start+4,end).trim() + "}\n";
        } else {
            return "";
        }
    }*/
    /*private String getTempoSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String both = s.substring(start+35,end);
            String[] bits = both.split("\\|");
            if (bits.length>0) {
                String t = bits[0].replace("Tempo", "");
                t = t.replace("-", "");
                t = t.replace("bpm", "");
                t = t.replace("BPM", "");
                t = t.replace("Bpm", "");
                t = t.trim();
                return "{tempo:" + t + "}\n";
            } else {
                return "";
            }
        }
        return "";
    }*/
    /*private String getTimeSigSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String both = s.substring(start+35,end);
            String[] bits = both.split("\\|");
            if (bits.length>1) {
                String t = bits[1].replace("Time","");
                t = t.replace("-","");
                t = t.trim();
                return "{time:" + t + "}\n";
            } else {
                return "";
            }
        }
        return "";
    }*/
    /*private String getLyricsSongSelectChordPro(String s) {
        int start = s.indexOf("<pre class=\"cproSongBody\">");
        int end = s.indexOf("</pre>",start);
        if (start>-1 && end>-1 && end>start) {
            String lyrics = s.substring(start+26,end);

            // Fix the song section headers
            while (lyrics.contains("<span class=\"cproSongSection\"><span class=\"cproComment\">")) {
                start = lyrics.indexOf("<span class=\"cproSongSection\"><span class=\"cproComment\">");
                end = lyrics.indexOf("</span>",start);
                String sectiontext;
                if (start>-1 && end>-1 && end>start) {
                    sectiontext = lyrics.substring(start+56,end);
                    lyrics = lyrics.replace("<span class=\"cproSongSection\"><span class=\"cproComment\">"+sectiontext+"</span>",sectiontext.trim()+":");
                }
            }

            // Fix the chords
            // Chords are found in a bit like this:
            // <span class="chordWrapper"><code class="chord" data-chordname="D<sup>2</sup>">D<sup>2</sup></code>
            // We wand the last D<sup>2</sup> bit (<sup> removed later).

            while (lyrics.contains("<span class=\"chordWrapper\"><code ")) {
                start = lyrics.indexOf("<span class=\"chordWrapper\"><code ");
                int newstart = lyrics.indexOf(">",start); // Move to bit before <
                newstart = lyrics.indexOf("\">",newstart)+2; // Go to bit after chordname="....">
                end = lyrics.indexOf("</code>",newstart);
                if (start>-1 && newstart>-1 && end>-1 && end>newstart) {
                    String chordfound = lyrics.substring(newstart,end);
                    String bittoremove = lyrics.substring(start,end+7);
                    lyrics = lyrics.replace(bittoremove,"["+chordfound+"]");
                }
            }

            // Get rid of code that we don't need
            return getRidOfRogueCode(lyrics);
        }
        return "";
    }*/

    /*private String extractSongSelectUsr(String s, String temptitle) {
        String title = temptitle;
        String author = "";
        String copyright = "";
        String ccli = "";
        String lyrics = "";

        int start;
        int end;

        start = s.indexOf("<div id=\"LyricsText\" style=\"display: none;\">");
        end = s.indexOf("</div>", start);
        if (start > -1 && end > -1 && end > start) {
            int newstart = s.indexOf(">", start);
            if (newstart < 0) {
                newstart = start;
            }
            String text = s.substring(newstart + 1, end).trim();

            // The first line is the title normally
            end = text.indexOf("\n");
            if (end > -1) {
                title = "{title:" + text.substring(0, end).trim() + "}\n";
                filename = text.substring(0, end).trim();
                text = text.substring(end).trim();
            }

            // Get the bottom bit
            String bottombit;
            start = text.indexOf("CCLI Song");
            if (start>-1) {
                bottombit = text.substring(start);
                // Remove this from the text (leaving the lyrics)
                text = text.replace(bottombit,"");

                // Now look for the stuff we want
                // Break it into lines
                String[] bottomlines = bottombit.split("\n");
                for (String line:bottomlines) {
                    // Is this the CCLI line?
                    if (line.contains("CCLI Song #")) {
                        line = line.replace("CCLI Song #","");
                        line = line.trim();
                        ccli = "{ccli:" + line + "}\n";

                        // Is this the copyright line?
                    } else if (line.contains("opyright") || line.contains("&#169;") || line.contains("©")) {
                        copyright = "{copyright:" + line.trim() + "}\n";

                        // Is this the author line?
                    } else if (!line.contains("For use solely") && !line.contains("Note:") && !line.contains("Licence No")) {
                        author = "{artist:" + line.trim() + "}\n";
                    }
                }

            }

            lyrics = text;
        }
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + "\n" + lyrics;
        }
    }*/

    /*private String getRidOfRogueCode(String lyrics) {
        // Get rid of lyric indications
        lyrics = lyrics.replace("<span class=\"chordLyrics\">","");

        // Get rid of the new line indications
        lyrics = lyrics.replace("<span class=\"cproSongLine\">","");

        // Get rid of the chord line only indications
        lyrics = lyrics.replace("<span class=\"cproSongLine chordsOnly\">","");

        // Get rid of directions indicators
        lyrics = lyrics.replace("<span class=\"cproDirectionWrapper\">","");
        lyrics = lyrics.replace("<span class=\"cproDirection\">","");

        // Get rid of any remaining close spans
        lyrics = lyrics.replace("</span>","");

        // Get rid of any superscripts or subscripts
        lyrics = lyrics.replace("<sup>","");
        lyrics = lyrics.replace("</sup>","");

        // Finally, trim the lyrics
        return lyrics.trim();
    }*/

/*    private String fixForeignLanguageHTML(String s) {
        s = s.replace("&iquest;","¿");
        s = s.replace("&Agrave;","À");
        s = s.replace("&agrave;","à");
        s = s.replace("&Aacute;","Á");
        s = s.replace("&aacute;","á");
        s = s.replace("&Acirc;;","Â");
        s = s.replace("&acirc;;","â");
        s = s.replace("&Atilde;","Ã");
        s = s.replace("&atilde;","ã");
        s = s.replace("&Aring;","Å");
        s = s.replace("&aring;", "å");
        s = s.replace("&Auml;","Ä");
        s = s.replace("&auml;","ä");
        s = s.replace("&AElig;","Æ");
        s = s.replace("&aelig;","æ");
        s = s.replace("&Cacute;","Ć");
        s = s.replace("&cacute;","ć");
        s = s.replace("&Ccedil;","Ç");
        s = s.replace("&ccedil;","ç");
        s = s.replace("&Eacute;","É");
        s = s.replace("&eacute;","é");
        s = s.replace("&Ecirc;;","Ê");
        s = s.replace("&ecirc;;","ê");
        s = s.replace("&Egrave;","È");
        s = s.replace("&egrave;","è");
        s = s.replace("&Euml;","Ë");
        s = s.replace("&euml;","ë");
        s = s.replace("&Iacute;","Í");
        s = s.replace("&iacute;","í");
        s = s.replace("&Icirc;;","Î");
        s = s.replace("&icirc;;","î");
        s = s.replace("&Igrave;","Ì");
        s = s.replace("&igrave;","ì");
        s = s.replace("&Iuml;","Ï");
        s = s.replace("&iuml;","ï");
        s = s.replace("&Oacute;","Ó");
        s = s.replace("&oacute;","ó");
        s = s.replace("&Ocirc;;","Ô");
        s = s.replace("&ocirc;;","ô");
        s = s.replace("&Ograve;","Ò");
        s = s.replace("&ograve;","ò");
        s = s.replace("&Ouml;","Ö");
        s = s.replace("&ouml;","ö");
        s = s.replace("&szlig;", "ß");
        s = s.replace("&Uacute;","Ú");
        s = s.replace("&uacute;","ú");
        s = s.replace("&Ucirc;;","Û");
        s = s.replace("&ucirc;;","û");
        s = s.replace("&Ugrave;","Ù");
        s = s.replace("&ugrave;","ù");
        s = s.replace("&Uuml;","Ü");
        s = s.replace("&uuml;","ü");
        return s;
    }*/


 /*   public Song fixChordieContent(String resultposted) {
        // Song is from Chordie!
        String temptitle = FullscreenActivity.phrasetosearchfor;

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
        // Find the position of the start of this section
        int getstart = resultposted.indexOf("<textarea id=\"chordproContent\"");
        int startpos = resultposted.indexOf("\">", getstart) + 2;
        if (getstart < 1) {
            // We are using the other version of the file content
            getstart = resultposted.indexOf("<h1 class=\"titleLeft\"");
            startpos = resultposted.indexOf(">",getstart) + 1;

            // Remove everything before this position
            resultposted = resultposted.substring(startpos);

            // The title is everything up to &nbsp; or <span>
            int endpos = resultposted.indexOf("&nbsp;");
            if (endpos>100) {
                endpos = resultposted.indexOf("<span>");
            }
            if (endpos>0) {
                temptitle = resultposted.substring(0, endpos);
            }

            // The author is between the first <span> and </span>
            startpos = resultposted.indexOf("<span>")+6;
            endpos = resultposted.indexOf("</span>");
            if (endpos>startpos && startpos>-1 && endpos<200) {
                authorname = resultposted.substring(startpos,endpos);
            }

            // Get the rest of the song content
            startpos = resultposted.indexOf("<div");
            endpos = resultposted.indexOf("<div id=\"importantBox\"");

            if (startpos>-1 && startpos<endpos) {
                resultposted = resultposted.substring(startpos,endpos);
            }

            StringBuilder contents = new StringBuilder();
            // Split into lines
            String[] lines = resultposted.split("\n");
            for (String line:lines) {
                line = line.replaceAll("<(.*?)>", "");
                line = line.replaceAll("<(.*?)\n", "");
                line = line.replaceFirst("(.*?)>", "");
                line = line.replaceAll("&nbsp;", " ");
                line = line.replaceAll("&amp;", "&");

                if (!line.equals("")) {
                    contents.append(line);
                    contents.append("\n");
                }
            }
            filename = temptitle.trim();
            filecontents = contents.toString();

        } else {
            // Remove everything before this position - using the desktop version
            resultposted = resultposted.substring(startpos);

            // Find the position of the end of the form
            int endpos = resultposted.indexOf("</textarea>");
            if (endpos < 0) {
                endpos = resultposted.length();
            }
            resultposted = resultposted.substring(0, endpos);

            //Replace all \r with \n
            resultposted = resultposted.replace("\r", "\n");
            resultposted = resultposted.trim();

            // Try to get the title of the song from the metadata
            int startpostitle = resultposted.indexOf("{t:");
            int endpostitle = resultposted.indexOf("}", startpostitle);
            if (startpostitle >= 0 && startpostitle < endpostitle && endpostitle < startpostitle + 50) {
                temptitle = resultposted.substring(startpostitle + 3, endpostitle);
            }
            filename = temptitle.trim();
            filecontents = resultposted;
        }
    }
*/
 /*   private Song fixWTContent(String resultposted) {
        // From Worship Together

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted;
        String filenametosave = "WT Song";
        authorname = "";
        String copyright = "";
        String bpm = "";
        String ccli = "";
        StringBuilder lyrics = new StringBuilder();
        String key = "";

        // Get the song title
        int startpos = resultposted.indexOf("<title>");
        int endpos = resultposted.indexOf("</title>");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            title_resultposted = resultposted.substring(startpos + 7, endpos);
            title_resultposted = title_resultposted.trim();
            int pos_of_extra = title_resultposted.indexOf(" - ");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            pos_of_extra = title_resultposted.indexOf("Lyrics and Chords");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            title_resultposted = title_resultposted.replace("|","");

            pos_of_extra = title_resultposted.indexOf("Worship Together");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            filenametosave = title_resultposted.trim();
            filename = filenametosave;
        }

        String song_taxonomy;
        startpos = resultposted.indexOf("<div class=\"song_taxonomy\">");
        endpos = resultposted.indexOf("<div class=\"t-setlist-details__related-list\">");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            // Extract the song taxonomy so we can edit this bit quickly
            song_taxonomy = resultposted.substring(startpos,endpos);

            // Try to get the author data
            startpos = song_taxonomy.indexOf("Writer(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                authorname = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+10,endpos));
            }

            // Try to get the copyright data
            startpos = song_taxonomy.indexOf("Ministry(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                copyright = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+12,endpos));
            }

            // Try to get the bpm data
            startpos = song_taxonomy.indexOf("BPM:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                bpm = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+4,endpos));
            }

            // Try to get the ccli data
            startpos = song_taxonomy.indexOf("CCLI #:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                ccli = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+7,endpos));
            }

            // Try to get the key data
            startpos = song_taxonomy.indexOf("Original Key(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                key = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+16,endpos));
            }

        }

        // Now try to get the chordpro file contents
        startpos = resultposted.indexOf("<div class='chord-pro-line'");
        endpos = resultposted.indexOf("<div class=\"song_taxonomy\">",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            lyrics = new StringBuilder(resultposted.substring(startpos, endpos));

            // Split the lines up
            String[] lines = lyrics.toString().split("\n");
            StringBuilder newline = new StringBuilder();
            lyrics = new StringBuilder();
            // Go through each line and do what we need
            for (String l : lines) {
                l = l.trim();
                boolean emptystuff = false;
                if (l.equals("</div") || l.contains("<div class='chord-pro-br'>") ||
                        l.contains("<div class='chord-pro-segment'>") || l.contains("<div class=\"inner_col")) {
                    emptystuff = true;
                }

                if (!emptystuff && l.contains("<div class=\"chord-pro-disp\"")) {
                    // Start section, so initialise the newline and lyrics
                    lyrics = new StringBuilder();
                    newline = new StringBuilder();

                } else if (!emptystuff && l.contains("<div class='chord-pro-line'>")) {
                    // Starting a new line, so add the previous newline to the lyrics text
                    lyrics.append("\n").append(newline);
                    newline = new StringBuilder();

                } else if (!emptystuff && l.contains("<div class='chord-pro-note'>")) {
                    // This is a chord
                    startpos = l.indexOf("<div class='chord-pro-note'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        String chordbit = l.substring(startpos+2,endpos);
                        if (!chordbit.isEmpty()) {
                            newline.append("[").append(l, startpos + 2, endpos).append("]");
                            //newline.append("[").append(l.substring(startpos + 2, endpos)).append("]");
                        }
                    }


                } else if (!emptystuff && l.contains("<div class='chord-pro-lyric'>")) {
                    // This is lyrics
                    startpos = l.indexOf("<div class='chord-pro-lyric'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        newline.append(l, startpos + 2, endpos);
                        //newline.append(l.substring(startpos + 2, endpos));
                    }
                }

            }

        }

        // Build the chordpro file contents:
        filecontents  = "{title:"+filenametosave+"}\n";
        filecontents += "{artist:"+authorname+"}\n";
        filecontents += "{copyright:"+copyright+"}\n";
        filecontents += "{ccli:"+ccli+"}\n";
        filecontents += "{key:"+key+"}\n";
        filecontents += "{tempo:"+bpm+"}\n\n";
        filecontents += lyrics.toString().trim();

        if (lyrics.toString().trim().isEmpty() || lyrics.toString().trim().equals("")) {
            filecontents = null;
        }
    }
*/
/*

*/

 /*   private String getRidOfExtraCodeWT(String s) {
        s = s.replace("<strong>","");
        s = s.replace("</strong>","");
        s = s.replace("<p>","");
        s = s.replace("</p>","");
        s = s.replace("</a>","");
        s = s.replace("<a>","");
        s = s.replace("<span>","");
        s = s.replace("</span>","");
        while (s.contains("<a href")) {
            // Remove the hypertext references
            int startpos = s.indexOf("<a href");
            int endpos = s.indexOf("'>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                String bittoremove = s.substring(startpos,endpos+2);
                s = s.replace(bittoremove,"");
            } else {
                // Problem, so just get rid of it all
                s = "";
            }
        }
        s = s.replace("\n","");
        s = s.trim();
        return s;
    }*/
}
