package com.garethevans.church.opensongtablet.pdf;

public class PDFSong {

    // This holds info about the pdf
    private int pdfPageCount;
    private int pdfPageCurrent;
    private boolean showstartofpdf;
    private String direction;

    // The setters
    public void setPdfPageCount(int pdfPageCount) {
        this.pdfPageCount = pdfPageCount;
    }
    public void setPdfPageCurrent(int pdfPageCurrent) {
        this.pdfPageCurrent = pdfPageCurrent;
    }
    public void setShowstartofpdf(boolean showstartofpdf) {
        this.showstartofpdf = showstartofpdf;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }

    // The getters
    public int getPdfPageCount() {
        return pdfPageCount;
    }
    public int getPdfPageCurrent() {
        return pdfPageCurrent;
    }
    public boolean getShowstartofpdf() {
        return showstartofpdf;
    }
    public String getDirection() {
        return direction;
    }
}
