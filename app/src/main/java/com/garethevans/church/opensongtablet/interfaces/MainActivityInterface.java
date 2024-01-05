package com.garethevans.church.opensongtablet.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.garethevans.church.opensongtablet.abcnotation.ABCNotation;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.appdata.AlertChecks;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.appdata.MyFonts;
import com.garethevans.church.opensongtablet.appdata.VersionNumber;
import com.garethevans.church.opensongtablet.autoscroll.Autoscroll;
import com.garethevans.church.opensongtablet.beatbuddy.BeatBuddy;
import com.garethevans.church.opensongtablet.bible.Bible;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.chords.ChordDirectory;
import com.garethevans.church.opensongtablet.chords.ChordDisplayProcessing;
import com.garethevans.church.opensongtablet.chords.Transpose;
import com.garethevans.church.opensongtablet.controls.CommonControls;
import com.garethevans.church.opensongtablet.controls.Gestures;
import com.garethevans.church.opensongtablet.controls.HotZones;
import com.garethevans.church.opensongtablet.controls.PageButtons;
import com.garethevans.church.opensongtablet.controls.PedalActions;
import com.garethevans.church.opensongtablet.controls.Swipes;
import com.garethevans.church.opensongtablet.customslides.CustomSlide;
import com.garethevans.church.opensongtablet.customviews.DrawNotes;
import com.garethevans.church.opensongtablet.customviews.MyToolbar;
import com.garethevans.church.opensongtablet.drummer.Drummer;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.export.PrepareFormats;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.SaveSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.importsongs.ImportOnlineFragment;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.pads.Pad;
import com.garethevans.church.opensongtablet.pdf.MakePDF;
import com.garethevans.church.opensongtablet.pdf.OCR;
import com.garethevans.church.opensongtablet.performance.DisplayPrevNext;
import com.garethevans.church.opensongtablet.performance.PerformanceGestures;
import com.garethevans.church.opensongtablet.preferences.AppPermissions;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.ProfileActions;
import com.garethevans.church.opensongtablet.presenter.PresenterSettings;
import com.garethevans.church.opensongtablet.screensetup.BatteryStatus;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.screensetup.WindowFlags;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.songmenu.SongMenuFragment;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongSheetHeaders;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.garethevans.church.opensongtablet.utilities.TimeTools;
import com.garethevans.church.opensongtablet.webserver.WebServer;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

public interface MainActivityInterface {

    // Initialising the activity and settings
    void initialiseActivity();
    void updateSizes(int width, int height);
    int[] getDisplayMetrics();
    float getDisplayDensity();
    Handler getMainHandler();
    ThreadPoolExecutor getThreadPoolExecutor();
    Locale getLocale();
    VersionNumber getVersionNumber();
    String getMode();
    void setMode(String whichMode);
    void setFirstRun(boolean firstRun);
    boolean getFirstRun();
    int getOrientation();
    WebServer getWebServer();

    // Preferences and settings
    Preferences getPreferences();
    PresenterSettings getPresenterSettings();
    ProfileActions getProfileActions();
    MyFonts getMyFonts();
    ThemeColors getMyThemeColors();
    AppPermissions getAppPermissions();

    // Song stuff
    Song getSong();
    Song getIndexingSong();
    Song getTempSong();
    LoadSong getLoadSong();
    SaveSong getSaveSong();
    void setSong(Song song);
    void setTempSong(Song tempSong);
    String getMainfoldername();


    // Capo
    void dealWithCapo();

    // Metronome
    Metronome getMetronome();
    void midiStartStopReceived(boolean start);

    // Pads
    Pad getPad();
    boolean playPad();

    // Autoscroll
    Autoscroll getAutoscroll();
    void toggleAutoscroll();

    // Set stuff
    CurrentSet getCurrentSet();
    SetActions getSetActions();
    void updateSetList();
    void checkSetMenuItemHighlighted(int setPosition);
    void toggleInlineSet();
    void notifyInlineSetInserted();
    void notifyInlineSetInserted(int position);
    void notifyInlineSetMove(int from, int to);
    void notifyInlineSetRemoved(int postion);
    void notifyInlineSetChanged(int position);
    void notifyInlineSetRangeChanged(int from, int count);
    void notifyToClearInlineSet(int from, int count);
    void notifyToInsertAllInlineSet();
    void updateInlineSetVisibility();
    void notifyInlineSetHighlight();
    void notifySetFragment(String what, int position);
    void notifyInlineSetScrollToItem();


