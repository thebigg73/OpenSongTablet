package com.garethevans.church.opensongtablet.abcnotation;

public class InlineAbcWebViewTagObject {
    private String abc = "";
    private int webViewNum = -1;
    private boolean isForSecondary = false;

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

    public void setIsForSecondary(boolean isForSecondary) {
        this.isForSecondary = isForSecondary;
    }

    public boolean getIsForSecondary() {
        return isForSecondary;
    }

    public int getWebViewNum() {
        return webViewNum;
    }
}
