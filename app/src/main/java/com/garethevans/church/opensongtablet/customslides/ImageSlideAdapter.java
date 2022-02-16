package com.garethevans.church.opensongtablet.customslides;

// This allows populating a recycler view with thumbnails of custom image slides
// We tap into a bit of the pdf logic in Song

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class ImageSlideAdapter  extends RecyclerView.Adapter<ImageSlideViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final String TAG = "ImageSlideAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<ImageSlideItemInfo> slideInfos;
    private ArrayList<Float> sectionHeights;
    private final int viewWidth, viewHeight;
    private String slideFolder, slideFilename;
    private Uri slideUri;
    private int totalPages;
    private int totalHeight;
    private final String scaleType;
    private boolean manualDrag = false;
    private final float density;
    private int currentSection = 0;
    private final Context c;

    public ImageSlideAdapter(Context c, MainActivityInterface mainActivityInterface, DisplayInterface displayInterface, int viewWidth, int viewHeight) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        scaleType = mainActivityInterface.getPreferences().getMyPreferenceString(c,"songAutoScale","W");
        density = c.getResources().getDisplayMetrics().density;
        setSongInfo();
        sectionHeights = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageSlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_pdf_page, parent, false);
        return new ImageSlideViewHolder(mainActivityInterface,itemView);
    }

    private void setSongInfo() {
        totalHeight = 0;
        float floatHeight = 0;
        slideFolder = mainActivityInterface.getSong().getFolder();
        slideFilename = mainActivityInterface.getSong().getFilename();

        // The images are references in user3
        Log.d(TAG,"user3: "+mainActivityInterface.getSong().getUser3());
        String[] images = mainActivityInterface.getSong().getUser3().trim().split("\n");
        totalPages = images.length;
        mainActivityInterface.getSong().setPdfPageCount(totalPages);
        if (totalPages==0) {
            mainActivityInterface.getSong().setPdfPageCurrent(0);
        } else {
            mainActivityInterface.getSong().setPdfPageCurrent(1);
        }
        mainActivityInterface.getSong().setShowstartofpdf(true);

        slideInfos = new ArrayList<>();
        sectionHeights = new ArrayList<>();

        for (int x=0; x<totalPages; x++) {
            ImageSlideItemInfo slideInfo = new ImageSlideItemInfo();
            slideInfo.pageNum = x;
            slideInfo.pageNumText = (x+1) + "/" + totalPages;

            // Get the image sizes from the files
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,mainActivityInterface,images[x]);

            slideInfo.uri = uri;

            int width;
            int height;

            try {
                ParcelFileDescriptor fd = c.getContentResolver().openFileDescriptor(uri, "r");
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                width = options.outWidth;
                height = options.outHeight;
            } catch (Exception e) {
                e.printStackTrace();
                width = 200;
                height = 200;
            }
            float scaleFactor;
            if (scaleType.equals("Y") && width > 0 && height > 0) {
                scaleFactor = Math.min((float) viewWidth / (float) width, (float) viewHeight / (float) height);
            } else if (scaleType.equals("W")) {
                scaleFactor = (float) viewWidth / (float) width;
            } else {
                scaleFactor = 1f;
            }
            Log.d(TAG, "width=" + width + "  height=" + height + "  scaleFactor=" + scaleFactor);

            slideInfo.width = (int) (width * scaleFactor);
            slideInfo.height = (int) (height * scaleFactor);

            // Add up the heights plus the 4dp padding at the bottom of each view
            float thisHeight = height*scaleFactor;
            thisHeight += 4f*density;
            sectionHeights.add(thisHeight);
            floatHeight += thisHeight;

            slideInfos.add(slideInfo);
        }

        // Round total height
        totalHeight = (int) floatHeight;

        notifyItemRangeChanged(0, totalPages);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSlideViewHolder holder, int position) {
        int pageNum = slideInfos.get(position).pageNum;
        int width = slideInfos.get(position).width;
        int height = slideInfos.get(position).height;
        CardView cardView = (CardView)holder.v;
        float alpha = 1.0f;
        if (mainActivityInterface.getMode().equals("Stage")) {
            if (position == currentSection) {
                alpha = 1.0f;
            } else {
                alpha = 0.4f;
            }
        }
        cardView.setAlpha(alpha);

        Uri uri = slideInfos.get(position).uri;
        String pageNumText = slideInfos.get(position).pageNumText;
        holder.imageSlideNumText.setText(pageNumText);

        Bitmap imageSlideBitmap = mainActivityInterface.getProcessSong().getBitmapFromUri(c,
                mainActivityInterface,uri,width,height);

        Glide.with(c).load(imageSlideBitmap).override(width,height).into(holder.imageSlideImage);
        holder.imageSlideImage.setOnClickListener(view -> {
            Log.d(TAG,"clicked on "+pageNum);
            // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
            onTouchAction();
            sectionSelected(pageNum);
            // Send and update notification to Performance Fragment via the MainActivity
            displayInterface.performanceShowSection(pageNum);
        });
        holder.imageSlideImage.setOnLongClickListener(view -> {
            // Do nothing other than consume the long press
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return totalPages;
    }

    public int getHeight() {
        Log.d(TAG,"totalHeight="+totalHeight);
        return totalHeight;
    }

    private void onTouchAction() {
        mainActivityInterface.getDisplayPrevNext().showAndHide();
        mainActivityInterface.updateOnScreenInfo("showhide");
        mainActivityInterface.showHideActionBar();
    }

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        notifyItemChanged(currentSection);

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        // Now update the newly selected position
        if (position>-1 && position<slideInfos.size()) {
            mainActivityInterface.getSong().setCurrentSection(position);
            currentSection = position;
            notifyItemChanged(position);
        }

        // Send and update notification to Performance Fragment via the MainActivity
        displayInterface.performanceShowSection(position);
    }

    public ArrayList<Float> getHeights() {
        return sectionHeights;
    }

}
