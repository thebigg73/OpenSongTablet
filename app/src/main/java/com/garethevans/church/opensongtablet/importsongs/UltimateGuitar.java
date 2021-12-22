package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class UltimateGuitar {

    // Song is effectively written in <pre> formatting with chords above lyrics.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is htmlentitied - i.e. " is shown as &quot;, ' is shown as &#039;

    private final String[] bitsToClear = new String[] {"</span>", "(Chords)"};

    // New lines are identified as new lines

    public Song processContent(Context c, MainActivityInterface mainActivityInterface,
                               Song newSong, String s) {
        // First up separate the content from the headers
        String headerTitle = getHeaderTitle(s);

        // Get the title and author from this
        newSong.setFiletype("XML");
        newSong.setTitle(getTitle(c,mainActivityInterface,headerTitle));
        newSong.setFilename(newSong.getTitle());
        newSong.setAuthor(getAuthor(c,mainActivityInterface,headerTitle));

        // Get the key which might be in a div
        newSong.setKey(getKey(s));
        newSong.setCapo(getCapo(s));

        /*
        String[] ls = s.split("\n");
        for (String l:ls) {
            Log.d(TAG,l);
        }

        2021-06-28 17:59:01.903 19508-19508/com.garethevans.church.opensongtablet D/d: line:            <div class="js-page js-global-wrapper"><div class="_2Js2x"></div><div class="wgutd"><div class="WfRXW _1okNR SiteWideBanner"><a href="https://www.ultimate-guitar.com/pro/?artist=Misc%20Praise%20Songs&amp;song=I%20Surrender%20All&amp;utm_campaign=PermanentBanner&amp;utm_content&amp;utm_medium=TopBanner&amp;utm_source=UltimateGuitar" target="_blank"><div class="_11qhT"><div class="_2GW7i _2mQdt _9CyMm s8n4E Q2t4b"><div class="GfRfl"><span class="_1jD_J">Summer sale: <span class="Y9TTk">Pro Access 80% OFF</span></span><div class="skij3"><div class="_1Koj8"><div>0</div><div>days</div></div><div class="_2JyA-">:</div><div class="_1Koj8"><div>23</div><div>hrs</div></div><div class="_2JyA-">:</div><div class="_1Koj8"><div>33</div><div>min</div></div><div class="_2JyA-">:</div><div class="_1Koj8"><div>17</div><div>sec</div></div></div><button type="button" class="_14yTH _1mR8e ECe2T _2ITWR _31tuI _2gkW5 _3zFY1"><span class="_2_Eks _1TkzG">GET SPECIAL OFFER</span></button></div></div></div></a><button type="button" class="_3AfOI _10C8S _14yTH _1mR8e _1u_VY _31tuI _2gkW5 _3zFY1"><svg class="_3QZQf _2rPDt _1NYjE M8cMk _4yvEM" viewBox="0 0 16 16"><path d="M9.414 8l3.44-3.439a.499.499 0 0 0 0-.707l-.708-.708a.5.5 0 0 0-.707 0L8 6.586l-3.44-3.44a.5.5 0 0 0-.707 0l-.707.708a.5.5 0 0 0 0 .707L6.586 8l-3.44 3.439a.5.5 0 0 0 0 .707l.707.708a.5.5 0 0 0 .707 0L8 9.414l3.44 3.44a.5.5 0 0 0 .706 0l.707-.708a.499.499 0 0 0 0-.707L9.414 8z" fill-rule="nonzero"></path></svg></button></div><div class="_1-S8P"><div class="n5ucf"><p class="xk9Qx"><a class="_3D84Q _1IgUn _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com"><svg width="39" height="40" viewBox="0 0 39 40" class="muWyX _3geIg"><path d="M38.438 18.97c.053.568.038 1.394.038 1.975.025 10.502-8.57 19.032-19.195 19.055C8.657 40.023.023 31.53 0 21.03A18.894 18.894 0 0 1 5.707 7.475L3.76 5.63V0l5.146 4.958a19.462 19.462 0 0 1 20.79.079L34.998 0v5.631L32.875 7.58a18.9 18.9 0 0 1 4.716 7.674h-4.306C30.102 7.612 21.254 3.97 13.522 7.115 5.789 10.263 2.103 19.007 5.288 26.65c3.183 7.64 12.032 11.284 19.764 8.136 4.915-2 8.409-6.4 9.204-11.596H21.604l-4.015-4.22h20.849z" fill-rule="nonzero"></path></svg><span class="_3jjrV">ultimate<br>guitar<br>com</span></a></p><nav class="xk9Qx"><div class="_1N_vJ"><a class="_2Tety _2hIzM _28IxZ _1vnj7 _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com/explore"><span class="_3DU-x hn34w _3dYeW _1rzfW">Tabs</span></a><a class="_2Tety _2hIzM _28IxZ _1vnj7 _1UEJY _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com/shots/explore/"><span class="_3DU-x hn34w _3dYeW _1rzfW">Shots</span><div class="_2CYrx _3kjj7 _2DVq3">new</div></a><a class="_2Tety _2hIzM _28IxZ _1vnj7 _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com/news/"><span class="_3DU-x hn34w _3dYeW _1rzfW">Articles</span></a><a class="_2Tety _2hIzM _28IxZ _1vnj7 _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com/forum/"><span class="_3DU-x hn34w _3dYeW _1rzfW">Forums</span></a><a class="_2Tety _2hIzM _28IxZ _1vnj7 _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com/contribution/submit/tabs?utm_campaign=top_menu&amp;utm_content=button&amp;utm_medium=internal&amp;utm_source=ug&amp;utm_term=publish"><span class="text-primary _27ECw">+ </span><span class="_3DU-x hn34w _3dYeW _1rzfW">Publish tab</span></a><a class="_2Tety _2hIzM _28IxZ _1vnj7 _293yJ _3DU-x hn34w _3dYeW" href="https://www.ultimate-guitar.com/pro/?utm_campaign=UG%20Tab%20Pro&amp;utm_content=View%20Index&amp;utm_medium=Header&amp;utm_source=UltimateGuitar"><span class="_3DU-x hn34w _3dYeW _1rzfW">Pro</span></a></div></nav><p iscontent="true" class="xk9Qx _24AaS"><form action="https://www.ultimate-guitar.com/search.php" class="_1dnQP AHe46"><div class="_1nC5x _2c2jQ"><input class="_12b3b hXuKo _1icVK" blurdelay="150" size="4" autocomplete="off" autocorrect="off" autocapitalize="off" required="" value="" type="text" name="value" state="dark" placeholder="Enter artist name or song title"><div class="GleS-"><button type="button" class="_14yTH _1KmoP _1u_VY _31t

        */

        // Trim out everything around the lyrics/content
        String contentStart = "<div class=\"ugm-b-tab--content js-tab-content\">";
        int start = s.indexOf(contentStart);
        String contentEnd = "</pre>";
        int end = s.indexOf(contentEnd,start);
        if (start>=0 && end>start) {
            s = s.substring(start+ contentStart.length(),end);
        }
        // Trim the lyrics start right up to the <pre tag
        start = s.indexOf("<pre");
        end = s.indexOf(">",start);
        if (start>0 && end>start) {
            s = s.substring(end+1);
        }

        // Split the content into lines
        String[] lines = s.split("\n");
        StringBuilder lyrics = new StringBuilder();
        for (String line:lines) {
            String chordIdentfier = "<span class=\"text-chord js-tab-ch js-tapped\">";
            if (line.contains(chordIdentfier)) {
                // Make it a chord line
                line = "." + line;
                line = line.replaceAll(chordIdentfier, "");
                line = line.trim();
            } else if (mainActivityInterface.getProcessSong().looksLikeGuitarTab(line)) {
                // Looks like a tab line
                line = mainActivityInterface.getProcessSong().fixGuitarTabLine(line);
                line = line.trim();
            } else if (mainActivityInterface.getProcessSong().looksLikeHeadingLine(line)) {
                // Looks like it is a heading line
                line = mainActivityInterface.getProcessSong().fixHeadingLine(line);
                line = line.trim();
            } else {
                // Assume it is a lyric line
                line = " " + line;
            }
            line = stripOutTags(line);
            line = fixHTMLStuff(c,mainActivityInterface,line);
            lyrics.append(line).append("\n");
        }
        newSong.setLyrics(lyrics.toString());

        // If we hae a capo (which means the key and the chords won't match in UG)
        // We will need to transpose the lyrics to match
        newSong = fixChordsForCapo(c,mainActivityInterface,newSong);
        return newSong;
    }

    private String getHeaderTitle(String s) {
        // We use this for the title and author
        int start = s.indexOf("<title>");
        int end = s.indexOf("</title>",start);
        if (start>=0 && end>start) {
            s = s.substring(start+7,end);
            return clearOutRubbish(s);
        } else {
            return "";
        }
    }
    private String getTitle(Context c, MainActivityInterface mainActivityInterface, String s) {
        // Likely to be sent something like <title>{TitleInCaps} CHORDS by {Author} @ Ultimate-Guitar.Com</title>
        // Give options in decreasing order of goodness
        int end = s.indexOf("CHORDS");
        if (end<0) {
            end = s.indexOf("by");
        }
        if (end<0) {
            end = s.length();
        }
        s = s.substring(0,end);
        // Try to sort the default capitalisation
        s = s.toLowerCase(mainActivityInterface.getLocale());
        // Capitalise the first letter
        if (s.length()>1) {
            s = s.substring(0,1).toUpperCase(mainActivityInterface.getLocale()) + s.substring(1);
        }
        s = s.trim();
        s = fixHTMLStuff(c,mainActivityInterface,s);
        return s;
    }
    private String getAuthor(Context c, MainActivityInterface mainActivityInterface, String s) {
        // Likely to be sent something like <title>{TitleInCaps} CHORDS by {Author} @ Ultimate-Guitar.Com</title>
        // Give options in decreasing order of goodness
        int end = s.indexOf("by ");
        if (end>0) {
            s = s.substring(end+3);
        } else {
            s = "";
        }
        s = s.trim();
        s = fixHTMLStuff(c,mainActivityInterface,s);
        return s;
    }
    private String getKey(String s) {
        return getMetaData(s, "<div class=\"label\">Key</div>");
    }
    private String getCapo(String s) {
        return getMetaData(s,"<div class=\"label\">Capo</div>");
    }
    private Song fixChordsForCapo(Context c, MainActivityInterface mainActivityInterface, Song newSong) {
        // If there is a capo, we have to transpose the song to match
        // UG shows the capo chords and the key but they don't match!
        // We want the actual chords to be stored in the file
        // The app takes care of showing capo/native chords
        String capo = newSong.getCapo();
        if (!capo.isEmpty()) {
            // Try to get a number
            capo = capo.replaceAll("[^\\d]", "");
            if (!capo.isEmpty()) {
                String key = newSong.getKey();
                String lyrics = newSong.getLyrics();
                try {
                    int transpnum = Integer.parseInt(capo);
                    newSong.setCapo(""+transpnum);
                    // So far so good.  Transpose the song
                    // Keep the original key ref

                    newSong = mainActivityInterface.getTranspose().
                            doTranspose(c,mainActivityInterface,newSong,
                                    "+1",transpnum,newSong.getDetectedChordFormat(),
                                    newSong.getDesiredChordFormat());
                    // Put the original key back as we only want transposed lyrics
                    newSong.setKey(key);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Repair anything that was broken
                    newSong.setKey(key);
                    newSong.setCapo("");
                    newSong.setLyrics(lyrics);
                }
            }
        }
        return newSong;
    }

    private String clearOutRubbish(String s) {
        s = s.replace("@ Ultimate-Guitar.Com","");
        s = s.trim();
        return s;
    }

    private String stripOutTags(String s) {
        for (String bit:bitsToClear) {
            s = s.replace(bit,"");
        }
        s = s.replaceAll("<(.*?)>", "");
        return s;
    }

    private String getMetaData(String s, String identifer) {
        if (s.contains(identifer) && s.contains("<div class=\"tag\">") && s.contains("</div>")) {
            int pos1 = s.indexOf(identifer);
            int pos2 = s.indexOf("<div class=\"tag\">",pos1);
            pos2 = s.indexOf(">",pos2)+1;
            int pos3 = s.indexOf("</div>",pos2+1);
            if (pos2 > 0 && pos3 > pos2) {
                return s.substring(pos2, pos3).trim();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
    private String fixHTMLStuff(Context c, MainActivityInterface mainActivityInterface, String s) {
        // Fix html entities to more user friendly
        s = mainActivityInterface.getProcessSong().parseHTML(s);
        // Make it xml friendly though (no <,> or &)
        s = mainActivityInterface.getProcessSong().parseToHTMLEntities(s);
        return s;
    }

}
