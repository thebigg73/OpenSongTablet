package com.garethevans.church.opensongtablet.autoscroll;

public class AutoscrollActions {

    private boolean isAutoscrolling;
    private boolean wasScrolling;
    private boolean autoscrollOK;

    // The setters
    public void setIsAutoscrolling(boolean isAutoscrolling) {
        this.isAutoscrolling = isAutoscrolling;
    }
    public void setWasScrolling(boolean wasScrolling) {
        this.wasScrolling = wasScrolling;
    }
    public void setAutoscrollOK(boolean autoscrollOK) {
        this.autoscrollOK = autoscrollOK;
    }

    // The getters
    public boolean getIsAutoscrolling() {
        return isAutoscrolling;
    }
    public boolean getWasScrolling() {
        return wasScrolling;
    }
    public boolean getAutoscrollOK() {
        return autoscrollOK;
    }
}
