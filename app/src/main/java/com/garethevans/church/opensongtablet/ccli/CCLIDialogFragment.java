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
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.databinding.CcliDialogBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class CCLIDialogFragment extends DialogFragment {

        CcliDialogBinding ccliDialogBinding;
        MainActivityInterface mainActivityInterface;
        Preferences preferences;
        StorageAccess storageAccess;
        CCLILog ccliLog;

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
            ccliDialogBinding = CcliDialogBinding.inflate(inflater, container, false);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Initialise helpers
            initialiseHelpers();



            ccliDialogBinding.close.setOnClickListener(v -> dismiss());

            // Set up the default values
            Uri uri = storageAccess.getUriForItem(requireContext(),preferences,"Settings","","ActivityLog.xml");
            String logsize = ccliLog.getLogFileSize(requireContext(),storageAccess,uri);
            ccliLog.getCurrentEntries(requireContext(),storageAccess,uri);
            String table = ccliLog.buildMyTable(requireContext(),preferences,logsize);

            new Thread(() -> requireActivity().runOnUiThread(() -> {
                WebView logWebView = ccliDialogBinding.ccliWebView;
                logWebView.getSettings().setBuiltInZoomControls(true);
                logWebView.getSettings().setDisplayZoomControls(false);
                logWebView.clearCache(true);
                logWebView.setInitialScale(100);
                logWebView.loadDataWithBaseURL(null,table, "text/html; charset=utf-8", "UTF-8",null);
            })).start();

            Log.d("d","logsize="+logsize);
            Log.d("d","table="+table);
            return ccliDialogBinding.getRoot();
        }

        private void initialiseHelpers() {
            preferences = new Preferences();
            storageAccess = new StorageAccess();
            ccliLog = new CCLILog();
        }
}
