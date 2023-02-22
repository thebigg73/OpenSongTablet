package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.List;

// This deals with previewing the PDF file.  Only available for Lollipop+

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PDFPageAdapter extends RecyclerView.Adapter<PDFPageViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private final Context c;
    private ArrayList<PDFPageItemInfo> pageInfos;
    private SparseBooleanArray itemHighlighted;
    private ArrayList<Float> floatSizes;
    private final int viewWidth, viewHeight;
    private String pdfFolder, pdfFilename;
    private Uri pdfUri;
    private int totalPages, inlineSetWidth;
    private float floatHeight;
    private final float alphaoff = 0.4f;
    private final String scaleType;
    private final float density;
    private int currentSection = 0;
    private final String alphaChange = "alpha";
    private boolean fakeClick;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "PDFPageAdapter";

    public PDFPageAdapter(Context c, MainActivityInterface mainActivityInterface,
                          DisplayInterface displayInterface, int viewWidth, int viewHeight, int inlineSetWidth) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        scaleType = mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale","W");
        density = c.getResources().getDisplayMetrics().density;
        this.inlineSetWidth = inlineSetWidth;
        setSongInfo();
    }

    private void setSongInfo() {
        floatHeight = 0;
        pageInfos = new ArrayList<>();
        floatSizes = new ArrayList<>();
        itemHighlighted = new SparseBooleanArray();

        pdfFolder = mainActivityInterface.getSong().getFolder();
        pdfFilename = mainActivityInterface.getSong().getFilename();
        pdfUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",pdfFolder,pdfFilename);
        if (mainActivityInterface.getStorageAccess().uriExists(pdfUri)) {
            ParcelFileDescriptor parcelFileDescriptor = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(pdfUri);
            PdfRenderer pdfRenderer = mainActivityInterface.getProcessSong().getPDFRenderer(parcelFileDescriptor);
            if (pdfRenderer!=null) {
                totalPages = pdfRenderer.getPageCount();
            } else {
                totalPages = 0;
            }
            mainActivityInterface.getSong().setPdfPageCount(totalPages);
            if (totalPages==0) {
                mainActivityInterface.getSong().setPdfPageCurrent(-1);
            } else {
                mainActivityInterface.getSong().setPdfPageCurrent(0);
            }
            mainActivityInterface.getSong().setShowstartofpdf(true);

            pageInfos = new ArrayList<>();
            if (pdfRenderer!=null) {
                for (int x = 0; x < totalPages; x++) {
                    PdfRenderer.Page page = pdfRenderer.openPage(x);
                    PDFPageItemInfo pageInfo = new PDFPageItemInfo();
                    pageInfo.pageNum = x;
                    pageInfo.pageNumText = (x + 1) + "/" + totalPages;
                    int width = page.getWidth();
                    int height = page.getHeight();
                    float scaleFactor;
                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                        float x_scale = (float)(viewWidth-inlineSetWidth)/(float)width;
                        float y_scale = (float)(viewHeight-mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()))/(float)height;
                        scaleFactor = Math.min(x_scale,y_scale);
                    } else {
                        if (scaleType.equals("Y") && width > 0 && height > 0) {
                            scaleFactor = Math.min((float) (viewWidth-inlineSetWidth) / (float) width, (float) viewHeight / (float) height);
                        } else if (scaleType.equals("W")) {
                            scaleFactor = (float) (viewWidth-inlineSetWidth) / (float) width;
                        } else {
                            scaleFactor = 1f;
                        }
                    }

                    // Add up the heights
                    float itemHeight = height * scaleFactor + (4f * density);
                    floatHeight += itemHeight ;
                    floatSizes.add(itemHeight);

                    pageInfo.width = (int) (width * scaleFactor);
                    pageInfo.height = (int) itemHeight;

                    pageInfos.add(pageInfo);
                    page.close();

                    // Set the song load success
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean("songLoadSuccess", true);
                    mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", mainActivityInterface.getSong().getFilename());
                    mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", mainActivityInterface.getSong().getFolder());
                }
            }

            try {
                if (pdfRenderer!=null) {
                    pdfRenderer.close();
                }
                if (parcelFileDescriptor!=null) {
                    parcelFileDescriptor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            totalPages = 0;
            floatHeight = 0;
        }
    }

    @NonNull
    @Override
    public PDFPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_pdf_page, parent, false);
        return new PDFPageViewHolder(mainActivityInterface,itemView);
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
        int pageNum = pageInfos.get(position).pageNum;
        int width = pageInfos.get(position).width;
        int height = pageInfos.get(position).height;
        float alpha = alphaoff;
        if (itemHighlighted.get(position,false)) {
            alpha = 1f;
        }
        String pagetNumText = pageInfos.get(position).pageNumText;

        CardView cardView = (CardView)holder.v;
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) && position == currentSection) {
            alpha = 1.0f;
        }
        float finalAlpha = alpha;

        // Update the views post to ensure drawing is ready
        holder.pdfPageNumText.post(()-> {
            try {
                holder.pdfPageNumText.setText(pagetNumText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Bitmap pdfPageBitmap = mainActivityInterface.getProcessSong().getBitmapFromPDF(
                pdfFolder,pdfFilename,pageNum,width,height,mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale","W"));
        // If we want to enable PDF trimming of whitespace
        //Bitmap newPageBitmap= mainActivityInterface.getProcessSong().trimBitmap(pdfPageBitmap);
        holder.pdfPageImage.post(()-> {
            try {
                Glide.with(c).load(pdfPageBitmap).override(width, height).into(holder.pdfPageImage);
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

        cardView.post(()->{
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
        Bitmap pdfHighlighter = mainActivityInterface.getProcessSong().getPDFHighlighterBitmap(mainActivityInterface.getSong(),width,height,pageNum);
        if (pdfHighlighter!=null) {
            holder.pdfPageHighlight.post(()->{
                try {
                    holder.pdfPageHighlight.setVisibility(View.VISIBLE);
                    Glide.with(c).load(pdfHighlighter).override(width, height).into(holder.pdfPageHighlight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            holder.pdfPageHighlight.post(()->{
                try {
                    holder.pdfPageHighlight.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if (pdfUri==null) {
            setSongInfo();
        }
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

    public void clickOnSection(int position) {
        fakeClick = true;
        sectionSelected(position);
    }

    public void sectionSelected(int position) {
        if (pageInfos.size()>position) {
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
                    mainActivityInterface.getSong().getFiletype().equals("PDF")) {
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

    public ArrayList<Float> getHeights() {
        return floatSizes;
    }
}
