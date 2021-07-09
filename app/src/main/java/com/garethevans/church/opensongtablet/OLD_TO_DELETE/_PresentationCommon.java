//package com.garethevans.church.opensongtablet;
//
//// This contains all of the scripts for the PresentationService and PresentationServiceHDMI
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Handler;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.core.content.res.ResourcesCompat;
//import androidx.core.graphics.ColorUtils;
//
//import com.bumptech.glide.request.RequestOptions;
//import com.garethevans.church.opensongtablet.customviews.GlideApp;
//import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
//import com.garethevans.church.opensongtablet.preferences.StaticVariables;
//import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
//
//import java.io.InputStream;
//
//class _PresentationCommon {
//    // IV - Lyric display is delayed for a change of infobar - not at other times
//    long infoBarChangeDelay;
//    long infoBarUntilTime;
//    String infoBarAlertState = PresenterMode.alert_on;
//    // IV - Support for 'last change only' fade in of content
//    long lyricAfterTime;
//    long lyricDelay;
//    long panicAfterTime;
//    long panicDelay;
//    // IV - doUpdate can run frequently - this supports better transitions
//    boolean doUpdateActive = false;
//    boolean animateOutActive = false;
//    boolean showLogoActive = false;
//    boolean blankActive = false;
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//}