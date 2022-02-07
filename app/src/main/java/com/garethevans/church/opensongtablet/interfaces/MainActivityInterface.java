package com.garethevans.church.opensongtablet.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.garethevans.church.opensongtablet.abcnotation.ABCNotation;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.AlertChecks;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.appdata.SoftKeyboard;
import com.garethevans.church.opensongtablet.appdata.VersionNumber;
import com.garethevans.church.opensongtablet.autoscroll.Autoscroll;
import com.garethevans.church.opensongtablet.bible.Bible;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.chords.Transpose;
import com.garethevans.church.opensongtablet.controls.Gestures;
import com.garethevans.church.opensongtablet.controls.PageButtons;
import com.garethevans.church.opensongtablet.controls.PedalActions;
import com.garethevans.church.opensongtablet.controls.Swipes;
import com.garethevans.church.opensongtablet.customslides.CustomSlide;
import com.garethevans.church.opensongtablet.customviews.DrawNotes;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.export.PrepareFormats;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.SaveSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.pads.Pad;
import com.garethevans.church.opensongtablet.pdf.MakePDF;
import com.garethevans.church.opensongtablet.pdf.OCR;
import com.garethevans.church.opensongtablet.pdf.PDFSong;
import com.garethevans.church.opensongtablet.performance.DisplayPrevNext;
import com.garethevans.church.opensongtablet.performance.PerformanceGestures;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.ProfileActions;
import com.garethevans.church.opensongtablet.presenter.PresenterSettings;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.DoVibrate;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongSheetHeaders;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.garethevans.church.opensongtablet.tools.TimeTools;

import java.util.ArrayList;
import java.util.Locale;

public interface MainActivityInterface {

    // Initialising the activity and settings
    void initialiseActivity();
    void updateSizes(int width, int height);
    int[] getDisplayMetrics();
    void setWindowFlags();
    Locale getLocale();
    VersionNumber getVersionNumber();
    String getMode();
    void setMode(String whichMode);
    void setFirstRun(boolean firstRun);
    boolean getFirstRun();
    SoftKeyboard getSoftKeyboard();

    // Preferences and settings
    Preferences getPreferences();
    PresenterSettings getPresenterSettings();
    ProfileActions getProfileActions();
    SetTypeFace getMyFonts();
    ThemeColors getMyThemeColors();

    // Song stuff
    Song getSong();
    Song getIndexingSong();
    Song getTempSong();
    LoadSong getLoadSong();
    SaveSong getSaveSong();
    void setSong(Song song);
    void setTempSong(Song tempSong);
    void updateKeyAndLyrics(Song song);

    // Metronome
    Metronome getMetronome();

    // Pads
    Pad getPad();
    boolean playPad();

    // Autoscroll
    Autoscroll getAutoscroll();
    void toggleAutoscroll();

    // Set stuff
    CurrentSet getCurrentSet();
    SetActions getSetActions();
    void updateSetTitle();
    void refreshSetList();
    void updateSetList();
    void addSetItem(int currentSetPosition);
    void removeSetItem(int currentSetPosition);

    // Menus
    DrawerLayout getDrawer();
    void lockDrawer(boolean lock);
    void closeDrawer(boolean close);
    void moveToSongInSongMenu();
    int getPositionOfSongInMenu();
    void updateSongList();
    void quickSongMenuBuild();
    void setFullIndexRequired(boolean fullIndexRequired);
    void fullIndex();
    void indexSongs();
    void updateSongMenu(String fragName, Fragment callingFragment, ArrayList<String> arguments);
    void updateSongMenu(Song song);
    void chooseMenu(boolean showSetMenu);
    void setIndexingSong(Song indexingSong);
    Song getSongInMenu(int position);
    boolean getShowSetMenu();
    void refreshMenuItems();
    ArrayList<Song> getSongsInMenu();
    ArrayList<Song> getSongsFound(String whichMenu);
    SongListBuildIndex getSongListBuildIndex();

    // Action bar
    ActionBar getMyActionBar();
    AppActionBar getAppActionBar();
    BatteryStatus getBatteryStatus();
    void hideActionBar(boolean hide);
    void showHideActionBar();
    void updateToolbar(String what);
    void updateActionBarSettings(String prefName, float floatval, boolean isvisible);

