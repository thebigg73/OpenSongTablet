package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

class Bible {

    static String response = "";
    //private static String tagbibleroot = "";
    private static String tagtosearch_book = "";
    //private static String tagtosearch_chapter = "";
    //private static String tagtosearch_verse = "";
    private static String attributetosearch_book = "";
    private static String attributetosearch_chapter = "";
    private static String attributetosearch_verse = "";
    static String bibleFormat = "OpenSong";

    private static Document dom;
    private static Element docEle;
    private static XPath xpath;
    private static NodeList nl;
    private static NamedNodeMap nmm;

    private static void getAttributeToSearch() {
        Log.d("d","getAttributeToSearch() called");

        switch (bibleFormat) {
            case "OpenSong":
            default:
                tagtosearch_book = "b";
                //tagtosearch_chapter = "c";
                //tagtosearch_verse = "v";
                attributetosearch_book = "n";
                attributetosearch_chapter = "n";
                attributetosearch_verse = "n";
                break;

            case "Zefania":
                //tagbibleroot = "XMLBIBLE";
                tagtosearch_book = "BIBLEBOOK";
                //tagtosearch_chapter = "CHAPTER";
                //tagtosearch_verse = "VERSE";
                attributetosearch_book = "bname";
                attributetosearch_chapter = "cnumber";
                attributetosearch_verse = "vnumber";
                break;
        }
    }

