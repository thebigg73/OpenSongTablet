package com.garethevans.church.opensongtablet.customslides;

// This allows populating a recycler view with thumbnails of custom image slides
// We tap into a bit of the pdf logic in Song

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
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
import java.util.List;

public class ImageSlideAdapter  extends RecyclerView.Adapter<ImageSlideViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<ImageSlideItemInfo> slideInfos;
    private ArrayList<Float> floatSizes;
    private final int viewWidth, viewHeight;
    private int totalPages;
    private float floatHeight;
    private final String scaleType;
    private final float density;
    private int currentSection = 0;
    private final Context c;
    private final String alphaChange = "alpha";

    public ImageSlideAdapter(Context c, MainActivityInterface mainActivityInterface, DisplayInterface displayInterface, int viewWidth, int viewHeight) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        scaleType = mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale","W");
        density = c.getResources().getDisplayMetrics().density;
        setSongInfo();
    }

    private void setSongInfo() {
        floatHeight = 0;
        slideInfos = new ArrayList<>();
        floatSizes = new ArrayList<>();

        // The images are references in user3
        String[] images = mainActivityInterface.getSong().getUser3().trim().split("\n");
        totalPages = images.length;
        mainActivityInterface.getSong().setPdfPageCount(totalPages);
        if (totalPages==0) {
            mainActivityInterface.getSong().setPdfPageCurrent(-1);
        } else {
            mainActivityInterface.getSong().setPdfPageCurrent(0);
        }
        mainActivityInterface.getSong().setShowstartofpdf(true);

        for (int x=0; x<totalPages; x++) {
            ImageSlideItemInfo slideInfo = new ImageSlideItemInfo();
            slideInfo.pageNum = x;
            slideInfo.pageNumText = (x+1) + "/" + totalPages;

            if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                slideInfo.alpha = 0.4f;
            } else {
                slideInfo.alpha = 1f;
            }

            // Get the image sizes from the files
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(images[x]);

            slideInfo.uri = uri;

            int width;
            int height;

            try {
                ParcelFileDescriptor fd = c.getContentResolver().openFileDescriptor(uri, "r");
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                width = options.outWidth;
                height = options.outHeight;
                fd.close();
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

            // Add up the heights
            float itemHeight = height * scaleFactor + (4f * density);
            floatHeight += itemHeight;
            floatSizes.add(itemHeight);

            slideInfo.width = (int) (width * scaleFactor);
            slideInfo.height = (int) itemHeight;

            slideInfos.add(slideInfo);
        }
    }

    @NonNull
    @Override
    public ImageSlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_pdf_page, parent, false);
        return new ImageSlideViewHolder(mainActivityInterface,itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSlideViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals(alphaChange)) {
                    // We want to update the highlight colour to off
                    holder.v.setAlpha(slideInfos.get(position).alpha);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSlideViewHolder holder, int position) {
        int pageNum = slideInfos.get(position).pageNum;
        int width = slideInfos.get(position).width;
        int height = slideInfos.get(position).height;
        float alpha = slideInfos.get(position).alpha;
        CardView cardView = (CardView)holder.v;
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) && position == currentSection) {
            alpha = 1.0f;
        }
        cardView.setAlpha(alpha);

        Uri uri = getUri(position);
        String pageNumText = slideInfos.get(position).pageNumText;
        holder.imageSlideNumText.setText(pageNumText);

        Bitmap imageSlideBitmap = mainActivityInterface.getProcessSong().getBitmapFromUri(uri,width,height);

        Glide.with(c).load(imageSlideBitmap).override(width,height).into(holder.imageSlideImage);
        holder.imageSlideImage.setOnClickListener(view -> sectionSelected(pageNum));
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
        return (int) floatHeight;
    }

    private void onTouchAction() {
        mainActivityInterface.getDisplayPrevNext().showAndHide();
        mainActivityInterface.updateOnScreenInfo("showhide");
        mainActivityInterface.showActionBar();
    }

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        // Only do this alpha change in stage mode

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            slideInfos.get(currentSection).alpha = 0.4f;
            notifyItemChanged(currentSection, alphaChange);
        }

        // Now update the newly selected position
        if (position >= 0 && position < slideInfos.size()) {
            mainActivityInterface.getSong().setCurrentSection(position);
            currentSection = position;
            slideInfos.get(position).alpha = 1.0f;
            notifyItemChanged(position, alphaChange);
        }

        // Send and update notification to Performance/Presenter Fragment via the MainActivity
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
            displayInterface.presenterShowSection(position);
        } else {
            displayInterface.performanceShowSection(position);
        }
    }

    public ArrayList<Float> getHeights() {
        return floatSizes;
    }

    public Uri getUri(int position) {
        return slideInfos.get(position).uri;
    }
}
