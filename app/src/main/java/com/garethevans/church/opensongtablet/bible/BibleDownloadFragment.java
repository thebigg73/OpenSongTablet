package com.garethevans.church.opensongtablet.bible;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BibleDownloadBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.download));

        // Set up helpers
        setupHelpers();

        // Get the English basic bibles
        buildBibles_EN();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
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
        bibles_EN_URL.add("http://www.opensong.org/bible-modules/CEV.zip");
        bibles_EN_URL.add("http://www.opensong.org/bible-modules/KJV.zip");
        bibles_EN_URL.add("https://www.emp1.net/opensong/NASB.zip");
        bibles_EN_URL.add("http://www.opensong.org/bible-modules/NIV.zip");
        bibles_EN_URL.add("http://www.opensong.org/bible-modules/NKJV.zip");
        bibles_EN_URL.add("http://www.opensong.org/bible-modules/AMP.zip");
        bibles_EN_URL.add("http://www.opensong.org/bible-modules/MSG.zip");

        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(
                requireContext(),myView.translation, R.layout.view_exposed_dropdown_item,bibles_EN);
        myView.translation.setAdapter(exposedDropDownArrayAdapter);
        myView.translation.setText("Contemporary English Version");
    }

    private void setupListeners() {
        myView.downloadWiFiOnly.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"downloadWiFiOnly", true));
        myView.downloadWiFiOnly.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"downloadWiFiOnly",b));
        myView.download.setOnClickListener(v -> doDownload());
        myView.opensong.setOnClickListener(v -> openWeb("http://www.opensong.org/home/download"));
        myView.zefania.setOnClickListener(v -> openWeb("https://sourceforge.net/projects/zefania-sharp/files/Bibles/"));
    }

    private void doDownload() {
        // Only proceed if a valid connection
        if (checkInternet.isNetworkConnected(requireContext(),mainActivityInterface)) {
            Log.d(TAG,"Connected!");
            progressBar(true);
            // Run this in a new Thread
            new Thread(() -> {
                // Get the url based on the text position
                int position = -1;
                if (myView.translation.getText()!=null) {
                    position = bibles_EN.indexOf(myView.translation.getText().toString());
                }
                if (position>-1) {
                    String url = bibles_EN_URL.get(position);
                    String name = bibles_EN_URL.get(position).substring(bibles_EN_URL.get(position).lastIndexOf("/")+1);
                    String[] downloadInfo = webDownload.doDownload(requireContext(), url, name);
                    if (downloadInfo[1]!=null) {
                        Uri uri = Uri.parse(downloadInfo[1]);
                        // Now we need to extract the xmm file from the zip file
                        if (extractBibleZipFile(uri)) {
                            requireActivity().runOnUiThread(() -> mainActivityInterface.getShowToast().doIt(getString(R.string.success)));
                        } else {
                            requireActivity().runOnUiThread(() -> mainActivityInterface.getShowToast().doIt(getString(R.string.error)));
                        }
                    } else {
                        requireActivity().runOnUiThread(() -> mainActivityInterface.getShowToast().doIt(downloadInfo[0]));
                    }
                }
                requireActivity().runOnUiThread(() -> progressBar(false));
                }).start();
        } else {
            mainActivityInterface.getShowToast().doIt(getString(R.string.requires_internet));
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
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(requireContext(), zipUri);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            zis = new ZipInputStream(bufferedInputStream);
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName() != null && !ze.getName().startsWith("_")) {
                    Log.d(TAG,"ze.getName()="+ze.getName());
                    mainActivityInterface.getStorageAccess().createFile(requireContext(), mainActivityInterface, null, folder, subfolder, ze.getName());
                    Uri newUri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(), mainActivityInterface, folder, subfolder, ze.getName());
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(), newUri);

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
        mainActivityInterface.getStorageAccess().deleteFile(requireContext(),zipUri);

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