    // Page button(s)
    PageButtons getPageButtons();
    void hideActionButton(boolean hide);
    void updatePageButtonLayout();

    // Controls
    PedalActions getPedalActions();
    Gestures getGestures();
    PerformanceGestures getPerformanceGestures();
    Swipes getSwipes();
    void enableSwipe(boolean canSwipe);

    // Navigation
    void navHome();
    void navigateToFragment(String deepLink, int id);
    void popTheBackStack(int id, boolean inclusive);
    void registerFragment(Fragment frag, String what);
    void updateFragment(String fragName, Fragment callingFragment, ArrayList<String> arguments);
    int getFragmentOpen();
    FragmentManager getMyFragmentManager();

    // Showcase
    ShowCase getShowCase();
    void showTutorial(String what,ArrayList<View> viewsToHighlight);

    // File work
    StorageAccess getStorageAccess();
    void doSongLoad(String folder, String filename, boolean closeDrawer);
    void loadSongFromSet(int position);
    void doExport(String what);
    ExportActions getExportActions();
    Uri getImportUri();
    void setImportFilename(String importFilename);
    void setImportUri(Uri importUri);
    String getImportFilename();
    PrepareFormats getPrepareFormats();

    // Nearby connections
    NearbyConnections getNearbyConnections(MainActivityInterface mainActivityInterface);
    NearbyConnections getNearbyConnections();
    void updateConnectionsLog();
    boolean requestNearbyPermissions();
    boolean requestCoarseLocationPermissions();
    boolean requestFineLocationPermissions();
    void setNearbyOpen(boolean nearbyOpen);

    // Midi
    Midi getMidi();
    void registerMidiAction(boolean actionDown, boolean actionUp, boolean actionLong, String note);

    // Database
    SQLiteHelper getSQLiteHelper();
    NonOpenSongSQLiteHelper getNonOpenSongSQLiteHelper();
    CommonSQL getCommonSQL();

    // Web activities
    WebDownload getWebDownload();
    CheckInternet getCheckInternet();
    void isWebConnected(Fragment fragment, int fragId, boolean isConnected);
    void songSelectDownloadPDF(Fragment fragment, int fragId, Uri uri);
    void openDocument(String guideId, String location);
    void installPlayServices();

    // General tools
    CustomAnimation getCustomAnimation();
    Transpose getTranspose();
    AlertChecks getAlertChecks();
    TimeTools getTimeTools();
    DisplayPrevNext getDisplayPrevNext();
    DoVibrate getDoVibrate();
    void hideKeyboard();
    void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song);
    void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song);
    void refreshAll();
    ShowToast getShowToast();
    String getWhattodo();
    void setWhattodo(String whattodo);

    // CCLI
    CCLILog getCCLILog();

    // ABC Notation
    ABCNotation getAbcNotation();

    // Highlighter notes
    DrawNotes getDrawNotes();
    void setDrawNotes(DrawNotes view);
    void setScreenshot(Bitmap bitmap);
    Bitmap getScreenshot();

    // Custom slides
    Bible getBible();
    CustomSlide getCustomSlide();

    // PDF stuff
    void pdfScrollToPage(int pageNumber);
    OCR getOCR();
    MakePDF getMakePDF();
    PDFSong getPDFSong();

    // Song sections and view for display
    void setSectionViews(ArrayList<View> views);
    ArrayList<View> getSectionViews();
    ArrayList<Integer> getSectionWidths();
    ArrayList<Integer> getSectionHeights();
    ArrayList<Integer> getSectionColors();
    void addSectionSize(int position, int width, int height);
    void setSectionColors(ArrayList<Integer> colors);

    // Song sheet titles
    void setSongSheetTitleLayout(LinearLayout linearLayout);
    LinearLayout getSongSheetTitleLayout();
    SongSheetHeaders getSongSheetHeaders();
    ArrayList<Integer> getSongSheetTitleLayoutSize();

    // Song processing
    ConvertChoPro getConvertChoPro();
    ConvertOnSong getConvertOnSong();
    ConvertTextSong getConvertTextSong();
    ProcessSong getProcessSong();

}
