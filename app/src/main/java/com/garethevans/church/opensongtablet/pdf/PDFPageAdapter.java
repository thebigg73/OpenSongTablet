package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

// This deals with previewing the PDF file.  Only available for Lollipop+

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PDFPageAdapter extends RecyclerView.Adapter<PDFPageViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final String TAG = "PDFPageAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    private ArrayList<PDFPageItemInfo> pageInfos;
    private final int viewWidth, viewHeight;
    private String pdfFolder, pdfFilename;
    private Uri pdfUri;
    private int totalPages;
    private int totalHeight;
    private String scaleType;

    public PDFPageAdapter(Context c, MainActivityInterface mainActivityInterface, int viewWidth, int viewHeight) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        scaleType = mainActivityInterface.getPreferences().getMyPreferenceString(c,"songAutoScale","W");
        setSongInfo();
    }

    private void setSongInfo() {
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
            Log.d(TAG,"totalPages="+totalPages);
            totalHeight = 0;
            pageInfos = new ArrayList<>();
            for (int x=0;x<totalPages;x++) {
                PdfRenderer.Page page = pdfRenderer.openPage(x);
                PDFPageItemInfo pageInfo = new PDFPageItemInfo();
                pageInfo.pageNum = x;
                pageInfo.pageNumText = (x+1)+"/"+totalPages;
                int width = page.getWidth();
                int height = page.getHeight();
                float scaleFactor;
                if (scaleType.equals("Y") && width>0 && height>0) {
                    scaleFactor = Math.min((float)((float)viewWidth/(float)width),(float)((float)viewHeight/(float)height));
                } else if (scaleType.equals("W")) {
                    scaleFactor = (float)((float)viewWidth/(float)width);
                } else {
                    scaleFactor = 1f;
                }
                Log.d(TAG,"width="+width+"  height="+height+"  scaleFactor="+scaleFactor);
                pageInfo.width = (int)(width*scaleFactor);
                pageInfo.height = (int)(height*scaleFactor);
                pageInfos.add(pageInfo);
                page.close();
            }
            Log.d(TAG,"final total Height="+totalHeight);

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
            totalHeight = 0;
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
    public void onBindViewHolder(@NonNull PDFPageViewHolder holder, int position) {
        int pageNum = pageInfos.get(position).pageNum;
        int width = pageInfos.get(position).width;
        int height = pageInfos.get(position).height;
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
    }

    @Override
    public int getItemCount() {
        if (pdfUri==null) {
            setSongInfo();
        }
        return totalPages;
    }

    public int getHeight() {
        return totalHeight;
    }
}
