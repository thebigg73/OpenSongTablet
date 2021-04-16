package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsImportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ImportOptionsFragment extends Fragment {

    // This class asks the user which type of file should be imported.

    private MainActivityInterface mainActivityInterface;
    private SettingsImportBinding myView;
    private final String[] validFiles = new String[] {"text/plain","image/*","text/xml","application/xml","application/pdf","application/octet-stream"};
    private final String[] validBackups = new String[] {"application/zip","application/octet-stream"};
    private Thread thread;
    private Runnable runnable;
    private boolean alive = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.import_main));

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        myView.importFile.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_FILE_CHOOSER"),validFiles));
        myView.importOSB.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE"),validBackups));
        myView.importiOS.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE"),validBackups));
        myView.importOnline.setOnClickListener(v -> onlineSearch());
        myView.importBand.setOnClickListener(v -> importSample("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_leDR5bFFjRVVxVjA","Band.osb"));
        myView.importChurch.setOnClickListener(v -> importSample("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_lbVY3VVVOMkc5OGM","Church.osb"));
    }

    private void selectFile(int id, String[] mimeTypes) {
        Intent intent = mainActivityInterface.getStorageAccess().selectFileIntent(mimeTypes);
        requireActivity().startActivityForResult(intent, id);
    }

    private void onlineSearch() {

    }

    private void importSample(String url, String filename) {
        // Get the WebDownload
        WebDownload webDownload = mainActivityInterface.getWebDownload();
        // Run this in a new thread
        runnable = () -> {
            if (alive) {
                requireActivity().runOnUiThread(() -> myView.progressBar.setVisibility(View.VISIBLE));
            }
            String[] messages = webDownload.doDownload(getContext(),url,filename);
            if (alive) {
                requireActivity().runOnUiThread(() -> myView.progressBar.setVisibility(View.GONE));
            }
            if (messages[1]==null) {
                // There was a problem
                mainActivityInterface.getShowToast().doIt(getContext(),messages[0]);
            } else {
                mainActivityInterface.setImportFilename(filename);
                mainActivityInterface.setImportUri(Uri.parse(messages[1]));
                if (alive) {
                    requireActivity().runOnUiThread(() -> mainActivityInterface.navigateToFragment(null,R.id.importOSBFragment));
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        killThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        killThread();
        myView = null;
    }

    private void killThread() {
        alive = false;
        if (thread!=null) {
            thread.interrupt();
            runnable = null;
            thread = null;
        }
    }

}

/*

// TODO for now try reading in a pdf
                    Log.d("SongMwnuDialog","Getting here");
                            mainActivityInterface.navigateToFragment(R.id.importOptionsFragment);
                    */
/*NavHostFragment.findNavController(callingFragment)
                            .navigate(R.id.ac,null,null);*//*

                            //ocr.getTextFromPDF(getContext(),preferences,storageAccess,processSong,mainActivityInterface,"test","MAIN_Give thanks.pdf");
                            break;*/
