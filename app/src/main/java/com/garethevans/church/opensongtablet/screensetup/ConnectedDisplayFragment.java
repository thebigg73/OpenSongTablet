package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.SettingsConnectedDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ConnectedDisplayFragment extends Fragment {

    private final String TAG = "ConnectedDisplayFrag";
    private String pickThis;
    private SettingsConnectedDisplayBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Uri image1, image2, video1, video2;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsConnectedDisplayBinding.inflate(inflater,container,false);

        // Update views
        updateViews();

        return myView.getRoot();
    }


    private void updateViews() {
        String img1 = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundImage1", "ost_bg.png");
        String img2 = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundImage2", "ost_bg.png");
        String vid1 = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundVideo1", "");
        String vid2 = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundVideo2", "");

        image1 = getUriForPref(img1);
        image2 = getUriForPref(img2);
        video1 = getUriForPref(vid1);
        video2 = getUriForPref(vid2);

        updatePreview(myView.image1,image1);
        updatePreview(myView.image2,image2);
        updatePreview(myView.video1,video1);
        updatePreview(myView.video2,video2);

        pickThis = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundToUse", "img1");
        setSelectedBackgroundHighlight();

        myView.image1.setOnClickListener(v -> {
            pickThis = "img1";
            setSelectedBackgroundHighlight();
            mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                    "backgroundImage1",getUriString(image1));
            // TODO Actually do something!!
        });
        myView.image2.setOnClickListener(v -> {
            pickThis = "img2";
            setSelectedBackgroundHighlight();
            mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                    "backgroundImage2",getUriString(image2));
            // TODO Actually do something!!
        });
        myView.video1.setOnClickListener(v -> {
            pickThis = "vid1";
            setSelectedBackgroundHighlight();
            mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                    "backgroundVideo1",getUriString(video1));
            // TODO Actually do something!!
        });
        myView.video2.setOnClickListener(v -> {
            pickThis = "vid2";
            setSelectedBackgroundHighlight();
            mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                    "backgroundVideo2",getUriString(video2));
            // TODO Actually do something!!
        });
        myView.image1.setOnLongClickListener(v -> {
            pickThis = "img1";
            activityResultLauncher.launch("image/*");
            return true;
        });
        myView.image2.setOnLongClickListener(v -> {
            pickThis = "img2";
            activityResultLauncher.launch("image/*");
            return true;
        });
        myView.video1.setOnLongClickListener(v -> {
            pickThis = "vid1";
            activityResultLauncher.launch("video/*");
            return true;
        });
        myView.video2.setOnLongClickListener(v -> {
            pickThis = "vid2";
            activityResultLauncher.launch("video/*");
            return true;
        });
        // If on a Chromebook, allow right click context menu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            myView.image1.setOnContextClickListener(v -> {
                pickThis = "img1";
                activityResultLauncher.launch("image/*");
                return true;
            });
            myView.image2.setOnContextClickListener(v -> {
                pickThis = "img2";
                activityResultLauncher.launch("image/*");
                return true;
            });
            myView.video1.setOnContextClickListener(v -> {
                pickThis = "vid1";
                activityResultLauncher.launch("image/*");
                return true;
            });
            myView.video2.setOnContextClickListener(v -> {
                pickThis = "vid2";
                activityResultLauncher.launch("image/*");
                return true;
            });
        }
    }

    private Uri getUriForPref(String pref) {
        if (pref.equals("ost_bg.png")) {
            return mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                    mainActivityInterface.getPreferences(), "Backgrounds", "",
                    pref);
        } else if (pref.startsWith("../")) {
            pref = pref.replace("../","");
            return mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                    mainActivityInterface.getPreferences(), "", "",
                    pref);
        } else if (!pref.isEmpty()){
            return Uri.parse(pref);
        } else {
            return null;
        }
    }

    private void updatePreview (ImageView imageView, Uri uri) {
        if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(requireContext(),uri)) {
            String uriString = getUriString(uri);
            RequestOptions myOptions = new RequestOptions().fitCenter().override(120, 90);
            if (uriString.contains("ost_logo")) {
                GlideApp.with(requireContext()).load(R.drawable.ost_logo).apply(myOptions).into(imageView);
            } else if (uriString.contains("ost_bg")) {
                GlideApp.with(requireContext()).load(R.drawable.preso_default_bg).apply(myOptions).into(imageView);
            } else {
                GlideApp.with(requireContext()).load(uri).apply(myOptions).into(imageView);
            }
        }
    }

    private void setSelectedBackgroundHighlight() {
        int colorSelected = getResources().getColor(R.color.colorSecondary);
        int colorUnselected = getResources().getColor(R.color.colorPrimary);
        myView.image1.setBackgroundColor(colorUnselected);
        myView.image2.setBackgroundColor(colorUnselected);
        myView.video1.setBackgroundColor(colorUnselected);
        myView.video2.setBackgroundColor(colorUnselected);

        switch (pickThis) {
            case "img1":
                myView.image1.setBackgroundColor(colorSelected);
                break;
            case "img2":
                myView.image2.setBackgroundColor(colorSelected);
                break;
            case "vid1":
                myView.video1.setBackgroundColor(colorSelected);
                break;
            case "vid2":
                myView.video2.setBackgroundColor(colorSelected);
                break;
        }
    }

    // Listener for file chooser
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                Log.d(TAG,"uri="+uri);
                if (uri!=null) {
                    String which = "";
                    switch (pickThis) {
                        case "img1":
                            image1 = uri;
                            updatePreview(myView.image1, uri);
                            which = "backgroundImage1";
                            break;
                        case "img2":
                            image2 = uri;
                            updatePreview(myView.image2, uri);
                            which = "backgroundImage1";
                            break;
                        case "vid1":
                            video1 = uri;
                            updatePreview(myView.video1, uri);
                            which = "backgroundImage1";
                            break;
                        case "vid2":
                            video2 = uri;
                            updatePreview(myView.video2, uri);
                            which = "backgroundImage1";
                            break;
                    }
                    mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),
                            which,getUriString(uri));
                }
            });

    private String getUriString(Uri uri) {
        String uriString = "";
        if (uri!=null) {
            uriString = uri.toString();
            if (uriString.contains("OpenSong/")) {
                // Localised preLollipop
                uriString = uriString.substring(uriString.lastIndexOf("OpenSong/")+9);
                uriString = "../" + uriString;
            } else if (uriString.contains("OpenSong%2F")) {
                // Localised storageAccessFramework
                uriString = uriString.substring(uriString.lastIndexOf("OpenSong%2F")+11);
                uriString = uriString.replace("%2F","/");
                uriString = uriString.replace("%20"," ");
                uriString = "../" + uriString;
            }
        }
        Log.d(TAG,"uriString="+uriString);
        return uriString;
    }
}
