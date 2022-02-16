package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
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
    private final String TAG = "PDFPageAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private final Context c;
    private ArrayList<PDFPageItemInfo> pageInfos;
    private ArrayList<Float> floatSizes;
    private final int viewWidth, viewHeight;
    private String pdfFolder, pdfFilename;
    private Uri pdfUri;
    private int totalPages;
    private float floatHeight;
    private final String scaleType;
    private final float density;
    private int currentSection = 0;
    private final String alphaChange = "alpha";

    public PDFPageAdapter(Context c, MainActivityInterface mainActivityInterface, DisplayInterface displayInterface, int viewWidth, int viewHeight) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        scaleType = mainActivityInterface.getPreferences().getMyPreferenceString(c,"songAutoScale","W");
        density = c.getResources().getDisplayMetrics().density;
        setSongInfo();
    }

    private void setSongInfo() {
        floatHeight = 0;
        pageInfos = new ArrayList<>();
        floatSizes = new ArrayList<>();

        pdfFolder = mainActivityInterface.getSong().getFolder();
        pdfFilename = mainActivityInterface.getSong().getFilename();
        pdfUri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,
                "Songs",pdfFolder,pdfFilename);
        if (mainActivityInterface.getStorageAccess().uriExists(c,pdfUri)) {
            ParcelFileDescriptor parcelFileDescriptor = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(c,pdfUri);
            PdfRenderer pdfRenderer = mainActivityInterface.getProcessSong().getPDFRenderer(parcelFileDescriptor);
            if (pdfRenderer!=null) {
                totalPages = pdfRenderer.getPageCount();
            } else {
                totalPages = 0;
            }
            mainActivityInterface.getSong().setPdfPageCount(totalPages);
            if (totalPages==0) {
                mainActivityInterface.getSong().setPdfPageCurrent(0);
            } else {
                mainActivityInterface.getSong().setPdfPageCurrent(1);
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
                    if (scaleType.equals("Y") && width > 0 && height > 0) {
                        scaleFactor = Math.min((float) viewWidth / (float) width, (float) viewHeight / (float) height);
                    } else if (scaleType.equals("W")) {
                        scaleFactor = (float) viewWidth / (float) width;
                    } else {
                        scaleFactor = 1f;
                    }

                    // Add up the heights
                    float itemHeight = height * scaleFactor + (4f * density);
                    floatHeight += itemHeight ;
                    floatSizes.add(itemHeight);

                    pageInfo.width = (int) (width * scaleFactor);
                    pageInfo.height = (int) itemHeight;

                    if (mainActivityInterface.getMode().equals("Stage")) {
                        pageInfo.alpha = 0.4f;
                    } else {
                        pageInfo.alpha = 1f;
                    }
                    pageInfos.add(pageInfo);
                    page.close();

                    // Set the song load success
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean(c, "songLoadSuccess", true);
                    mainActivityInterface.getPreferences().setMyPreferenceString(c, "songfilename", mainActivityInterface.getSong().getFilename());
                    mainActivityInterface.getPreferences().setMyPreferenceString(c, "whichSongFolder", mainActivityInterface.getSong().getFolder());
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
                    holder.v.setAlpha(pageInfos.get(position).alpha);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PDFPageViewHolder holder, int position) {
        int pageNum = pageInfos.get(position).pageNum;
        int width = pageInfos.get(position).width;
        int height = pageInfos.get(position).height;
        float alpha = pageInfos.get(position).alpha;
        CardView cardView = (CardView)holder.v;
        if (mainActivityInterface.getMode().equals("Stage") && position == currentSection) {
            alpha = 1.0f;
        }
        cardView.setAlpha(alpha);
        String pagetNumText = pageInfos.get(position).pageNumText;
        holder.pdfPageNumText.setText(pagetNumText);
        Bitmap pdfPageBitmap = mainActivityInterface.getProcessSong().getBitmapFromPDF(c,mainActivityInterface,
                pdfFolder,pdfFilename,pageNum,width,height,mainActivityInterface.getPreferences().getMyPreferenceString(c,"songAutoScale","W"));
        Glide.with(c).load(pdfPageBitmap).override(width,height).into(holder.pdfPageImage);
        // If we have a matching highlighter file...
        Bitmap pdfHighlighter = mainActivityInterface.getProcessSong().getPDFHighlighterBitmap(c,
                mainActivityInterface,mainActivityInterface.getSong(),width,height,pageNum);
        if (pdfHighlighter!=null) {
            Glide.with(c).load(pdfHighlighter).override(width,height).into(holder.pdfPageHighlight);
        }
        holder.pdfPageImage.setOnClickListener(view -> sectionSelected(pageNum));
        holder.pdfPageImage.setOnLongClickListener(view -> {
            // Do nothing other than consume the long press
            return true;
        });
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
        mainActivityInterface.showHideActionBar();
    }

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        // Only do this alpha change in stage mode

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        if (mainActivityInterface.getMode().equals("Stage")) {
            pageInfos.get(currentSection).alpha = 0.4f;
            notifyItemChanged(currentSection, alphaChange);

            // Now update the newly selected position
            if (position >= 0 && position < pageInfos.size()) {
                mainActivityInterface.getSong().setCurrentSection(position);
                currentSection = position;
                pageInfos.get(position).alpha = 1.0f;
                notifyItemChanged(position, alphaChange);
            }
        }

        // Send and update notification to Performance Fragment via the MainActivity (scrolls to position)
        displayInterface.performanceShowSection(position);
    }

    public ArrayList<Float> getHeights() {
        return floatSizes;
    }
}
