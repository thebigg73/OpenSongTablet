package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;

import java.util.ArrayList;

@SuppressLint({"DefaultLocale", "RtlHardcoded", "InflateParams", "SdCardPath"})
public class FullscreenActivity extends AppCompatActivity {


    public static String profile;

    // This is for trying to automatically open songs via intent
    public static Intent incomingfile;
    public static Uri file_uri;

    // Screencapture variables
    public static Bitmap bmScreen;
    public static boolean abort = false;

    // Custom note/slide variables
    public static String noteorslide = "", customslide_title = "", customslide_content = "",
            customimage_list = "", customimage_loop = "", customimage_time = "",
            customreusabletoload = "", imagetext="";
    public static boolean customreusable = false, isImageSection = false;
    public static int checkscroll_time = 1600, delayswipe_time = 800, crossFadeTime = 8000;

    public static boolean converting = false;
    public static String phrasetosearchfor;

    public static int myWidthAvail, myHeightAvail;

    public static String webpage = "https://www.google.com";

    public static ArrayList<String> exportsetfilenames = new ArrayList<>(), exportsetfilenames_ost = new ArrayList<>();


    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    public static boolean receiveHostFiles;

    public static String emailtext = "";

    public static String whattodo;

    public static boolean pressing_button = false;

    public static String mCurrentPhotoPath;

    public static int pdfPageCurrent = 0, pdfPageCount = 0;
    public static boolean isPDF = false, isImage = false, isSong = false, isSlide = false,
            isScripture = false, isImageSlide = false, isVideo = false;

    // Views and bits on the pages
    public static int mScreenOrientation;
    public static boolean scrollbutton = false, actionbarbutton = false;

    // Set variables
    public static boolean wasscrolling = false;
    public static boolean isManualDragging = false;
    public static String tempswipeSet = "enable", whichDirection = "R2L";

    // Song xml data
    public static ArrayList<String> foundSongSections_heading = new ArrayList<>();


    public static boolean isPresenting, isHDMIConnected = false;
    public static int scalingDensity = 240;

    // Info for the lyrics table
    public static boolean scalingfiguredout = false, botherwithcolumns;
    public static int splitpoint, thirdsplitpoint, twothirdsplitpoint, halfsplit_section,
            thirdsplit_section, twothirdsplit_section;
    public static String myLyrics = "";
    public static String myXML = "", mynewXML = "";
    public static String linkclicked = "";
    public static int numrowstowrite;
    public static String[] myParsedLyrics, myTransposedLyrics;

    public static String mScripture = "", incoming_text = "", scripture_title,
            scripture_verse;
    public static int whichPad = 0;

    public static boolean alreadyloading = false;

    // Flag to indicate that Android Beam is available
    public static boolean mAndroidBeamAvailable  = false;
    public static boolean forcecastupdate;


    // TODO move to StaticVariables if still needed
    public static boolean needtorefreshsongmenu = false, needtoeditsong = false;
    public static String indexlog = "";
    public static String currentFolder = "";
    public static String newFolder = "";
    public static boolean appRunning = false;
    public static int currentSongIndex, previousSongIndex, nextSongIndex, firstSongIndex;
    @SuppressLint("StaticFieldLeak")
    public static SearchViewAdapter sva;
    public static int keyindex;
    public static String autoscrollonoff = "false";
    public static float autoscroll_pixels = 0.0f, newPosFloat = 0.0f;
    public static long time_start, time_passed = 0;
    public static final short minBpm = 40, maxBpm = 199;
    public static short noteValue = 4, beats = 4;
    public static double beatSound = 1200, sound = 1600;
    public static MediaPlayer mPlayer1 = new MediaPlayer(), mPlayer2 = new MediaPlayer();
    public static boolean mPlayer1Paused = false, mPlayer2Paused = false;
    // Salut / connect devices
    @SuppressLint("StaticFieldLeak")
    public static Button hostButton, clientButton;
    @SuppressLint("StaticFieldLeak")
    public static TextView connectionsLog;
    @SuppressLint("StaticFieldLeak")
    public static SalutDataReceiver dataReceiver;
    public static SalutServiceData serviceData;
    public static Salut network;
    public static BluetoothAdapter mBluetoothAdapter;
    public static String hostButtonText="", clientButtonText="", salutLog="",
            mBluetoothName, mySalutXML = "", presenterSendSong="";
    public static boolean firstSendingOfSalut = true, firstSendingOfSalutXML = true,
            firstSendingOfSalutSection = true, firstReceivingOfSalut = true,
            firstReceivingOfSalutXML = true, firstReceivingOfSalutSection = true,
            firstSendingOfSalutAutoscroll = true, firstReceivingOfSalutAutoscroll = true;
    static NfcAdapter mNfcAdapter;
    // Drawing stuff
    public static boolean highlightOn, saveHighlight = false;

    // Chords, capo and transpose stuff
    public static String capokey = null;

    public static boolean orientationchanged = false;
    public static boolean sortAlphabetically = true;

    // Long and short key presses


    // Updated scaled view stuff
    public static int[] viewwidth, viewheight;
    public static int padding = 18;
    // Song sections
    public static LinearLayout[] sectionviews;
    public static boolean[] sectionrendered;
    public static Bitmap[] sectionbitmaps;

}