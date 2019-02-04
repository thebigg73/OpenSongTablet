package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

class Bible {

    private String tagtosearch_book = "";
    private String attributetosearch_book = "";
    private String attributetosearch_chapter = "";
    private String attributetosearch_verse = "";
    String bibleFormat = "OpenSong";

    private Document dom;
    private Element docEle;
    private XPath xpath;
    private NodeList nl;
    private NamedNodeMap nmm;

    private void getAttributeToSearch() {

        switch (bibleFormat) {
            case "OpenSong":
            default:
                tagtosearch_book = "b";
                attributetosearch_book = "n";
                attributetosearch_chapter = "n";
                attributetosearch_verse = "n";
                break;

            case "Zefania":
                tagtosearch_book = "BIBLEBOOK";
                attributetosearch_book = "bname";
                attributetosearch_chapter = "cnumber";
                attributetosearch_verse = "vnumber";
                break;
        }
    }

    private void decideOnBibleFormat(Context c, Uri bibleUri) {
        StorageAccess storageAccess = new StorageAccess();
        boolean exists = storageAccess.uriExists(c,bibleUri);
        try {
            if (exists) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputStream inputStream = storageAccess.getInputStream(c,bibleUri);
                dom = db.parse(inputStream);
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

    String getZefaniaBibleName(Context c, Uri bibleUri) {
        StorageAccess storageAccess = new StorageAccess();
        boolean exists = storageAccess.uriExists(c,bibleUri);
        String b = "";
        try {
            if (exists && dom!=null && xpath!=null) {
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

    ArrayList<String> getBibleBookNames(Context c, Uri bibleUri) {
        // Get the bible format

        decideOnBibleFormat(c, bibleUri);

        StorageAccess storageAccess = new StorageAccess();
        boolean exists = storageAccess.uriExists(c,bibleUri);

        ArrayList<String> bookNames = new ArrayList<>();
        try {
            // The bible we are using is stored with our preferences
            if (exists && dom!=null && xpath!=null) {
                bookNames.add("----"+c.getString(R.string.pleaseselect)+"----");

                docEle = dom.getDocumentElement();
                nl = docEle.getElementsByTagName(tagtosearch_book);
                for (int i=0; i<nl.getLength();i++) {
                    nmm = nl.item(i).getAttributes();
                    if (nmm != null) {
                        if (nmm.getNamedItem(attributetosearch_book)==null ) {
                            attributetosearch_book = "bsname";  // Use the short name instead (Zefania)
                            if (nmm.getNamedItem(attributetosearch_book)==null) {
                                attributetosearch_book = "bnumber"; // Use the book number instead (Zefania)
                            }
                        }
                        String val = nmm.getNamedItem(attributetosearch_book).getNodeValue();
                        if (attributetosearch_book.equals("bnumber")) {
                            val = getBookNameFromNumber(val);
                        }
                        bookNames.add(val);
                    }
                }

            } else {
                bookNames = blankArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
            bookNames = blankArray();
        }
        // Add a blank value at the top
        return bookNames;
    }

    ArrayList<String> getChaptersForBook(Context c, Uri bibleUri, String book) {

        // If the xml file only has booknums, we need to change back to this
        if (attributetosearch_book.equals("bnumber")) { //Zefania with no book names in the xml
            book = getBookNumberFromName(book);
        }
        StorageAccess storageAccess = new StorageAccess();
        boolean exists = storageAccess.uriExists(c,bibleUri);

        ArrayList<String> chapters = new ArrayList<>();
        if (!book.equals("")) {
            try {
                if (exists) {
                    chapters.add("----"+c.getString(R.string.pleaseselect)+"----");

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
                    chapters = blankArray();
                }

            } catch (Exception e) {
                e.printStackTrace();
                chapters = blankArray();
            }

        }
        return chapters;
    }

    void getVersesForChapter(Context c, Uri bibleUri, String book, String chapter) {

        // If the xml file only has booknums, we need to change back to this
        if (attributetosearch_book.equals("bnumber")) { //Zefania with no book names in the xml
            book = getBookNumberFromName(book);
        }

        StorageAccess storageAccess = new StorageAccess();
        boolean exists = storageAccess.uriExists(c,bibleUri);

        PopUpBibleXMLFragment.bibleVerses = new ArrayList<>();
        PopUpBibleXMLFragment.bibleText = new ArrayList<>();
        if (!book.equals("") && !chapter.equals("")) {
            try {
                if (exists) {

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
                    PopUpBibleXMLFragment.bibleVerses = blankArray();
                    PopUpBibleXMLFragment.bibleText = blankArray();
                }

            } catch (Exception e) {
                e.printStackTrace();
                PopUpBibleXMLFragment.bibleVerses = blankArray();
                PopUpBibleXMLFragment.bibleText = blankArray();
            }
        }
    }

    private ArrayList<String> blankArray() {
        ArrayList<String> al = new ArrayList<>();
        al.add("");
        return al;
    }
    boolean isYouVersionScripture(String importtext) {
        /* A simple way to check if this is a scripture file from Bible
         is to look for the last line starting with http://bible.com

         Split the string into separate lines
         If it is a scripture, the last line indicates so
         The second last line is the Scripture reference */
        String[] importtextparts = importtext.split("\n");
        int lines = importtextparts.length;
        String identifying_line = "http://bible.com";
        if (lines>2 && importtextparts[lines-1].contains(identifying_line)) {
            // Ok it is a scripture
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

    @SuppressWarnings("SameParameterValue")
    String shortenTheLines(String originaltext, int charsperline, int linesbeforenewslide) {
        StringBuilder scripture = new StringBuilder();
        // Split the current string into a separate words array
        String[] scripturewords = originaltext.split(" ");
        StringBuilder currentline= new StringBuilder();
        ArrayList<String> newimprovedscripture = new ArrayList<>();
        for (String words:scripturewords) {
            if (currentline.length()<charsperline) {
                currentline.append(" ").append(words);
            } else {
                newimprovedscripture.add(currentline.toString().trim());
                if (words.startsWith(";") || words.startsWith("|") || words.startsWith("[") || words.startsWith("]")) {
                    words = " " + words;
                }
                currentline = new StringBuilder(words);
            }
        }
        newimprovedscripture.add(currentline.toString());

        int newslideneeded = 0;
        for (int z=0;z<newimprovedscripture.size();z++) {
            scripture.append("\n").append(newimprovedscripture.get(z));
            newslideneeded ++;
            // Every linesbeforenewslide lines, start a new slide
            if (newslideneeded >= linesbeforenewslide) {
                scripture.append("\n---");
                newslideneeded = 0;
            }
        }

        return scripture.toString().trim();
    }

    private String[] bibleBooks = {"Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
            "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings",
            "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms",
            "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah",
            "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah",
            "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi",

            "Matthew", "Mark", "Luke", "John", "Acts (of the Apostles)", "Romans",
            "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians",
            "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy",
            "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter",
            "1 John", "2 John", "3 John", "Jude", "Revelation"};

    private String getBookNameFromNumber (String n) {
        try {
            int num = Integer.parseInt(n);
            num = num-1;  // Make the first item 0
            if (num < bibleBooks.length) {
                return bibleBooks[num];
            } else {
                return ""+num;
            }
        } catch (Exception e) {
            return n;
        }
    }

    private String getBookNumberFromName (String n) {
        for (int x=0; x<bibleBooks.length; x++) {
            if (n.equals(bibleBooks[x])) {
                // This is it.  Add one on to convert array to number 1-66 (not 0-65)
                return ""+(x+1);
            }
        }
        return "1";
    }
}