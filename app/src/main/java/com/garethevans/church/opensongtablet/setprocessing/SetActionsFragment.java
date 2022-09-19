package com.garethevans.church.opensongtablet.setprocessing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.io.OutputStream;

public class SetActionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final String TAG = "SetActionsFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SettingsSetsBinding myView = SettingsSetsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.set_manage));

        initialiseLauncher();

        myView.createSet.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("newSet",getString(R.string.set_new),null,"SetActionsFragment",this,null));
        myView.loadSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("loadset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        myView.saveSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("saveset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        myView.renameSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("renameset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        myView.deleteSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("deleteset");
            mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
        });
        myView.bibleButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.bible_graph));
        myView.slideButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.customSlideFragment));
        myView.exportSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("exportset");
            mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
        });
        myView.backupSets.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("backupsets");
            mainActivityInterface.navigateToFragment(null,R.id.backupRestoreSetsFragment);
        });
        myView.restoreSets.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("restoresets");
            mainActivityInterface.navigateToFragment(null,R.id.backupRestoreSetsFragment);
        });
        myView.importSet.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            String[] mimetypes = {"text/xml","application/octet-stream"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activityResultLauncher.launch(intent);
        });
        return myView.getRoot();
    }

    private void initialiseLauncher() {
        // Initialise the launcher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    Log.d(TAG,"data="+data);

                    if (data != null) {
                        Uri contentUri = data.getData();
                        Log.d(TAG,"contentUri="+contentUri);
                        String importFilename = mainActivityInterface.getStorageAccess().getFileNameFromUri(contentUri);
                        //String location = mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri);
                        Log.d(TAG,"filename="+importFilename);
                        if (importFilename.endsWith(".osts") || !importFilename.contains(".")) {
                            // Copy the file into the sets folder
                            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(contentUri);
                            importFilename = importFilename.replace(".osts","");

                            Log.d(TAG,"importFile: "+importFilename);
                            Uri copyToUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", importFilename);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" initialiseLauncher Create Sets/"+importFilename+" deleteOld=true");
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyToUri, null, "Sets", "", importFilename);
                            OutputStream outputStream = mainActivityInterface.getStorageAccess().
                                    getOutputStream(copyToUri);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" copyPDF copyFile from "+contentUri+" to Sets/"+importFilename);
                            mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
                            mainActivityInterface.setWhattodo("loadset:"+importFilename);
                            mainActivityInterface.navigateToFragment(getString(R.string.deeplink_sets_manage), 0);
                        } else {
                            mainActivityInterface.getShowToast().doIt(getString(R.string.file_type)+" "+getString(R.string.unknown));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