    private static void decideOnBibleFormat(File bibleFile) {
        Log.d("d","decideOnBibleFormat() called");

        try {
            if (bibleFile.exists() && bibleFile.isFile()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                dom = db.parse(bibleFile);
                docEle = dom.getDocumentElement();
                XPathFactory xPathfactory = XPathFactory.newInstance();
                xpath = xPathfactory.newXPath();

                NodeList nl1 = docEle.getElementsByTagName("b");            // OpenSong
                NodeList nl2 = docEle.getElementsByTagName("BIBLEBOOK");    // Zefania
                if (nl1 != null && nl1.getLength() > 0) {
                    bibleFormat = "OpenSong";
                } else if (nl2 != null && nl2.getLength() > 0) {
                    bibleFormat = "Zefania";
                }
                getAttributeToSearch();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    static String getZefaniaBibleName(File bibleFile) {
        Log.d("d","getZefaniaBibleName() called");

        String b = "";
        try {
            if (bibleFile.exists() && bibleFile.isFile() && dom!=null && xpath!=null) {
                XPathExpression expr = xpath.compile("/XMLBIBLE/INFORMATION/identifier");
                nl = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);
                if (nl!=null && nl.getLength()>0) {
                    b = nl.item(0).getTextContent();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return b;
    }

    static ArrayList<String> getBibleBookNames(Context c, File bibleFile) {
        Log.d("d","getBibleBookNames() called");
        // Get the bible format
        decideOnBibleFormat(bibleFile);

        ArrayList<String> bookNames = new ArrayList<>();
        try {
            // The bible we are using is stored with our preferences
            if (bibleFile.exists() && bibleFile.isFile() && dom!=null && xpath!=null) {
                bookNames.add("----"+c.getString(R.string.pleaseselect)+"----");

                docEle = dom.getDocumentElement();
                nl = docEle.getElementsByTagName(tagtosearch_book);
                for (int i=0; i<nl.getLength();i++) {
                    nmm = nl.item(i).getAttributes();
                    if (nmm != null) {
                        bookNames.add(nmm.getNamedItem(attributetosearch_book).getNodeValue());
                    }
                }

            } else {
                bookNames = new ArrayList<>();
                bookNames.add("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            bookNames = new ArrayList<>();
            bookNames.add("");
        }
        return bookNames;
    }

    static ArrayList<String> getChaptersForBook(Context c, File bibleFile, String book) {
        Log.d("d","getChaptersForBook() called");

        ArrayList<String> chapters = new ArrayList<>();
        if (!book.equals("")) {
            try {
                if (bibleFile.exists()) {
                    chapters.add("----"+c.getString(R.string.pleaseselect)+"----");


                    /*// Get the bible format
                    decideOnBibleFormat(bibleFile);*/

                    /*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document dom = db.parse(bibleFile);

                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    */

                    /*expr = xpath.compile("/" + tagbibleroot + "/" + tagtosearch_book +
                            "[@" + attributetosearch_book + "='" + book + "']/" + tagtosearch_chapter);
                    nl = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);

                    if (nl != null && nl.getLength() > 0) {
                        for (int i = 0; i < nl.getLength(); i++) {
                            nmm = nl.item(i).getAttributes();
                            if (nmm != null) {
                                chapters.add(nmm.getNamedItem(attributetosearch_chapter).getNodeValue());
                            }
                        }
                    }*/

                    docEle = dom.getDocumentElement();
                    nl = docEle.getElementsByTagName(tagtosearch_book);
                    for (int i=0; i<nl.getLength();i++) {
                        nmm = nl.item(i).getAttributes();
                        if (nmm != null) {
                            String foundbook = nmm.getNamedItem(attributetosearch_book).getNodeValue();
                            if (foundbook.equals(book)) {
                                NodeList children = nl.item(i).getChildNodes();
                                if (children != null) {
                                    for (int y = 0; y < children.getLength(); y++) {
                                        NamedNodeMap nmm2 = children.item(y).getAttributes();
                                        if (nmm2 != null) {
                                            String s = nmm2.getNamedItem(attributetosearch_chapter).getNodeValue();
                                            chapters.add(s);
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    chapters = new ArrayList<>();
                    chapters.add("");
                }

            } catch (Exception e) {
                e.printStackTrace();
                chapters = new ArrayList<>();
                chapters.add("");
            }

        }
        return chapters;
    }

    static void getVersesForChapter(File bibleFile, String book, String chapter) {
        Log.d("d","getVersesForChapter() called");

        PopUpBibleXMLFragment.bibleVerses = new ArrayList<>();
        PopUpBibleXMLFragment.bibleText = new ArrayList<>();
        if (!book.equals("") && !chapter.equals("")) {
            try {
                if (bibleFile.exists() && bibleFile.isFile()) {
                    /*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document dom = db.parse(bibleFile);

                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    */

                    /*expr = xpath.compile("/" + tagbibleroot + "/" + tagtosearch_book +
                            "[@" + attributetosearch_book + "='" + book + "']/" + tagtosearch_chapter +
                            "[@" + attributetosearch_chapter + "='" + chapter + "']/" + tagtosearch_verse);
                    nl = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);

                    if (nl != null && nl.getLength() > 0) {
                        for (int i = 0; i < nl.getLength(); i++) {
                            nmm = nl.item(i).getAttributes();
                            if (nmm != null) {
                                String foundverse = nmm.getNamedItem(attributetosearch_verse).getNodeValue();
                                String foundtext = nl.item(i).getTextContent();
                                PopUpBibleXMLFragment.bibleVerses.add(foundverse);
                                PopUpBibleXMLFragment.bibleText.add(foundtext);
                            }
                        }
                    }*/

                    Element docEle = dom.getDocumentElement();
                    NodeList nl = docEle.getElementsByTagName(tagtosearch_book);
                    for (int i=0; i<nl.getLength();i++) {
                        NamedNodeMap nmm = nl.item(i).getAttributes();
                        if (nmm != null) {
                            String foundbook = nmm.getNamedItem(attributetosearch_book).getNodeValue();
                            if (foundbook.equals(book)) {
                                NodeList c_children = nl.item(i).getChildNodes();
                                if (c_children != null) {
                                    for (int y = 0; y < c_children.getLength(); y++) {
                                        NamedNodeMap nmm2 = c_children.item(y).getAttributes();
                                        if (nmm2 != null) {
                                            String foundchapter = nmm2.getNamedItem(attributetosearch_chapter).getNodeValue();
                                            if (foundchapter.equals(chapter)) {
                                                NodeList v_children = c_children.item(y).getChildNodes();
                                                if (v_children != null) {
                                                    for (int z = 0; z < v_children.getLength(); z++) {
                                                        NamedNodeMap nmm3 = v_children.item(z).getAttributes();
                                                        if (nmm3 != null) {
                                                            String foundverse = nmm3.getNamedItem(attributetosearch_verse).getNodeValue();
                                                            String foundtext = v_children.item(z).getTextContent();
                                                            PopUpBibleXMLFragment.bibleVerses.add(foundverse);
                                                            PopUpBibleXMLFragment.bibleText.add(foundtext);
                                                            Log.d("d", "book=" + book + "  chapter=" + chapter + "  verse=" + foundverse);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    PopUpBibleXMLFragment.bibleVerses = new ArrayList<>();
                    PopUpBibleXMLFragment.bibleText = new ArrayList<>();
                    PopUpBibleXMLFragment.bibleVerses.add("");
                    PopUpBibleXMLFragment.bibleText.add("");
                }

            } catch (Exception e) {
                e.printStackTrace();
                PopUpBibleXMLFragment.bibleVerses = new ArrayList<>();
                PopUpBibleXMLFragment.bibleText = new ArrayList<>();
                PopUpBibleXMLFragment.bibleVerses.add("");
                PopUpBibleXMLFragment.bibleText.add("");
            }
        }
    }

    static boolean isYouVersionScripture(String importtext) {
        // A simple way to check if this is a scripture file from Bible
        // is to look for the last line starting with http://bible.com

        // Split the string into separate lines
        // If it is a scripture, the last line indicates so
        // The second last line is the Scripture reference
        String[] importtextparts = importtext.split("\n");
        int lines = importtextparts.length;
        String identifying_line = "http://bible.com";
        if (lines>2 && importtextparts[lines-1].contains(identifying_line)) {
            // Ok it is a scripture
            FullscreenActivity.mScripture = importtextparts[0];
            if (importtextparts[1]!=null) {
                FullscreenActivity.scripture_title = importtextparts[1];
            } else {
                FullscreenActivity.scripture_title = "";
            }
            if (importtextparts[0]!=null) {
                FullscreenActivity.mScripture = importtextparts[0];
            } else {
                FullscreenActivity.mScripture = "";
            }
            return true;
        } else {
            return false;
        }
    }

    static String shortenTheLines(String originaltext, int charsperline, int linesbeforenewslide) {
        String scripture = "";
        // Split the current string into a separate words array
        String[] scripturewords = originaltext.split(" ");
        String currentline="";
        ArrayList<String> newimprovedscripture = new ArrayList<>();
        for (String words:scripturewords) {
            if (currentline.length()<charsperline) {
                currentline = currentline + " " + words;
            } else {
                newimprovedscripture.add(currentline.trim());
                if (words.startsWith(";") || words.startsWith("|") || words.startsWith("[") || words.startsWith("]")) {
                    words = " " + words;
                }
                currentline = words;
            }
        }
        newimprovedscripture.add(currentline);

        int newslideneeded = 0;
        for (int z=0;z<newimprovedscripture.size();z++) {
            scripture = scripture + "\n" + newimprovedscripture.get(z);
            newslideneeded ++;
            // Every linesbeforenewslide lines, start a new slide
            if (newslideneeded >= linesbeforenewslide) {
                scripture = scripture + "\n---";
                newslideneeded = 0;
            }
        }

        return scripture.trim();
    }

    static void grabBibleText(Context c, String weblink) {
        DownloadWebTextTask task = new DownloadWebTextTask(c);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,weblink);
    }
    private static class DownloadWebTextTask extends AsyncTask<String, Void, String> {
        @SuppressLint("StaticFieldLeak")
        Context c;

        DownloadWebTextTask (Context context) {
            c = context;
        }
        @Override
        protected String doInBackground(String... addresses) {
            response = "";
            for (String address:addresses) {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(address);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        response += "\n" + s;
/*
                        if (s.contains("<meta name=\"twitter:title\" content=\"")) {
                            gottitle=true;
                        }
                        if (s.contains("<meta property=\"og:description\" content=\"")) {
                            gotscripture=true;
                        }
*/

                        // OVERRIDE THIS BIT FOR NOW WHILE I TEST FULL SCRIPTURE EXTRACT
/*
                        if (s.contains("<meta property=\"al:ios:url\"") || (gottitle && gotscripture)) {
                            // Force s to be null as we've got all we need!
                            break;
                        }
*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String result)  {
            String scripture;
            String scripture_title = "";

            // TEST THE FULLY EXTRACTED SCRIPTURE (FULLER THAN HEADER)
            String newbit = result;

            // Find the start and end of the scripture bit
            int startoffull = newbit.indexOf("<sup class=\"versenum\">");
            int endoffull   = newbit.indexOf("<div class=\"crossrefs hidden\">");

            if (endoffull>startoffull && startoffull>0 && endoffull>0) {
                newbit = newbit.substring(startoffull,endoffull);
            } else {
                FullscreenActivity.myToastMessage = c.getResources().getString(R.string.error_missingsection);
                ShowToast.showToast(c);
            }

            newbit = Html.fromHtml(newbit).toString();
            newbit = newbit.replace("<p>","");
            newbit = newbit.replace("</p>","");
            //newbit = newbit.replace("\n","");

            //Now look to see if the webcontent has the desired text in it
            if (result.contains("og:description")) {
                // Find the position of the start of this section
                // Get the scripture
/*
                int script_startpos = result.indexOf("og:description\" content=\"")+25;
                int script_endpos = result.indexOf("\"/>",script_startpos);
                try {
                    scripture = result.substring(script_startpos,script_endpos);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("d","Error getting scripture");
                    FullscreenActivity.myToastMessage = FullscreenActivity.error_missingsection;
                    ShowToast.showToast(context);
                }
*/

                // Get the title
                int title_startpos = result.indexOf("<meta name=\"twitter:title\" content=\"")+36;
                int title_endpos   = result.indexOf("\" />",title_startpos);

                try {
                    scripture_title = result.substring(title_startpos,title_endpos);
                } catch (Exception e) {
                    Log.d("d","Error getting scripture title");
                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.error_missingsection);
                    ShowToast.showToast(c);
                }

                // Make the scripture more readable by making a line break at the start of the word after 40 chars
                // First split the scripture into an array of words
                //String[] scripturewords = scripture.split(" ");

                scripture = shortenTheLines(newbit, 40, 6);

                // Send these back to the popupcustomslide creator window
                FullscreenActivity.scripture_title = scripture_title;
                FullscreenActivity.scripture_verse = scripture;

                PopUpCustomSlideFragment.addScripture();

            } else {
                FullscreenActivity.myToastMessage = c.getResources().getString(R.string.error_missingsection);
                ShowToast.showToast(c);
            }
        }
    }

}
