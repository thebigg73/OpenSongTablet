package com.garethevans.church.opensongtablet.interfaces;

public interface DisplayInterface {

    // General display tasks (from any mode)
    void checkDisplays();
    void updateDisplay(String what);

    // Presenter Mode tasks
    void presenterShowSection(int position);

    // Performance and Stage Mode tasks
    void performanceShowSection(int position);

}
