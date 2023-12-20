package com.garethevans.church.opensongtablet.bible;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

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

public class Bible {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Bible";
    public Bible(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }
    private final MainActivityInterface mainActivityInterface;
    // This class is instantiated from the Bible bottom sheets.  It will retain info until garbage collection
    private ArrayList<String> bibleFiles, defaultBibleBooks, bibleBooks, bibleChapters, bibleVerses, bibleTexts;
    private String bibleFile, bibleBook = "Genesis", bibleChapter = "1", bibleVerseFrom = "1",
            bibleVerseTo = "1", bibleFormat = "OpenSong", bibleTranslation;
    private String tagtosearch_book = "";
    private String attributetosearch_book = "";
    private String attributetosearch_chapter = "";
    private String attributetosearch_verse = "";
    private int lineLength = 50;
    private int linesPerSlide = 8;
    private boolean showVerseNumbers = true;
    private Document document;
    private Element documentElement;
    private XPath xpath;
    private NodeList nl;
    private NamedNodeMap nmm;
    Uri bibleUri;

    // Bible files in the OpenSong/OpenSong Scripture/ folder
    public void buildBibleFiles() {
        bibleFiles = new ArrayList<>();
        bibleFiles = mainActivityInterface.getStorageAccess().listFilesInFolder("OpenSong Scripture","");
        String myBiblePref = mainActivityInterface.getPreferences().getMyPreferenceString("bibleCurrentFile","");
        if (!myBiblePref.isEmpty() && bibleFiles.contains(myBiblePref)) {
            bibleFile = myBiblePref;
        }

    }
    public ArrayList<String> getBibleFiles() {
        return bibleFiles;
    }
    public String getBibleFile() {
        return bibleFile;
    }
    public void setBibleFile(String bibleFile) {
        this.bibleFile = bibleFile;
        mainActivityInterface.getPreferences().setMyPreferenceString("bibleCurrentFile",bibleFile);
        decideOnBibleFileFormat();
    }
    public void decideOnBibleFileFormat() {
        bibleUri = mainActivityInterface.getStorageAccess().getUriForItem("OpenSong Scripture","",bibleFile);
        if (bibleFileSafe()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(bibleUri);
                document = db.parse(inputStream);
                documentElement = document.getDocumentElement();
                XPathFactory xPathfactory = XPathFactory.newInstance();
                xpath = xPathfactory.newXPath();

                NodeList nl1 = documentElement.getElementsByTagName("b");            // OpenSong
                NodeList nl2 = documentElement.getElementsByTagName("BIBLEBOOK");    // Zefania
                if (nl1 != null && nl1.getLength() > 0) {
                    bibleFormat = "OpenSong";
                } else if (nl2 != null && nl2.getLength() > 0) {
                    bibleFormat = "Zefania";
                }
                getAttributeToSearch();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void getAttributeToSearch() {
        switch (bibleFormat) {
            case "OpenSong":
            default:
                tagtosearch_book = "b";
                attributetosearch_book = "n";
                attributetosearch_chapter = "n";
                attributetosearch_verse = "n";
                setOpenSongBibleName();
                break;

            case "Zefania":
                tagtosearch_book = "BIBLEBOOK";
                attributetosearch_book = "bname";
                attributetosearch_chapter = "cnumber";
                attributetosearch_verse = "vnumber";
                setZefaniaBibleName();
                break;
        }
    }
    private boolean bibleFileSafe() {
        return bibleFile!=null && !bibleFile.isEmpty() && mainActivityInterface.getStorageAccess().uriExists(bibleUri);
    }
    private void setZefaniaBibleName() {
        if (bibleFileSafe() && document!=null && xpath!=null) {
            try {
                XPathExpression expr = xpath.compile("/XMLBIBLE/INFORMATION/identifier");
                nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
                if (nl!=null && nl.getLength()>0) {
                    bibleTranslation = nl.item(0).getTextContent();
                }
            } catch (Exception e) {
                e.printStackTrace();
                bibleTranslation = bibleFile;
            }
        }
    }
    private void setOpenSongBibleName() {
        if (bibleFileSafe()) {
            bibleTranslation = bibleFile.replace(".xmm","");
        }
    }


    // Bible books found in the chosen file
    public void buildBibleBooks() {
        bibleBooks = new ArrayList<>();
        if (bibleFileSafe() && document!=null && documentElement!=null && xpath!=null) {
            try {
                nl = documentElement.getElementsByTagName(tagtosearch_book);
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
                        bibleBooks.add(val);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public ArrayList<String> getBibleBooks() {
        return bibleBooks;
    }
    public String getBibleBook() {
        return bibleBook;
    }
    public void setBibleBook(String bibleBook) {
        this.bibleBook = bibleBook;
    }
    public void buildDefaultBibleBooks() {
        defaultBibleBooks = new ArrayList<>();

        // Old Testament
        defaultBibleBooks.add("Genesis");
        defaultBibleBooks.add("Exodus");
        defaultBibleBooks.add("Leviticus");
        defaultBibleBooks.add("Numbers");
        defaultBibleBooks.add("Deuteronomy");
        defaultBibleBooks.add("Joshua");
        defaultBibleBooks.add("Judges");
        defaultBibleBooks.add("Ruth");
        defaultBibleBooks.add("1 Samuel");
        defaultBibleBooks.add("2 Samuel");
        defaultBibleBooks.add("1 Kings");
        defaultBibleBooks.add("2 Kings");
        defaultBibleBooks.add("1 Chronicles");
        defaultBibleBooks.add("2 Chronicles");
        defaultBibleBooks.add("Ezra");
        defaultBibleBooks.add("Nehemiah");
        defaultBibleBooks.add("Esther");
        defaultBibleBooks.add("Job");
        defaultBibleBooks.add("Psalms");
        defaultBibleBooks.add("Proverbs");
        defaultBibleBooks.add("Ecclesiastes");
        defaultBibleBooks.add("Song of Solomon");
        defaultBibleBooks.add("Isaiah");
        defaultBibleBooks.add("Jeremiah");
        defaultBibleBooks.add("Lamentations");
        defaultBibleBooks.add("Ezekiel");
        defaultBibleBooks.add("Daniel");
        defaultBibleBooks.add("Hosea");
        defaultBibleBooks.add("Joel");
        defaultBibleBooks.add("Amos");
        defaultBibleBooks.add("Obadiah");
        defaultBibleBooks.add("Jonah");
        defaultBibleBooks.add("Micah");
        defaultBibleBooks.add("Nahum");
        defaultBibleBooks.add("Habakkuk");
        defaultBibleBooks.add("Zephaniah");
        defaultBibleBooks.add("Haggai");
        defaultBibleBooks.add("Zechariah");
        defaultBibleBooks.add("Malachi");

        // New Testament
        defaultBibleBooks.add("Matthew");
        defaultBibleBooks.add("Mark");
        defaultBibleBooks.add("Luke");
        defaultBibleBooks.add("John");
        defaultBibleBooks.add("Acts (of the Apostles)");
        defaultBibleBooks.add("Romans");
        defaultBibleBooks.add("1 Corinthians");
        defaultBibleBooks.add("2 Corinthians");
        defaultBibleBooks.add("Galatians");
        defaultBibleBooks.add("Ephesians");
        defaultBibleBooks.add("Philippians");
        defaultBibleBooks.add("Colossians");
        defaultBibleBooks.add("1 Thessalonians");
        defaultBibleBooks.add("2 Thessalonians");
        defaultBibleBooks.add("1 Timothy");
        defaultBibleBooks.add("2 Timothy");
        defaultBibleBooks.add("Titus");
        defaultBibleBooks.add("Philemon");
        defaultBibleBooks.add("Hebrews");
        defaultBibleBooks.add("James");
        defaultBibleBooks.add("1 Peter");
        defaultBibleBooks.add("2 Peter");
        defaultBibleBooks.add("1 John");
        defaultBibleBooks.add("2 John");
        defaultBibleBooks.add("3 John");
        defaultBibleBooks.add("Jude");
        defaultBibleBooks.add("Revelation");
    }
    private String getBookNameFromNumber (String n) {
        try {
            int num = Integer.parseInt(n);
            //num = num;  // Make the first item 0
            if (num < defaultBibleBooks.size()) {
                return defaultBibleBooks.get(num);
            } else {
                return String.valueOf(num);
            }
        } catch (Exception e) {
            return n;
        }
    }
    private String getBookNumberFromName (String n) {
        for (int x=0; x<defaultBibleBooks.size(); x++) {
            if (n.equals(bibleBooks.get(x))) {
                // This is it.  Add one on to convert array to number 1-66 (not 0-65)
                //return ""+(x+1);
                return String.valueOf(x);
            }
        }
        return "1";
    }


    // Bible chapters for the chosen book in the chosen file
    public void buildBibleChapters() {
        bibleChapters = new ArrayList<>();

        // If the xml file only has booknums, we need to change back to this
        if (attributetosearch_book.equals("bnumber")) { //Zefania with no book names in the xml
            bibleBook = getBookNumberFromName(bibleBook);
        }

        if (bibleFileSafe() && document!=null && documentElement!=null && !bibleBook.isEmpty()) {
            try {
                nl = documentElement.getElementsByTagName(tagtosearch_book);
                for (int i=0; i<nl.getLength();i++) {
                    nmm = nl.item(i).getAttributes();
                    if (nmm != null) {
                        String foundbook = nmm.getNamedItem(attributetosearch_book).getNodeValue();
                        if (foundbook.equals(bibleBook)) {
                            NodeList children = nl.item(i).getChildNodes();
                            if (children != null) {
                                for (int y = 0; y < children.getLength(); y++) {
                                    NamedNodeMap nmm2 = children.item(y).getAttributes();
                                    if (nmm2 != null) {
                                        String s = nmm2.getNamedItem(attributetosearch_chapter).getNodeValue();
                                        bibleChapters.add(s);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public ArrayList<String> getBibleChapters() {
        return bibleChapters;
    }
    public String getBibleChapter() {
        return bibleChapter;
    }
    public void setBibleChapter(String bibleChapter) {
        this.bibleChapter = bibleChapter;
    }

    // Bible verses for the chosen chapter of the chosen book in the chosen file
    public void buildBibleVerses() {
        bibleVerses = new ArrayList<>();
        bibleTexts = new ArrayList<>();
        // If the xml file only has booknums, we need to change back to this
        if (attributetosearch_book.equals("bnumber")) { //Zefania with no book names in the xml
            bibleBook = getBookNumberFromName(bibleBook);
        }
        if (bibleFileSafe() && document!=null && documentElement!=null &&!bibleBook.equals("") && !bibleChapter.isEmpty()) {
            try {
                NodeList nl = documentElement.getElementsByTagName(tagtosearch_book);
                for (int i=0; i<nl.getLength();i++) {
                    NamedNodeMap nmm = nl.item(i).getAttributes();
                    if (nmm != null) {
                        String foundbook = nmm.getNamedItem(attributetosearch_book).getNodeValue();
                        if (foundbook.equals(bibleBook)) {
                            NodeList c_children = nl.item(i).getChildNodes();
                            if (c_children != null) {
                                for (int y = 0; y < c_children.getLength(); y++) {
                                    NamedNodeMap nmm2 = c_children.item(y).getAttributes();
                                    if (nmm2 != null) {
                                        String foundchapter = nmm2.getNamedItem(attributetosearch_chapter).getNodeValue();
                                        if (foundchapter.equals(bibleChapter)) {
                                            NodeList v_children = c_children.item(y).getChildNodes();
                                            if (v_children != null) {
                                                for (int z = 0; z < v_children.getLength(); z++) {
                                                    NamedNodeMap nmm3 = v_children.item(z).getAttributes();
                                                    if (nmm3 != null) {
                                                        String foundverse = nmm3.getNamedItem(attributetosearch_verse).getNodeValue();
                                                        String foundtext = v_children.item(z).getTextContent();
                                                        if (foundverse!=null && foundtext!=null && !foundverse.isEmpty() && !foundtext.isEmpty()) {
                                                            bibleVerses.add(foundverse);
                                                            bibleTexts.add(foundtext);
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public ArrayList<String> getBibleVerses() {
        return bibleVerses;
    }
    public String getBibleVerseFrom() {
        return bibleVerseFrom;
    }
    public String getBibleVerseTo() {
        return bibleVerseTo;
    }
    public void setBibleVerseFrom(String bibleVerseFrom) {
        this.bibleVerseFrom = bibleVerseFrom;
    }
    public void setBibleVerseTo(String bibleVerseTo) {
        this.bibleVerseTo = bibleVerseTo;
    }


    // Get the bible texts
    public String getBibleText() {
        if (bibleFileSafe()) {
            // Get from and to
            int from = Integer.parseInt(bibleVerseFrom);
            int to = Integer.parseInt(bibleVerseTo);

            if (from<1) {
                from = 1;
            }
            if (to<1) {
                to = 1;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=(from-1); i<=(to-1); i++) {
                // Array is 0-...  Verse numbers are from 1 though!  Add 1
                try {
                    stringBuilder.append("{").append(i + 1).append("}");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    stringBuilder.append(bibleTexts.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Trim and fix new sentence double spaces
            String string = stringBuilder.toString().trim();
            string = string.replace(".", ". ");
            string = string.replace(".  ", ". ");
            string = string.replace(". ", ".  ");
            return string;
        } else {
            return "";
        }
    }
    public String getTranslation() {
        return bibleTranslation;
    }

    // The line split value
    public int getLineLength() {
        return lineLength;
    }
    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
    }
    public int getLinesPerSlide() {
        return linesPerSlide;
    }
    public void setLinesPerSlide(int linesPerSlide) {
        this.linesPerSlide = linesPerSlide;
    }

    // Decide on showing verse numbers
    public boolean getShowVerseNumbers() {
        return showVerseNumbers;
    }
    public void setShowVerseNumbers(boolean showVerseNumbers) {
        this.showVerseNumbers = showVerseNumbers;
    }
}