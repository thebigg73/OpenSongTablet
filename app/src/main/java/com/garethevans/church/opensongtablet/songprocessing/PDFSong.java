package com.garethevans.church.opensongtablet.songprocessing;

public class PDFSong {

    // This holds info about the pdf
    private int pdfPageCount;
    private int pdfPageCurrent;
    private boolean showstartofpdf;

    // The setters
    void setPdfPageCount(int pdfPageCount) {
        this.pdfPageCount = pdfPageCount;
    }
    void setPdfPageCurrent(int pdfPageCurrent) {
        this.pdfPageCurrent = pdfPageCurrent;
    }
    void setShowstartofpdf(boolean showstartofpdf) {
        this.showstartofpdf = showstartofpdf;
    }

    // The getters
    int getPdfPageCount() {
        return pdfPageCount;
    }
    int getPdfPageCurrent() {
        return pdfPageCurrent;
    }
    boolean getShowstartofpdf() {
        return showstartofpdf;
    }

}
