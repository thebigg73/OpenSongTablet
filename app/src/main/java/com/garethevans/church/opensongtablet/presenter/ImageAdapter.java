package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import com.garethevans.church.opensongtablet.pdf.PDFPageItemInfo;
import com.garethevans.church.opensongtablet.pdf.PDFPageViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<PDFPageViewHolder> {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private final SongSectionsFragment songSectionsFragment;
    private final String TAG = "ImageAdapter";
    private final int viewWidth, viewHeight;
    private final String scaleType;
    private final float density;
    private ArrayList<PDFPageItemInfo> pdfPageItemInfos;
    private final String alphaChange = "alpha";
    private final float alphaoff = 0.4f;
    private SparseBooleanArray itemHighlighted;
    private int currentSection = 0;
    private boolean fakeClick;






    public ImageAdapter(Context c, SongSectionsFragment fragment,
                        MainActivityInterface mainActivityInterface, DisplayInterface displayInterface,
                        int viewWidth, int viewHeight) {
        this.c = c;
        this.songSectionsFragment = fragment;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        scaleType = mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale","W");
        density = c.getResources().getDisplayMetrics().density;
        setSongInfo();
    }

    @NonNull
    public PDFPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_pdf_page, parent, false);
        return new PDFPageViewHolder(mainActivityInterface,itemView);
    }

    public void setSongInfo() {
        if (pdfPageItemInfos == null) {
            pdfPageItemInfos = new ArrayList<>();
        } else {
            int oldSize = pdfPageItemInfos.size();
            pdfPageItemInfos.clear();
            notifyItemRangeRemoved(0, oldSize);
        }
        itemHighlighted = new SparseBooleanArray();


        // If this is a single image, set that up
        if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
            PDFPageItemInfo pdfPageItemInfo = new PDFPageItemInfo();
            pdfPageItemInfo.pageNum = 1;
            pdfPageItemInfo.pageNumText = "";
            String folder = mainActivityInterface.getSong().getFolder();
            String filename = mainActivityInterface.getSong().getFilename();
            pdfPageItemInfo.uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
            mainActivityInterface.getSong().setPdfPageCurrent(0);
            mainActivityInterface.getSong().setPdfPageCount(1);
            mainActivityInterface.getSong().setShowstartofpdf(true);
            if (mainActivityInterface.getStorageAccess().uriExists(pdfPageItemInfo.uri)) {
                Bitmap bitmap = mainActivityInterface.getProcessSong().getSongBitmap(folder, filename);
                pdfPageItemInfo.width = bitmap.getWidth();
                pdfPageItemInfo.height = bitmap.getHeight();
                // Set the song load success
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("songLoadSuccess", true);
                mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", mainActivityInterface.getSong().getFilename());
                mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", mainActivityInterface.getSong().getFolder());
                bitmap.recycle();
            } else {
                pdfPageItemInfo.uri = null;
            }
            pdfPageItemInfos.add(pdfPageItemInfo);


        } else {
            // This must be a custom image slide
            // TODO deal with this
            Log.d(TAG,"custom image slide not dealt with");

        }

    }

    @Override
    public void onBindViewHolder(@NonNull PDFPageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals(alphaChange)) {
                    // We want to update the highlight colour to off
                    holder.v.post(()->{
                        try {
                            float alphaval = alphaoff;
                            if (itemHighlighted.get(position,false)) {
                                alphaval = 1f;
                            }
                            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                                alphaval = 1f;
                                //pageInfos.get(position).alpha = 1f;
                            }
                            holder.v.setAlpha(alphaval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PDFPageViewHolder holder, int position) {
        if (pdfPageItemInfos.get(position).uri!=null) {
            float alpha = alphaoff;
            if (itemHighlighted.get(position, false)) {
                alpha = 1f;
            }

            CardView cardView = (CardView) holder.v;
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) && position == currentSection) {
                alpha = 1.0f;
            }
            float finalAlpha = alpha;

            // Hide the stuff not needed
            holder.pdfPageNumText.post(() -> {
                try {
                    holder.pdfPageNumText.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.pdfPageImage.post(() -> {
                try {
                    Glide.with(c).load(pdfPageItemInfos.get(position).uri).override(
                            pdfPageItemInfos.get(position).width,
                            pdfPageItemInfos.get(position).height).into(holder.pdfPageImage);
                    holder.pdfPageImage.setOnClickListener(view -> {
                        if (fakeClick) {
                            fakeClick = false;
                        } else {
                            sectionSelected(position);
                        }
                    });
                    holder.v.setOnLongClickListener(view -> {
                        // Do nothing other than consume the long press action
                        return true;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            cardView.post(() -> {
                try {
                    if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                        cardView.setAlpha(finalAlpha);
                    } else {
                        cardView.setAlpha(1f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // If we have a matching highlighter file...
            Bitmap highlighter = mainActivityInterface.getProcessSong().getHighlighterFile(
                    pdfPageItemInfos.get(position).width, pdfPageItemInfos.get(position).height);
            if (highlighter != null) {
                holder.pdfPageHighlight.post(() -> {
                    try {
                        holder.pdfPageHighlight.setVisibility(View.VISIBLE);
                        Glide.with(c).load(highlighter).override(pdfPageItemInfos.get(position).width,
                                pdfPageItemInfos.get(position).height).into(holder.pdfPageHighlight);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                holder.pdfPageHighlight.post(() -> {
                    try {
                        holder.pdfPageHighlight.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        // Not a pdf, but using this.
        // Single image files will always return 1 page, custom image slides maybe more
        return pdfPageItemInfos.size();
    }

    public void sectionSelected(int position) {
        if (pdfPageItemInfos.size()>position) {

            try {
                // Whatever the previously selected item was, change the alpha to the alphaOff value
                mainActivityInterface.getSong().setPdfPageCurrent(position);

                // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
                onTouchAction();

                // Only do this alpha change if we aren't in Performance mode
                if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                    itemHighlighted.put(currentSection,false);
                    //pageInfos.get(currentSection).alpha = alphaoff;
                    notifyItemChanged(currentSection, alphaChange);

                    // Now update the newly selected position
                    //pageInfos.get(position).alpha = 1.0f;
                    itemHighlighted.put(position,true);
                    notifyItemChanged(position, alphaChange);
                }

                Log.d(TAG,"position:"+position);

                // If stage mode or a pdf, update the presenter and send a nearby payload
                if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) ||
                        mainActivityInterface.getSong().getFiletype().equals("PDF") ||
                        mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                    // Send and update notification to Performance Fragment via the MainActivity (scrolls to position)
                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                        displayInterface.performanceShowSection(position);
                    } else {
                        displayInterface.presenterShowSection(position);
                    }

                    // Send a nearby notification (the client will ignore if not required or not ready)
                    if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                            mainActivityInterface.getNearbyConnections().getIsHost()) {
                        mainActivityInterface.getNearbyConnections().sendSongSectionPayload();
                    }
                }
                currentSection = position;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onTouchAction() {
        mainActivityInterface.getDisplayPrevNext().showAndHide();
        mainActivityInterface.updateOnScreenInfo("showhide");
        mainActivityInterface.showActionBar();
    }

}
