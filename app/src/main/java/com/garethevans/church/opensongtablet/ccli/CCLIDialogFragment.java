package com.garethevans.church.opensongtablet.ccli;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.databinding.CcliDialogBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class CCLIDialogFragment extends DialogFragment {

    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private CCLILog ccliLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        mainActivityInterface.songMenuActionButtonShow(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.garethevans.church.opensongtablet.databinding.CcliDialogBinding myView = CcliDialogBinding.inflate(inflater, container, false);
        Window w = requireDialog().getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialise helpers
        initialiseHelpers();

        myView.close.setOnClickListener(v -> dismiss());

        // Set up the default values
        Uri uri = storageAccess.getUriForItem(requireContext(), preferences, "Settings", "", "ActivityLog.xml");
        String logsize = ccliLog.getLogFileSize(requireContext(), storageAccess, uri);
        ccliLog.getCurrentEntries(requireContext(), storageAccess, uri);
        String table = ccliLog.buildMyTable(requireContext(), preferences, logsize);

        new Thread(() -> requireActivity().runOnUiThread(() -> {
            myView.ccliWebView.getSettings().setBuiltInZoomControls(true);
            myView.ccliWebView.getSettings().setDisplayZoomControls(false);
            myView.ccliWebView.clearCache(true);
            myView.ccliWebView.setInitialScale(100);
            myView.ccliWebView.loadDataWithBaseURL(null, table, "text/html; charset=utf-8", "UTF-8", null);
        })).start();

        Log.d("d", "logsize=" + logsize);
        Log.d("d", "table=" + table);
        return myView.getRoot();
    }

    private void initialiseHelpers() {
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        ccliLog = mainActivityInterface.getCCLILog();
    }
}