    // Menus
    void lockDrawer(boolean lock);
    void closeDrawer(boolean close);
    void moveToSongInSongMenu();
    int getPositionOfSongInMenu();
    void updateSongList();
    void quickSongMenuBuild();
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
    void scrollOpenMenu(boolean scrollDown);
    boolean getMenuOpen();
    boolean getSettingsOpen();
    void setSettingsOpen(boolean settingsOpen);
    void updateCheckForThisSong(Song thisSong);
    SongMenuFragment getSongMenuFragment();

    // Action bar
    MyToolbar getToolbar();
    BatteryStatus getBatteryStatus();
    void hideActionBar();
    void showActionBar();
    void removeActionBar(boolean remove);
    ImageView disableActionBarStuff(boolean disable);
    void updateToolbar(String what);
    void updateToolbarHelp(String webAddress);
    void updateActionBarSettings(String prefName, float floatval, boolean isvisible);
    boolean needActionBar();
    void allowNavigationUp(boolean allow);

    // Page button(s)
    PageButtons getPageButtons();
    void hideActionButton(boolean hide);
    void updatePageButtonLayout();
    void miniPageButton(boolean mini);
    void expandActionButton();

    // Controls
    CommonControls getCommonControls();
    PedalActions getPedalActions();
    Gestures getGestures();
    PerformanceGestures getPerformanceGestures();
    Swipes getSwipes();
    void enableSwipe(String which, boolean canSwipe);
    HotZones getHotZones();

    // Navigation
    void navHome();
    void navigateToFragment(String deepLink, int id);
    void popTheBackStack(int id, boolean inclusive);
    void registerFragment(Fragment frag, String what);
    void updateFragment(String fragName, Fragment callingFragment, ArrayList<String> arguments);
    FragmentManager getMyFragmentManager();
    void dealWithIntent(int fragmentId);
    void onBackPressed();

    // Showcase
    ShowCase getShowCase();
    void showTutorial(String what,ArrayList<View> viewsToHighlight);

    // File work
    StorageAccess getStorageAccess();
    void doSongLoad(String folder, String filename, boolean closeDrawer);
    void refreshSong();
    void loadSongFromSet(int position);
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

    // Midi
    Midi getMidi();
    void registerMidiAction(boolean actionDown, boolean actionUp, boolean actionLong, String note);
    Drummer getDrummer();
    BeatBuddy getBeatBuddy();

    // Database
    SQLiteHelper getSQLiteHelper();
    NonOpenSongSQLiteHelper getNonOpenSongSQLiteHelper();
    CommonSQL getCommonSQL();

    // Web activities
    WebDownload getWebDownload();
    CheckInternet getCheckInternet();
    void isWebConnected(Fragment fragment, int fragId, boolean isConnected);
    void songSelectDownload(Fragment fragment, int fragId, Uri uri, String filename);
    void openDocument(String location);
    void chordinatorResult(ImportOnlineFragment importOnlineFragment, String songText);

    // General tools
    CustomAnimation getCustomAnimation();
    Transpose getTranspose();
    AlertChecks getAlertChecks();
    TimeTools getTimeTools();
    DisplayPrevNext getDisplayPrevNext();
    void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song);
    void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song);
    ShowToast getShowToast();
    String getWhattodo();
    void setWhattodo(String whattodo);
    WindowFlags getWindowFlags();
    void setAlreadyBackPressed(boolean alreadyBackPressed);
    int[] getAvailableSizes();
    void setAvailableSizes(int availableWidth, int availableHeight);

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

    // Song sections and view for display
    void setSectionViews(ArrayList<View> views);
    ArrayList<View> getSectionViews();
    ArrayList<Integer> getSectionWidths();
    ArrayList<Integer> getSectionHeights();
    ArrayList<Integer> getSectionColors();
    void addSectionSize(int position, int width, int height);
    void setSectionColors(ArrayList<Integer> colors);
    void toggleScale();
    void updateMargins();
    int[] getViewMargins();
    boolean getIsSecondaryDisplaying();

    // Song sheet titles
    void setSongSheetTitleLayout(LinearLayout linearLayout);
    LinearLayout getSongSheetTitleLayout();
    SongSheetHeaders getSongSheetHeaders();

    // Song processing
    ConvertChoPro getConvertChoPro();
    ConvertOnSong getConvertOnSong();
    ConvertTextSong getConvertTextSong();
    ProcessSong getProcessSong();
    ChordDisplayProcessing getChordDisplayProcessing();
    ChordDirectory getChordDirectory();
    void updateOnScreenInfo(String what);

}
