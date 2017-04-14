package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

public class PresentationService extends CastRemoteDisplayLocalService {

    ExternalDisplay myPresentation;

    private void createPresentation(Display display) {
        dismissPresentation();
        myPresentation = new ExternalDisplay(this, display);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                myPresentation.show();
                FullscreenActivity.isPresenting = true;

            } catch (WindowManager.InvalidDisplayException ex) {
                dismissPresentation();
                FullscreenActivity.isPresenting = false;
            }
        }
    }

    @Override
    public void onCreatePresentation(Display display) {
        FullscreenActivity.isPresenting = true;
        createPresentation(display);
    }

    @Override
    public void onDismissPresentation() {
        FullscreenActivity.isPresenting = false;
        dismissPresentation();
    }

    private void dismissPresentation() {
        if (myPresentation != null) {
            myPresentation.dismiss();
            myPresentation = null;
        }
        FullscreenActivity.isPresenting = false;
    }

    static class ExternalDisplay extends CastPresentation {

        ExternalDisplay(Context c, Display display) {
            super(c, display);
            context = c;
            myscreen = display;
        }
        static Display myscreen;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout projected_LinearLayout;
        @SuppressLint("StaticFieldLeak")
        static RelativeLayout projectedPage_RelativeLayout;
        @SuppressLint("StaticFieldLeak")
        static ImageView projected_ImageView;
        @SuppressLint("StaticFieldLeak")
        static ImageView projected_Logo;
        @SuppressLint("StaticFieldLeak")
        static TextView songinfo_TextView;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout bottom_infobar;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout col1_1;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout col1_2;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout col2_2;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout col1_3;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout col2_3;
        @SuppressLint("StaticFieldLeak")
        static LinearLayout col3_3;

        @SuppressLint("StaticFieldLeak")
        static Context context;
        static int availableScreenWidth;
        static int availableScreenHeight;
        static int density;
        static int padding;
        static int availableWidth_1col;
        static int availableWidth_2col;
        static int availableWidth_3col;
        static int[] projectedviewwidth;
        static int[] projectedviewheight;
        static float[] projectedSectionScaleValue;

        static AsyncTask<Object, Void, String> preparefullprojected_async;
        static AsyncTask<Object, Void, String> preparestageprojected_async;
        static AsyncTask<Object, Void, String> projectedstageview1col_async;
        static AsyncTask<Object, Void, String> projectedPerformanceView1Col_async;
        static AsyncTask<Object, Void, String> projectedPerformanceView2Col_async;
        static AsyncTask<Object, Void, String> projectedPerformanceView3Col_async;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.cast_screen);

            projectedPage_RelativeLayout = (RelativeLayout) findViewById(R.id.projectedPage_RelativeLayout);
            projected_LinearLayout       = (LinearLayout)   findViewById(R.id.projected_LinearLayout);
            projected_ImageView          = (ImageView)      findViewById(R.id.projected_ImageView);
            projected_Logo               = (ImageView)      findViewById(R.id.projected_Logo);
            songinfo_TextView            = (TextView)       findViewById(R.id.songinfo_TextView);
            bottom_infobar               = (LinearLayout)   findViewById(R.id.bottom_infobar);
            col1_1                       = (LinearLayout)   findViewById(R.id.col1_1);
            col1_2                       = (LinearLayout)   findViewById(R.id.col1_2);
            col2_2                       = (LinearLayout)   findViewById(R.id.col2_2);
            col1_3                       = (LinearLayout)   findViewById(R.id.col1_3);
            col2_3                       = (LinearLayout)   findViewById(R.id.col2_3);
            col3_3                       = (LinearLayout)   findViewById(R.id.col3_3);

            // Change margins
            changeMargins();

            // Decide on screen sizes
            getScreenSizes();

            // Prepare the display after 2 secs (a chance for stuff to be measured and show the logo
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    projected_Logo.setVisibility(View.GONE);
                    doUpdate();
                }
            },2000);

        }

        @SuppressLint("NewApi")
        static void getScreenSizes() {
            DisplayMetrics metrics = new DisplayMetrics();
            myscreen.getMetrics(metrics);
            Drawable icon = bottom_infobar.getContext().getDrawable(R.mipmap.ic_round_launcher);
            int bottombarheight = 0;
            if (icon!=null) {
                bottombarheight= icon.getIntrinsicHeight();
            }

            density = metrics.densityDpi;

            padding = 8;

            int screenWidth = metrics.widthPixels;
            int leftpadding = projectedPage_RelativeLayout.getPaddingLeft();
            int rightpadding = projectedPage_RelativeLayout.getPaddingRight();
            availableScreenWidth = screenWidth - leftpadding - rightpadding;

            int screenHeight = metrics.heightPixels;
            int toppadding = projectedPage_RelativeLayout.getPaddingTop();
            int bottompadding = projectedPage_RelativeLayout.getPaddingBottom();
            availableScreenHeight = screenHeight - toppadding - bottompadding - bottombarheight - (padding*2);
            availableWidth_1col = availableScreenWidth - (padding*2);
            availableWidth_2col = (int) ((float)availableScreenWidth / 2.0f) - (padding*3);
            availableWidth_3col = (int) ((float)availableScreenWidth / 3.0f) - (padding*3);
        }

        static void doUpdate() {
            // Check the background page colour
            projectedPage_RelativeLayout.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
            songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);

            //projected_LinearLayout.removeAllViews();

            // Set the title of the song and author (if available)
            setSongTitle();

            // Decide on what we are going to show
            if (FullscreenActivity.whichMode.equals("Stage")) {
                prepareStageProjected();
            } else {
                prepareFullProjected();
            }
        }

        static void setSongTitle() {
            String s = FullscreenActivity.mTitle.toString();
            if (!FullscreenActivity.mAuthor.equals("")) {
                s = s + "\n" + FullscreenActivity.mAuthor;
            }
            songinfo_TextView.setText(s);
        }

        static void changeMargins() {
            projectedPage_RelativeLayout.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
            songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
            projectedPage_RelativeLayout.setPadding(FullscreenActivity.xmargin_presentation,
                    FullscreenActivity.ymargin_presentation,FullscreenActivity.xmargin_presentation,
                    FullscreenActivity.ymargin_presentation);
        }

        static void prepareStageProjected() {
            preparestageprojected_async = new PrepareStageProjected();
            try {
                FullscreenActivity.scalingfiguredout = false;
                preparestageprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static class PrepareStageProjected extends AsyncTask<Object, Void, String> {
            LinearLayout test1_1 = ProcessSong.createLinearLayout(context);

            @Override
            protected void onPreExecute() {
                // Remove all views from the test pane
                col1_1.removeAllViews();
            }

            @Override
            protected String doInBackground(Object... objects) {
                projectedSectionScaleValue = new float[1];
                projectedviewwidth = new int[1];
                projectedviewheight = new int[1];
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                test1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, 12.0f);
                col1_1.addView(test1_1);

                // Now premeasure the view
                test1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                projectedviewwidth[0] = test1_1.getMeasuredWidth();
                projectedviewheight[0] = test1_1.getMeasuredHeight();

                // Now display the song!
                //FullscreenActivity.scalingfiguredout = true;
                projectedStageView1Col();
            }
        }


        static void projectedStageView1Col() {
            projectedstageview1col_async = new ProjectedStageView1Col();
            try {
                projectedstageview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        static class ProjectedStageView1Col extends AsyncTask<Object, Void, String> {
            LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
            LinearLayout box1_1    = ProcessSong.prepareProjectedBoxView(context,0,padding);
            float scale;

            @Override
            protected void onPreExecute() {
                projected_LinearLayout.removeAllViews();
                float max_width_scale  = (float) availableWidth_1col   / (float) projectedviewwidth[0];
                float max_height_scale = (float) availableScreenHeight / (float) projectedviewheight[0];
                if (max_height_scale>max_width_scale) {
                    scale = max_width_scale;
                } else {
                    scale = max_height_scale;
                }

                float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;
                if (scale>maxscale) {
                    scale = maxscale;
                }

                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0,0,0,0);
            }

            @Override
            protected String doInBackground(Object... params) {
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                lyrics1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, ProcessSong.getProjectedFontSize(scale));
                LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp1_1.setMargins(0,0,0,0);
                lyrics1_1.setLayoutParams(llp1_1);
                //lyrics1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[FullscreenActivity.currentSection]));
                box1_1.addView(lyrics1_1);

                // Now add the display
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth,availableScreenHeight+padding);
                llp.setMargins(0,0,0,0);
                projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                box1_1.setLayoutParams(llp);
                projected_LinearLayout.addView(box1_1);
            }
        }

        static void prepareFullProjected() {
            preparefullprojected_async = new PrepareFullProjected();
            try {
                FullscreenActivity.scalingfiguredout = false;
                preparefullprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static class PrepareFullProjected extends AsyncTask<Object, Void, String> {
            LinearLayout test1_1 = ProcessSong.createLinearLayout(context);
            LinearLayout test1_2 = ProcessSong.createLinearLayout(context);
            LinearLayout test2_2 = ProcessSong.createLinearLayout(context);
            LinearLayout test1_3 = ProcessSong.createLinearLayout(context);
            LinearLayout test2_3 = ProcessSong.createLinearLayout(context);
            LinearLayout test3_3 = ProcessSong.createLinearLayout(context);

            @Override
            protected void onPreExecute() {
                // Remove all views from the test panes
                col1_1.removeAllViews();
                col1_2.removeAllViews();
                col2_2.removeAllViews();
                col1_3.removeAllViews();
                col2_3.removeAllViews();
                col3_3.removeAllViews();
            }

            @Override
            protected String doInBackground(Object... objects) {
                projectedSectionScaleValue = new float[6];
                projectedviewwidth = new int[6];
                projectedviewheight = new int[6];
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                // Go through each section
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                    test1_1 = ProcessSong.projectedSectionView(context, x, 12.0f);
                    col1_1.addView(test1_1);

                    if (x < FullscreenActivity.halfsplit_section) {
                        test1_2 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col1_2.addView(test1_2);
                    } else {
                        test2_2 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col2_2.addView(test2_2);
                    }

                    if (x < FullscreenActivity.thirdsplit_section) {
                        test1_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col1_3.addView(test1_3);
                    } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                        test2_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col2_3.addView(test2_3);
                    } else {
                        test3_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col3_3.addView(test3_3);
                    }
                }

                // Now premeasure the views
                col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                col1_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                col2_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                col1_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                col2_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                col3_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                projectedviewwidth[0] = col1_1.getMeasuredWidth();
                projectedviewheight[0] = col1_1.getMeasuredHeight();
                projectedviewwidth[1] = col1_2.getMeasuredWidth();
                projectedviewheight[1] = col1_2.getMeasuredHeight();
                projectedviewwidth[2] = col2_2.getMeasuredWidth();
                projectedviewheight[2] = col2_2.getMeasuredHeight();
                projectedviewwidth[3] = col1_3.getMeasuredWidth();
                projectedviewheight[3] = col1_3.getMeasuredHeight();
                projectedviewwidth[4] = col2_3.getMeasuredWidth();
                projectedviewheight[4] = col2_3.getMeasuredHeight();
                projectedviewwidth[5] = col3_3.getMeasuredWidth();
                projectedviewheight[5] = col3_3.getMeasuredHeight();

                // Now display the song!
                //FullscreenActivity.scalingfiguredout = true;
                displayFullSong();
            }
        }

        static void displayFullSong() {
            projected_LinearLayout.removeAllViews();

            // We know the widths and heights of all of the view (1,2 and 3 columns).
            // Decide which is best by looking at the scaling

            int colstouse = 1;
            // We know the size of each section, so we just need to know which one to display
            int widthofsection1_1  = projectedviewwidth[0];
            int widthofsection1_2  = projectedviewwidth[1];
            int widthofsection2_2  = projectedviewwidth[2];
            int widthofsection1_3  = projectedviewwidth[3];
            int widthofsection2_3  = projectedviewwidth[4];
            int widthofsection3_3  = projectedviewwidth[5];
            int heightofsection1_1 = projectedviewheight[0];
            int heightofsection1_2 = projectedviewheight[1];
            int heightofsection2_2 = projectedviewheight[2];
            int heightofsection1_3 = projectedviewheight[3];
            int heightofsection2_3 = projectedviewheight[4];
            int heightofsection3_3 = projectedviewheight[5];

            float maxwidth_scale1_1  = ((float) availableWidth_1col)/ (float) widthofsection1_1;
            float maxwidth_scale1_2  = ((float) availableWidth_2col)/ (float) widthofsection1_2;
            float maxwidth_scale2_2  = ((float) availableWidth_2col)/ (float) widthofsection2_2;
            float maxwidth_scale1_3  = ((float) availableWidth_3col)/ (float) widthofsection1_3;
            float maxwidth_scale2_3  = ((float) availableWidth_3col)/ (float) widthofsection2_3;
            float maxwidth_scale3_3  = ((float) availableWidth_3col)/ (float) widthofsection3_3;
            float maxheight_scale1_1 = ((float) availableScreenHeight)/ (float) heightofsection1_1;
            float maxheight_scale1_2 = ((float) availableScreenHeight)/ (float) heightofsection1_2;
            float maxheight_scale2_2 = ((float) availableScreenHeight)/ (float) heightofsection2_2;
            float maxheight_scale1_3 = ((float) availableScreenHeight)/ (float) heightofsection1_3;
            float maxheight_scale2_3 = ((float) availableScreenHeight)/ (float) heightofsection2_3;
            float maxheight_scale3_3 = ((float) availableScreenHeight)/ (float) heightofsection3_3;

            if (maxheight_scale1_1<maxwidth_scale1_1) {
                maxwidth_scale1_1 = maxheight_scale1_1;
            }
            if (maxheight_scale1_2<maxwidth_scale1_2) {
                maxwidth_scale1_2 = maxheight_scale1_2;
            }
            if (maxheight_scale2_2<maxwidth_scale2_2) {
                maxwidth_scale2_2 = maxheight_scale2_2;
            }
            if (maxheight_scale1_3<maxwidth_scale1_3) {
                maxwidth_scale1_3 = maxheight_scale1_3;
            }
            if (maxheight_scale2_3<maxwidth_scale2_3) {
                maxwidth_scale2_3 = maxheight_scale2_3;
            }
            if (maxheight_scale3_3<maxwidth_scale3_3) {
                maxwidth_scale3_3 = maxheight_scale3_3;
            }

            // Decide on the best scaling to use
            float myfullscale = maxwidth_scale1_1;

            if (maxwidth_scale1_2>myfullscale && maxwidth_scale2_2>myfullscale) {
                colstouse = 2;
                if (maxwidth_scale1_2>maxwidth_scale2_2) {
                    myfullscale = maxwidth_scale2_2;
                } else {
                    myfullscale = maxwidth_scale1_2;
                }
            }

            if (maxwidth_scale1_3>myfullscale && maxwidth_scale2_3>myfullscale && maxwidth_scale3_3>myfullscale) {
                colstouse = 3;
            }

            // Now we know how many columns we should use, let's do it!
            float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;

            switch (colstouse) {
                case 1:
                    if (maxwidth_scale1_1>maxscale) {
                        maxwidth_scale1_1 = maxscale;
                    }
                    projectedPerformanceView1col(maxwidth_scale1_1);
                    break;

                case 2:
                    if (maxwidth_scale1_2>maxscale) {
                        maxwidth_scale1_2 = maxscale;
                    }
                    if (maxwidth_scale2_2>maxscale) {
                        maxwidth_scale2_2 = maxscale;
                    }
                    projectedPerformanceView2col(maxwidth_scale1_2, maxwidth_scale2_2);
                    break;

                case 3:
                    if (maxwidth_scale1_3>maxscale) {
                        maxwidth_scale1_3 = maxscale;
                    }
                    if (maxwidth_scale2_3>maxscale) {
                        maxwidth_scale2_3 = maxscale;
                    }
                    if (maxwidth_scale3_3>maxscale) {
                        maxwidth_scale3_3 = maxscale;
                    }
                    projectedPerformanceView3col(maxwidth_scale1_3, maxwidth_scale2_3, maxwidth_scale3_3);
                    break;
            }
        }

        static void projectedPerformanceView1col(float scale1_1) {
            projectedPerformanceView1Col_async = new ProjectedPerformanceView1Col(scale1_1);
            try {
                projectedPerformanceView1Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static class ProjectedPerformanceView1Col extends AsyncTask<Object, Void, String> {
            LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
            LinearLayout box1_1    = ProcessSong.prepareProjectedBoxView(context,0,padding);
            float scale1_1;
            float fontsize1_1;

            ProjectedPerformanceView1Col(float s1_1) {
                scale1_1 = s1_1;
                fontsize1_1 = ProcessSong.getProjectedFontSize(scale1_1);
            }

            @Override
            protected void onPreExecute() {
                // Remove all views from the projector
                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0,0,0,0);
            }

            @Override
            protected String doInBackground(Object... objects) {
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                // Go through each section
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                    lyrics1_1 = ProcessSong.projectedSectionView(context, x, fontsize1_1);
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_1.setMargins(0,0,0,0);
                    lyrics1_1.setLayoutParams(llp1_1);
                    lyrics1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                    box1_1.addView(lyrics1_1);
                }

                // Now add the display
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth,availableScreenHeight+padding);
                llp.setMargins(0,0,0,0);
                projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                box1_1.setLayoutParams(llp);
                projected_LinearLayout.addView(box1_1);
            }
        }

        static void projectedPerformanceView2col(float scale1_2,float scale2_2) {
            projectedPerformanceView2Col_async = new ProjectedPerformanceView2Col(scale1_2,scale2_2);
            try {
                projectedPerformanceView2Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static class ProjectedPerformanceView2Col extends AsyncTask<Object, Void, String> {
            float scale1_2;
            float scale2_2;
            float fontsize1_2;
            float fontsize2_2;
            LinearLayout lyrics1_2 = ProcessSong.createLinearLayout(context);
            LinearLayout lyrics2_2 = ProcessSong.createLinearLayout(context);
            LinearLayout box1_2    = ProcessSong.prepareProjectedBoxView(context,0,padding);
            LinearLayout box2_2    = ProcessSong.prepareProjectedBoxView(context,0,padding);

            ProjectedPerformanceView2Col(float s1_2, float s2_2) {
                scale1_2 = s1_2;
                scale2_2 = s2_2;
                fontsize1_2 = ProcessSong.getProjectedFontSize(scale1_2);
                fontsize2_2 = ProcessSong.getProjectedFontSize(scale2_2);
            }

            @Override
            protected void onPreExecute() {
                // Remove all views from the projector
                projected_LinearLayout.removeAllViews();
                lyrics1_2.setPadding(0,0,0,0);
                lyrics2_2.setPadding(0,0,0,0);
            }

            @Override
            protected String doInBackground(Object... params) {
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                // Add the song sections...
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                    if (x < FullscreenActivity.halfsplit_section) {
                        lyrics1_2 = ProcessSong.projectedSectionView(context, x, fontsize1_2);
                        LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams(availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp1_2.setMargins(0,0,0,0);
                        lyrics1_2.setLayoutParams(llp1_2);
                        lyrics1_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box1_2.addView(lyrics1_2);
                    } else {
                        lyrics2_2 = ProcessSong.projectedSectionView(context, x, fontsize2_2);
                        LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams(availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp2_2.setMargins(0,0,0,0);
                        lyrics2_2.setLayoutParams(llp2_2);
                        lyrics2_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box2_2.addView(lyrics2_2);
                    }
                }
                // Now add the display
                LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(availableWidth_2col+(padding*2),availableScreenHeight+padding);
                LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(availableWidth_2col+(padding*2),availableScreenHeight+padding);
                llp1.setMargins(0,0,padding,0);
                llp2.setMargins(0,0,0,0);
                projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                box1_2.setLayoutParams(llp1);
                box2_2.setLayoutParams(llp2);
                projected_LinearLayout.addView(box1_2);
                projected_LinearLayout.addView(box2_2);
            }
        }

        static void projectedPerformanceView3col(float scale1_3,float scale2_3,float scale3_3) {
            projectedPerformanceView3Col_async = new ProjectedPerformanceView3Col(scale1_3,scale2_3,scale3_3);
            try {
                projectedPerformanceView3Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static class ProjectedPerformanceView3Col extends AsyncTask<Object, Void, String> {
            float scale1_3;
            float scale2_3;
            float scale3_3;
            float fontsize1_3;
            float fontsize2_3;
            float fontsize3_3;
            LinearLayout lyrics1_3 = ProcessSong.createLinearLayout(context);
            LinearLayout lyrics2_3 = ProcessSong.createLinearLayout(context);
            LinearLayout lyrics3_3 = ProcessSong.createLinearLayout(context);
            LinearLayout box1_3    = ProcessSong.prepareProjectedBoxView(context,0,padding);
            LinearLayout box2_3    = ProcessSong.prepareProjectedBoxView(context,0,padding);
            LinearLayout box3_3    = ProcessSong.prepareProjectedBoxView(context,0,padding);

            ProjectedPerformanceView3Col(float s1_3, float s2_3, float s3_3) {
                scale1_3 = s1_3;
                scale2_3 = s2_3;
                scale3_3 = s3_3;
                fontsize1_3 = ProcessSong.getProjectedFontSize(scale1_3);
                fontsize2_3 = ProcessSong.getProjectedFontSize(scale2_3);
                fontsize3_3 = ProcessSong.getProjectedFontSize(scale3_3);
            }

            @Override
            protected void onPreExecute() {
                // Remove all views from the projector
                projected_LinearLayout.removeAllViews();
                lyrics1_3.setPadding(0,0,0,0);
                lyrics2_3.setPadding(0,0,0,0);
                lyrics3_3.setPadding(0,0,0,0);
            }

            @Override
            protected String doInBackground(Object... params) {
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                // Add the song sections...
                for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                    if (x < FullscreenActivity.thirdsplit_section) {
                        lyrics1_3 = ProcessSong.projectedSectionView(context, x, fontsize1_3);
                        LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp1_3.setMargins(0,0,0,0);
                        lyrics1_3.setLayoutParams(llp1_3);
                        lyrics1_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box1_3.addView(lyrics1_3);
                    } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                        lyrics2_3 = ProcessSong.projectedSectionView(context, x, fontsize2_3);
                        LinearLayout.LayoutParams llp2_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp2_3.setMargins(0,0,0,0);
                        lyrics2_3.setLayoutParams(llp2_3);
                        lyrics2_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box2_3.addView(lyrics2_3);
                    } else {
                        lyrics3_3 = ProcessSong.projectedSectionView(context, x, fontsize3_3);
                        LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp3_3.setMargins(0,0,0,0);
                        lyrics3_3.setLayoutParams(llp3_3);
                        lyrics3_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box3_3.addView(lyrics3_3);
                    }
                }

                // Now add the display
                LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(availableWidth_3col+(padding*2),availableScreenHeight+padding);
                LinearLayout.LayoutParams llp3 = new LinearLayout.LayoutParams(availableWidth_3col+(padding*2),availableScreenHeight+padding);
                llp1.setMargins(0,0,padding,0);
                llp3.setMargins(0,0,0,0);
                projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                box1_3.setLayoutParams(llp1);
                box2_3.setLayoutParams(llp1);
                box3_3.setLayoutParams(llp3);
                projected_LinearLayout.addView(box1_3);
                projected_LinearLayout.addView(box2_3);
                projected_LinearLayout.addView(box3_3);
            }
        }

    }
}
