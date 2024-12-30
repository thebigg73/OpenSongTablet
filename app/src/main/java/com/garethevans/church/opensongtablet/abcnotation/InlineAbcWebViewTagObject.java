package com.garethevans.church.opensongtablet.abcnotation;

public class InlineAbcWebViewTagObject {
    private String abc = "";
    private int webViewNum = -1;

    // This is used to set some tag information for WebViews
    public void setAbc(String abc) {
        this.abc = abc;
    }

    public String getAbc() {
        return abc;
    }

    public void setWebViewNum(int webViewNum) {
        this.webViewNum = webViewNum;
    }

    public int getWebViewNum() {
        return webViewNum;
    }
}
