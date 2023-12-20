package com.garethevans.church.opensongtablet.bible;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BibleDownloadBinding;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BibleDownloadFragment extends Fragment {

    // This fragment deals with downloading various Bible translations
    // The list is non-exhaustive and instructions are shown to the user to download others
    // This just simplifies for some translations

    private MainActivityInterface mainActivityInterface;
    private final String TAG = "BibleDownloadFragment";
    private BibleDownloadBinding myView;
    private ArrayList<String> bibles_EN, bibles_EN_URL;
    private CheckInternet checkInternet;
    private WebDownload webDownload;
    private String success_string="", download_string="", website_bible_download_string,
        error_string="", requires_internet_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(download_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BibleDownloadBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_bible_download_string;

        // Set up helpers
        setupHelpers();

        // Get the English basic bibles
        buildBibles_EN();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            success_string = getString(R.string.success);
            download_string = getString(R.string.download);
            website_bible_download_string = getString(R.string.website_bible_download);
            error_string = getString(R.string.error);
            requires_internet_string = getString(R.string.requires_internet);
        }
    }
    private void setupHelpers() {
        checkInternet = new CheckInternet();
        webDownload = new WebDownload();
    }

    private void buildBibles_EN() {
        bibles_EN = new ArrayList<>();
        bibles_EN.add("Contemporary English Version");
        bibles_EN.add("King James Version");
        bibles_EN.add("New American Standard Bible");
        bibles_EN.add("New International Version");
        bibles_EN.add("New King James Version");
        bibles_EN.add("The Amplified Bible");
        bibles_EN.add("The Message");

        bibles_EN_URL = new ArrayList<>();
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/CEV.zip");
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/KJV.zip");
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/NASB.zip");
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/NIV.zip");
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/NKJV.zip");
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/AMP.zip");
        bibles_EN_URL.add("https://opensong.org/downloads/scripture/MSG.zip");

        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                    getContext(), myView.translation, R.layout.view_exposed_dropdown_item, bibles_EN);
            myView.translation.setAdapter(exposedDropDownArrayAdapter);
        }
        myView.translation.setText("Contemporary English Version");
    }

    private void setupListeners() {
        myView.downloadWiFiOnly.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("download_wifi_only", true));
        myView.downloadWiFiOnly.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("download_wifi_only",b));
        myView.download.setOnClickListener(v -> doDownload());
        myView.opensong.setOnClickListener(v -> openWeb("http://www.opensong.org/home/download"));
        myView.zefania.setOnClickListener(v -> openWeb("https://sourceforge.net/projects/zefania-sharp/files/Bibles/"));
    }

    private void doDownload() {
        // Only proceed if a valid connection
        if (getContext()!=null && checkInternet.isNetworkConnected(getContext(),mainActivityInterface)) {
            Log.d(TAG,"Connected!");
            progressBar(true);
            // Run this in a new Thread
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                try {
                    // Get the url based on the text position
                    int position = -1;
                    if (myView.translation.getText() != null) {
                        position = bibles_EN.indexOf(myView.translation.getText().toString());
                    }
                    if (position > -1) {
                        String url = bibles_EN_URL.get(position);
                        String name = bibles_EN_URL.get(position).substring(bibles_EN_URL.get(position).lastIndexOf("/") + 1);
                        String[] downloadInfo = webDownload.doDownload(getContext(), url, name);
                        if (downloadInfo[1] != null) {
                            Uri uri = Uri.parse(downloadInfo[1]);
                            // Now we need to extract the xmm file from the zip file
                            if (extractBibleZipFile(uri)) {
                                handler.post(() -> mainActivityInterface.getShowToast().doIt(success_string));
                            } else {
                                handler.post(() -> mainActivityInterface.getShowToast().doIt(error_string));
                            }
                        } else {
                            handler.post(() -> mainActivityInterface.getShowToast().doIt(downloadInfo[0]));
                        }
                    }
                    handler.post(() -> progressBar(false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                });
        } else {
            mainActivityInterface.getShowToast().doIt(requires_internet_string);
            progressBar(false);
        }
    }

    // This is used to extract downloaded bible XML files from the zip
    private boolean extractBibleZipFile(Uri zipUri) {
        boolean success = true;
        String folder = "OpenSong Scripture";
        String subfolder = "";

        // This bit could be slow, so it will likely be called in an async task
        ZipInputStream zis = null;
        try {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(zipUri);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            zis = new ZipInputStream(bufferedInputStream);
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName() != null && !ze.getName().startsWith("_")) {
                    Log.d(TAG,"ze.getName()="+ze.getName());
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" extractBibleZipFile createFile "+folder+"/"+subfolder+"/"+ze.getName());
                    mainActivityInterface.getStorageAccess().createFile(null, folder, subfolder, ze.getName());
                    Uri newUri = mainActivityInterface.getStorageAccess().getUriForItem(folder, subfolder, ze.getName());
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newUri);

                    try {
                        while ((count = zis.read(buffer)) != -1)
                            outputStream.write(buffer, 0, count);
                    } finally {
                        try {
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }
        // Delete the zip file
        if (getContext()!=null) {
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" extractBibleZipFile deleteFile "+zipUri);
            mainActivityInterface.getStorageAccess().deleteFile(zipUri);
        }

        return success;
    }

    private void progressBar(boolean show) {
        if (show) {
            myView.content.setAlpha(0.5f);
            myView.progressBar.setVisibility(View.VISIBLE);
        } else {
            myView.content.setAlpha(1.0f);
            myView.progressBar.setVisibility(View.GONE);
        }
    }

    private void openWeb(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
