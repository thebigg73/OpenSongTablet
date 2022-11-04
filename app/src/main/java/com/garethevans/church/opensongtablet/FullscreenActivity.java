package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

@SuppressLint({"DefaultLocale", "RtlHardcoded", "InflateParams", "SdCardPath"})
public class FullscreenActivity extends AppCompatActivity {

    public static Uri file_uri;

    // Screencapture variables
    public static Bitmap bmScreen;

    // Custom note/slide variables
    public static String noteorslide = "";
    public static String customslide_title = "";
    public static String customslide_content = "";
    public static String customimage_list = "";
    public static String customimage_loop = "";
    public static String customimage_time = "";
    public static String customreusabletoload = "";
    public static final String imagetext="";
    public static boolean customreusable = false;
    public static final int checkscroll_time = 1600;
    public static final int delayswipe_time = 800;

    public static String phrasetosearchfor;

    public static int myWidthAvail;

    public static final String webpage = "https://www.google.com";

    public static ArrayList<String> exportsetfilenames = new ArrayList<>(), exportsetfilenames_ost = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    public static String emailtext = "";

    public static String whattodo;

    public static String mCurrentPhotoPath;

    public static int pdfPageCurrent = 0, pdfPageCount = 0;
    public static boolean isPDF = false;
    public static boolean isImage = false;
    public static boolean isSong = false;
    public static boolean isSlide = false;
    public static boolean isScripture = false;
    public static boolean isImageSlide = false;
    public static final boolean isVideo = false;

    // Views and bits on the pages
    public static int mScreenOrientation;
    public static boolean scrollbutton = false;

    // Set variables
    public static boolean wasscrolling = false;
    public static boolean isManualDragging = false;
    public static String tempswipeSet = "enable", whichDirection = "R2L";

    // Song xml data
    public static ArrayList<String> foundSongSections_heading = new ArrayList<>();


    public static boolean isPresenting, isHDMIConnected = false;

    // Info for the lyrics table
    public static boolean scalingfiguredout = false;
    public static int splitpoint, thirdsplitpoint, twothirdsplitpoint, halfsplit_section,
            thirdsplit_section, twothirdsplit_section;
    public static String myLyrics = "";
    public static String myXML = "", mynewXML = "";
    public static String linkclicked = "";
    public static int numrowstowrite;
    public static String[] myParsedLyrics, myTransposedLyrics;

    public static String mScripture = "";
    public static final String incoming_text = "";
    public static String scripture_title;
    public static String scripture_verse;
    public static int whichPad = 0;

    public static boolean alreadyloading = false;
    // IV - Some mode oncreate actions are only needed once
    public static boolean doonetimeactions = true;
    // IV - Persist knowledge of a current hdmi session
    public static PresentationServiceHDMI hdmi;

    // Flag to indicate that Android Beam is available
    public static final boolean mAndroidBeamAvailable  = false;


    // TODO move to StaticVariables if still needed
    public static boolean needtorefreshsongmenu = true, needtoeditsong = false;
    public static final String indexlog = "";
    public static String currentFolder = "";
    public static String newFolder = "";
    public static boolean appRunning = false;
    public static int currentSongIndex;
    public static int previousSongIndex;
    public static int nextSongIndex;
    @SuppressLint("StaticFieldLeak")
    public static int keyindex;
    public static float autoscroll_pixels = 0.0f, newPosFloat = 0.0f;
    public static long time_start, time_passed = 0;
    public static final short minBpm = 40, maxBpm = 199;
    public static short noteValue = 4, beats = 4;
    public static final double beatSound = 1200;
    public static final double sound = 1600;
    public static final MediaPlayer mPlayer1 = new MediaPlayer();
    public static final MediaPlayer mPlayer2 = new MediaPlayer();
    public static boolean mPlayer1Paused = false, mPlayer2Paused = false;
    public static BluetoothAdapter mBluetoothAdapter;
    public static String mBluetoothName;
    static NfcAdapter mNfcAdapter;
    // Drawing stuff
    public static boolean highlightOn, saveHighlight = false;

    // Chords, capo and transpose stuff
    public static String capokey = null;

    public static boolean orientationchanged = false;
    public static boolean sortAlphabetically = true;


    // Updated scaled view stuff
    public static int[] viewwidth, viewheight;
    public static int padding = 18;
    // Song sections
    public static LinearLayout[] sectionviews;

}